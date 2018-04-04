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
	G2AppsManager g2AppsManager;
	
	private static final Logger logger = (Logger) LogManager.getLogger(DeleteNfeAndNfceCanceledTask.class.getName());
	
	public DeleteNfeAndNfceCanceledTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.timer = new Timer(true);
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.g2AppsManager = g2AppsManager;
	}
	
	public void deleteCanceledNfes() {
		timer.schedule(new DeleteNfeCanceledTimerTask(appsBean, labelTaskStatus, logger, g2AppsManager), 10 * 1000);
	}
	
	public void deleteCanceledNFCes() {
		this.timer = new Timer(true);
		this.timer.schedule(new DeleteNfceCanceledTimerTask(appsBean, labelTaskStatus, logger, g2AppsManager), 10 * 1000);
	}
}

class DeleteNfeCanceledTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	Calendar calendar;
	String cleanCnpj;
	Logger logger;	
	DeleteNfeAndNfceCanceledTask deleteNfeAndNfceCanceledTask;
	G2AppsManager g2AppsManager;
	
	
	public DeleteNfeCanceledTimerTask(AppsBean appsBean, JLabel labelTaskStatus, Logger logger, G2AppsManager g2AppsManager) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.calendar =  Calendar.getInstance();
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.g2AppsManager = g2AppsManager;
		this.logger = logger;
		deleteNfeAndNfceCanceledTask = new DeleteNfeAndNfceCanceledTask(labelTaskStatus, appsBean, g2AppsManager);
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
				dropDeleteFile.deleteFile("/" + this.cleanCnpj + "/NFE/" + 
						this.g2AppsManager.getPathDate(sightSale.getSellDate()) + "/" + 
						sightSale.getNfeKey() + "-procNfe.xml", labelTaskStatus);
			}
		}
		labelTaskStatus.setText("");
		deleteNfeAndNfceCanceledTask.deleteCanceledNFCes();
	}
	
}

class DeleteNfceCanceledTimerTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelTaskStatus;
	String cleanCnpj;
	Logger logger;
	Calendar calendar;
	UploadReportFilesTask uploadReportsTask;
	G2AppsManager g2AppsManager;
	
	public DeleteNfceCanceledTimerTask(AppsBean appsBean, JLabel labelTaskStatus, Logger logger, G2AppsManager g2AppsManager) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.logger = logger;
		this.calendar = Calendar.getInstance();
		this.cleanCnpj = appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.g2AppsManager = g2AppsManager;
		this.uploadReportsTask = new UploadReportFilesTask(labelTaskStatus, g2AppsManager);
	}
	
	@Override
	public void run() {
		System.out.println("Verificando nfces canceladas para remoção");
		this.labelTaskStatus.setText("Verificando Nfces canceladas para remoção");
		List<QuickSell> listQuickSellCanceled = this.appsBean.getListQuickSellCanceled();
		DropboxDeleteFile dropDeleteFile = new DropboxDeleteFile();
		if (listQuickSellCanceled != null && listQuickSellCanceled.size() > 0) {
			for (QuickSell quickSell : listQuickSellCanceled)
				dropDeleteFile.deleteFile("/" + this.cleanCnpj + "/NFCe/" + 
						this.g2AppsManager.getPathDate(quickSell.getSellDate()) + ".xml", labelTaskStatus);
		}
		labelTaskStatus.setText("");
		this.uploadReportsTask.uploadReports();
	}
	
}
