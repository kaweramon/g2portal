package com.g2soft.g2portal.service;

import java.io.File;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.service.dropbox.DropboxUploadFile;

public class UploadSpedFilesTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	Timer timer;
	G2AppsManager g2AppsManager;
	
	
	public UploadSpedFilesTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.timer = new Timer(true);
		this.g2AppsManager = g2AppsManager;
	}
	
	public void uploadSpedFiles() {
		timer.schedule(new UploadSpedFilesTimerTask(labelTaskStatus, appsBean, g2AppsManager), 5 * 1000);
	}
}

class UploadSpedFilesTimerTask extends TimerTask {

	JLabel labelTaskStatus; 
	AppsBean appsBean;
	DropboxUploadFile dropUpload;
	String cleanCnpj;
	private G2AppsManager g2AppsManager;
	private static final String SPED_PATH = "C:\\G2 Soft\\SpedFiscal";
	
	public UploadSpedFilesTimerTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.dropUpload = new DropboxUploadFile(g2AppsManager);
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.g2AppsManager = g2AppsManager;
	}
	
	@Override
	public void run() {
		System.out.println("Verificando arquivos Sped!");
		labelTaskStatus.setText("Verificando arquivos Sped");
		File pathFolder = new File(SPED_PATH);
		if (pathFolder.exists() && pathFolder.listFiles().length > 0) {
			for (File file : pathFolder.listFiles()) {
				String dropboxPath = "/" + this.cleanCnpj + "/SPED/" + this.g2AppsManager.getPathDate(new Date()) 
					+ "/" + file.getName();
				
				dropUpload.uploadFile(file, labelTaskStatus, dropboxPath);
			}
		}
		labelTaskStatus.setText("");
		new UploadBkpBDTask(labelTaskStatus, appsBean, g2AppsManager);
	}
	
}