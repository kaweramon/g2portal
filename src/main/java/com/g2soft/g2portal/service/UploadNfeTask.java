package com.g2soft.g2portal.service;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.model.QuickSell;
import com.g2soft.g2portal.model.SightSale;
import com.g2soft.g2portal.service.dropbox.DropboxUploadFile;

public class UploadNfeTask {

	Timer timer;
	AppsBean appsBean;
	JLabel labelTaskStatus;
	G2AppsManager g2AppsManager;
	
	public UploadNfeTask(JLabel labelTaskStatus, G2AppsManager g2AppsManager) {
		this.timer = new Timer(true);
		this.appsBean = new AppsBean();
		this.labelTaskStatus = labelTaskStatus;
		this.g2AppsManager = g2AppsManager;
	}
	
	public void uploadNfeXmls() {
		timer.schedule(new UploadNfeTimerTask(appsBean, labelTaskStatus, g2AppsManager), 20 * 1000);
	}
	
	public void uploadNFCeXmls() {
		this.timer = new Timer(true);
		timer.schedule(new UploadNfCeTimerTask(appsBean, labelTaskStatus, g2AppsManager), 10 * 1000);
	}
	
	public void uploadNfeXmls10Min() {
		timer.schedule(new UploadNfeTimerTask(appsBean, labelTaskStatus, g2AppsManager), 600 * 1000);
	}
}

class UploadNfeTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	private static String PATH_XML = "C:\\G2 arquivos Xml\\";
	private UploadNfeTask uploadNfeTask;
	private G2AppsManager g2AppsManager;
	private static final Logger logger = (Logger) LogManager.getLogger(UploadNfeTimerTask.class.getName());
	
	public UploadNfeTimerTask(AppsBean appsBean, JLabel labelTaskStatus, G2AppsManager g2AppsManager) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.g2AppsManager = g2AppsManager;
		this.uploadNfeTask = new UploadNfeTask(labelTaskStatus, g2AppsManager);
	}
	
	@Override
	public void run() {
		this.labelTaskStatus.setText("Verificando NFe para upload");
		System.out.println("Verificando NFe para upload");
		logger.info("Verificando NFe para upload");
		List<SightSale> listSightSale = this.appsBean.getListSightSaleNotUploaded();
		DropboxUploadFile dropboxUpload = new DropboxUploadFile(g2AppsManager);
		for (SightSale sightSale : listSightSale) {
			if (sightSale.getNfeStatus().equals("Enviada") && 
					sightSale.getStatus().equals("Concluido") && 
					(sightSale.getOnlineShipping() == null || sightSale.getOnlineShipping().equals("Nao Enviada"))) {
				
				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-3"));
				calendar.setTimeInMillis(sightSale.getSellDate().getTime());
				File file = getXmlFile(sightSale.getNfeKey());
				if (file != null) {
					String pathDropbox = "/" + getCleanCnpj(appsBean) + "/NFE/" + 
							g2AppsManager.getPathDate(sightSale.getSellDate()) + "/" + file.getName();
					if (dropboxUpload.uploadFile(file, labelTaskStatus, pathDropbox))
						appsBean.updateNfeStatusUploadedOk(sightSale);
				}
			}
		}
		labelTaskStatus.setText("");
		this.uploadNfeTask.uploadNFCeXmls();
	}

	private String getCleanCnpj(AppsBean appsBean) {
		return appsBean.getClientCnpj().replaceAll("[^0-9]", "");
	}
	
	private File getXmlFile(String nfeKey) {
		File fileXml = new File(PATH_XML + nfeKey + "-procNfe.xml");
		
		if (fileXml.exists())
			return fileXml;
		
		return null;
	}
	
}

class UploadNfCeTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	private String cleanCnpj;
	private static String PATH_XML = "C:\\G2 Soft\\NFCe_XML\\";
	Calendar calendar;
	DeleteNfeAndNfceCanceledTask deleteNfsTask;
	private static final Logger logger = (Logger) LogManager.getLogger(UploadNfCeTimerTask.class.getName());
	private G2AppsManager g2AppsManager;
	
	public UploadNfCeTimerTask(AppsBean appsBean, JLabel labelTaskStatus, G2AppsManager g2AppsManager) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.cleanCnpj = getCleanCnpj();
		this.calendar = Calendar.getInstance();
		this.g2AppsManager = g2AppsManager;
		this.deleteNfsTask = new DeleteNfeAndNfceCanceledTask(labelTaskStatus, appsBean, g2AppsManager);
	}
	
	@Override
	public void run() {
		this.labelTaskStatus.setText("Verificando NFCe para upload");
		System.out.println("Verificando NFCe para upload");
		logger.info("Verificando NFCe para upload");
		List<QuickSell> listQuickSell = this.appsBean.getListQuickSellNotUploaded();
		DropboxUploadFile dropboxUpload = new DropboxUploadFile(g2AppsManager);
		
		for (QuickSell quickSell : listQuickSell) {
			calendar.setTime(quickSell.getSellDate());
			File file = this.getXmlFile(calendar, quickSell.getNfceKey());
			if (file != null) {
				String pathDropbox = "/" + this.cleanCnpj + "/NFCe/" + 
						g2AppsManager.getPathDate(quickSell.getSellDate()) + "/" + quickSell.getNfceKey() + ".xml";
				if (dropboxUpload.uploadFile(file, labelTaskStatus, pathDropbox))
					this.appsBean.updateNfceQuickSellStatusUploadedOk(quickSell);
			}
		}
		labelTaskStatus.setText("");
		this.deleteNfsTask.deleteCanceledNfes();
	}
	
	private String getCleanCnpj() {
		return this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
	}
	
	private File getXmlFile(Calendar calendar, String nfeKey) {		
		File fileXml = new File(PATH_XML + getPath(calendar, nfeKey));
		
		if (fileXml.exists())
			return fileXml;
		
		return null;
	}
	
	private String getPath(Calendar calendar, String nfeKey) {		
		return getMonthAndYearPath(calendar) + "\\" +  nfeKey + ".xml";
	}

	private String getMonthAndYearPath(Calendar calendar) {
		String month = "";
		if (calendar.get(Calendar.MONTH) < 10)
			month = "0" + (calendar.get(Calendar.MONTH) + 1);
		else 
			month += (calendar.get(Calendar.MONTH) + 1);
		return month + calendar.get(Calendar.YEAR);
	}
	
}
