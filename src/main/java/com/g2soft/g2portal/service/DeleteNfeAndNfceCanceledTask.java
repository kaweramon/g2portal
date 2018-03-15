package com.g2soft.g2portal.service;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.model.QuickSell;
import com.g2soft.g2portal.model.SightSale;
import com.g2soft.g2portal.service.dropbox.DropboxDeleteFile;

public class DeleteNfeAndNfceCanceledTask {

	Timer timer;
	JLabel labelTaskStatus;
	AppsBean appsBean;
	
	private static final Logger logger = (Logger) LogManager.getLogger(DeleteNfeAndNfceCanceledTask.class.getName());
	
	public DeleteNfeAndNfceCanceledTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.timer = new Timer(true);
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
	}
	
	public void deleteCanceledNfes() {
		timer.schedule(new DeleteNfeCanceledTimerTask(appsBean, labelTaskStatus, logger), 10 * 1000);
	}
	
	public void deleteCanceledNFCes() {
		this.timer = new Timer(true);
		this.timer.schedule(new DeleteNfceCanceledTimerTask(appsBean, labelTaskStatus, logger), 10 * 1000);
	}
}

class DeleteNfeCanceledTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	Calendar calendar;
	String cleanCnpj;
	Logger logger;
	DeleteNfeAndNfceCanceledTask deleteNfeAndNfceCanceledTask;
	
	public DeleteNfeCanceledTimerTask(AppsBean appsBean, JLabel labelTaskStatus, Logger logger) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.calendar =  Calendar.getInstance();
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.logger = logger;
		deleteNfeAndNfceCanceledTask = new DeleteNfeAndNfceCanceledTask(labelTaskStatus, appsBean);
	}
	
	@Override
	public void run() {
		this.labelTaskStatus.setText("Verificando Nfes canceladas para remoção");
		System.out.println("Verificando Nfes canceladas para remoção");
		logger.info("Verificando Nfes canceladas para remoção");
		List<SightSale> listSightSaleCanceled = this.appsBean.getListSightSaleCanceled();
		DropboxDeleteFile dropDeleteFile = new DropboxDeleteFile();
		if (listSightSaleCanceled != null && listSightSaleCanceled.size() > 0) {
			for (SightSale sightSale : listSightSaleCanceled)	{
				this.calendar.setTime(sightSale.getSellDate());
				dropDeleteFile.deleteFile(getDropboxPath(sightSale), labelTaskStatus);
			}
		}
		labelTaskStatus.setText("");
		deleteNfeAndNfceCanceledTask.deleteCanceledNFCes();
	}
	
	private String getDropboxPath(SightSale sightSale) {
		String path = "";
		if (sightSale.getSellDate() != null && sightSale.getNfeKey() != null) {
			String month = "";
			if (calendar.get(Calendar.MONTH) < 10)
				month = "0" + (calendar.get(Calendar.MONTH) + 1);
			else 
				month += (calendar.get(Calendar.MONTH) + 1);
			path = "/" + this.cleanCnpj + "/" + month + calendar.get(Calendar.YEAR) + "/" + 
				sightSale.getNfeKey() + "-procNfe.xml";
		}
		return path;
	}
	
}

class DeleteNfceCanceledTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	String cleanCnpj;
	Logger logger;
	Calendar calendar;
	UploadReportFilesTask uploadReportsTask;
	
	public DeleteNfceCanceledTimerTask(AppsBean appsBean, JLabel labelTaskStatus, Logger logger) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.logger = logger;
		this.calendar = Calendar.getInstance();
		this.cleanCnpj = appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.uploadReportsTask = new UploadReportFilesTask(labelTaskStatus);
	}
	
	@Override
	public void run() {
		System.out.println("Verificando nfces canceladas para remoção");
		this.labelTaskStatus.setText("Verificando Nfces canceladas para remoção");
		List<QuickSell> listQuickSellCanceled = this.appsBean.getListQuickSellCanceled();
		DropboxDeleteFile dropDeleteFile = new DropboxDeleteFile();
		if (listQuickSellCanceled != null && listQuickSellCanceled.size() > 0) {
			for (QuickSell quickSell : listQuickSellCanceled)
				dropDeleteFile.deleteFile(getDropBoxQuickSellPath(quickSell), labelTaskStatus);
		}
		labelTaskStatus.setText("");
		this.uploadReportsTask.uploadReports();
	}
	
	private String getDropBoxQuickSellPath(QuickSell quickSell) {
		String path = "";
		
		if (quickSell.getSellDate() != null && quickSell.getNfceKey() != null) {
			String month = "";
			this.calendar.setTime(quickSell.getSellDate());
			if (calendar.get(Calendar.MONTH) < 10)
				month = "0" + (calendar.get(Calendar.MONTH) + 1);
			else 
				month += (calendar.get(Calendar.MONTH) + 1);
			path = "/" + this.cleanCnpj + "/NFCe_XML/" + month + calendar.get(Calendar.YEAR) + "/" + quickSell.getNfceKey() + ".xml";
		}
		
		return path;
	}
	
}
