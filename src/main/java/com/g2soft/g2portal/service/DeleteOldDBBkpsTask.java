package com.g2soft.g2portal.service;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.service.dropbox.DropboxDeleteFile;

public class DeleteOldDBBkpsTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	Timer timer;
	G2AppsManager g2AppsManager;
	
	public DeleteOldDBBkpsTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.timer = new Timer(true);
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.g2AppsManager = g2AppsManager;
	}
	
	public void deleteOldBkps() {
		this.timer.schedule(new DeleteOldDBBkpsTimerTask(labelTaskStatus, appsBean, g2AppsManager), 5 * 1000);
	}
}

class DeleteOldDBBkpsTimerTask extends TimerTask {

	JLabel labelTaskStatus;
	AppsBean appsBean;
	DropboxDeleteFile dropDelete;
	String cleanCnpj;
	private UploadNfeTask uploadNfeTask;
	private G2AppsManager g2AppsManager;
	private static final Logger logger = (Logger) LogManager.getLogger(DeleteOldDBBkpsTimerTask.class.getName());
	
	public DeleteOldDBBkpsTimerTask(JLabel labelTaskStatus, AppsBean appsBean, G2AppsManager g2AppsManager) {
		this.labelTaskStatus = labelTaskStatus;
		this.appsBean = appsBean;
		this.dropDelete = new DropboxDeleteFile();
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.g2AppsManager = g2AppsManager;
		this.uploadNfeTask = new UploadNfeTask(labelTaskStatus, g2AppsManager);
	}
	
	@Override
	public void run() {
		logger.info("iniciando task para deletar bkps antigos de BD");
		File folderBkp = new File(this.appsBean.getBackupPath());
		if (folderBkp.exists() && folderBkp.listFiles().length > 0) {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("d/MM/yyyy");
			for (File file : folderBkp.listFiles()) {
				String dateStr = file.getName().substring(0,2) + "/" + file.getName().substring(2, 4) + "/" + 
						file.getName().substring(4, file.getName().length());
				LocalDate date = LocalDate.parse(dateStr, dtf);
				if (date.isBefore(LocalDate.now().minusDays(1)))
					dropDelete.deleteFile("/" + this.cleanCnpj + "/Backup/" + g2AppsManager.getPathDate(new Date()) + "/" 
							+ file.getName(), labelTaskStatus);
			}
		}
		this.labelTaskStatus.setText("");
		this.uploadNfeTask.uploadNfeXmls10Min();
	}
	
}