package com.g2soft.g2portal.service;

import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.service.dropbox.DropboxUploadFile;

public class UploadReportFilesTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	Timer timer;
	G2AppsManager g2AppsManager;
	
	public UploadReportFilesTask(JLabel labelTaskStatus, G2AppsManager g2AppsManager) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = new AppsBean();
		this.g2AppsManager = g2AppsManager;
	}
	
	public void uploadReports() {
		this.timer = new Timer(true);
		this.timer.schedule(new UploadReportFilesTimerTask(labelTaskStatus, appsBean, g2AppsManager), 5 * 1000);
	}
}

class UploadReportFilesTimerTask extends TimerTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	DropboxUploadFile dropUpload;
	UploadSpedFilesTask uploadSpedTask;

	public UploadReportFilesTimerTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.dropUpload = new DropboxUploadFile(g2AppsManager);
		this.uploadSpedTask = new UploadSpedFilesTask(labelTaskStatus, appsBean, g2AppsManager);
	}
	
	@Override
	public void run() {
		System.out.println("Chamando task upload reports");
		this.dropUpload.uploadReports(labelTaskStatus, this.appsBean.getClientCnpj().replaceAll("[^0-9]", ""));
		this.labelTaskStatus.setText("");
		this.uploadSpedTask.uploadSpedFiles();
	}
	
}