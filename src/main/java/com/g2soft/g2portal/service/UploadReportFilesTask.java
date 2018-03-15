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
	
	public UploadReportFilesTask(JLabel labelTaskStatus) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = new AppsBean();
	}
	
	public void uploadReports() {
		this.timer = new Timer(true);
		this.timer.schedule(new UploadReportFilesTimerTask(labelTaskStatus, appsBean), 5 * 1000);
	}
}

class UploadReportFilesTimerTask extends TimerTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	DropboxUploadFile dropUpload;
	UploadSpedFilesTask uploadSpedTask;
	
	public UploadReportFilesTimerTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.dropUpload = new DropboxUploadFile();
		this.uploadSpedTask = new UploadSpedFilesTask(labelTaskStatus, appsBean);
	}
	
	@Override
	public void run() {
		System.out.println("Chamando task upload reports");
		this.dropUpload.uploadReports(labelTaskStatus, this.appsBean.getClientCnpj().replaceAll("[^0-9]", ""));
		this.labelTaskStatus.setText("");
		this.uploadSpedTask.uploadSpedFiles();
	}
	
}