package com.g2soft.g2portal.service;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.service.dropbox.DropboxUploadFile;

public class UploadSpedFilesTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	Timer timer;
	
	public UploadSpedFilesTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.timer = new Timer(true);
	}
	
	public void uploadSpedFiles() {
		timer.schedule(new UploadSpedFilesTimerTask(labelTaskStatus, appsBean), 5 * 1000);
	}
}

class UploadSpedFilesTimerTask extends TimerTask {

	JLabel labelTaskStatus; 
	AppsBean appsBean;
	DropboxUploadFile dropUpload;
	String cleanCnpj;
	private static final String SPED_PATH = "C:\\G2 Soft\\SpedFiscal";
	private static final String PATHLOG = "C:\\G2 Soft\\logs\\logG2Portal.txt";
	
	public UploadSpedFilesTimerTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.dropUpload = new DropboxUploadFile();
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
	}
	
	@Override
	public void run() {
		System.out.println("Verificando arquivos Sped!");
		labelTaskStatus.setText("Verificando arquivos Sped");
		File pathFolder = new File(SPED_PATH);
		if (pathFolder.exists() && pathFolder.listFiles().length > 0) {
			for (File file : pathFolder.listFiles()) {
				try {
					if (!FileUtils.readFileToString(new File(PATHLOG), "UTF-8").contains(file.getName())) {
						String dropboxPath = "/" + this.cleanCnpj + "/SPED/" + file.getName();
						dropUpload.uploadFile(file, labelTaskStatus, dropboxPath);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		labelTaskStatus.setText("");
		new UploadBkpBDTask(labelTaskStatus, appsBean);
	}
	
	
	
}