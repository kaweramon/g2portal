package com.g2soft.g2portal.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JLabel;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.service.dropbox.DropboxUploadFile;

public class UploadBkpBDTask {

	AppsBean appsBean;
	Timer timer;
	JLabel labelTaskStatus;
	
	public UploadBkpBDTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.timer = new Timer(true);
		timer.schedule(new UploadBkpBDTimerTask(labelTaskStatus, appsBean), 5 * 1000);
	}
}

class UploadBkpBDTimerTask extends TimerTask {
	
	AppsBean appsBean;
	JLabel labelTaskStatus;
	private String cleanCnpj;
	Calendar calendar;
	DropboxUploadFile dropUpload;
	private static final Logger logger = (Logger) LogManager.getLogger(UploadBkpBDTimerTask.class.getName());
	
	public UploadBkpBDTimerTask(JLabel labelTaskStatus, AppsBean appsBean) {
		this.appsBean = appsBean;
		this.labelTaskStatus = labelTaskStatus;
		this.cleanCnpj = this.appsBean.getClientCnpj().replaceAll("[^0-9]", "");
		this.calendar = Calendar.getInstance();
		this.dropUpload = new DropboxUploadFile();
	}

	@Override
	public void run() {

		String pathBkp = this.appsBean.getBackupPath();
		System.out.println("Verificando Arquivos de Backup");
		this.labelTaskStatus.setText("Verificando Arquivos de Backup");
		logger.info("Verificando Arquivos de Backup");
		if (pathBkp != null && pathBkp.length() > 0) {
			String day = calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + (calendar.get(Calendar.DAY_OF_MONTH)) : 
				"" + (calendar.get(Calendar.DAY_OF_MONTH)); 
			String month = calendar.get(Calendar.MONTH) < 10 ? "0" + (calendar.get(Calendar.MONTH) + 1) : 
				"" + (calendar.get(Calendar.MONTH) + 1);
			String dateStr = day + month + calendar.get(Calendar.YEAR);
			File fileToday = new File(pathBkp + "\\" + dateStr);
			if  (fileToday.exists())
				compressFile(fileToday.getPath(), dateStr);
			else if (calendar.get(Calendar.DAY_OF_MONTH) > 1) {
				String yesterday = calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + (calendar.get(Calendar.DAY_OF_MONTH) - 1) : 
					"" + (calendar.get(Calendar.DAY_OF_MONTH) - 1);
				String dateYesterdayStr = yesterday + month + calendar.get(Calendar.YEAR);
				File folderYesterday = new File(pathBkp + "\\" + dateYesterdayStr);
				if (folderYesterday.exists())
					compressFile(folderYesterday.getPath(), dateYesterdayStr);
			}
		}
		labelTaskStatus.setText("");
	}
	
	private void compressFile(String path, String dateStr) {
		List<File> files = getFiles(path);
		if (files.size() > 0) {
			File fileZip = new File("bkpDB_" + dateStr + ".zip");
			fileZip.deleteOnExit();
			ZipOutputStream zipOut;
			try {
				zipOut = new ZipOutputStream(new FileOutputStream(fileZip));
				for (File file : files) {
					ZipEntry zipEntry = new ZipEntry(file.getName());
					try {
						zipOut.putNextEntry(zipEntry);
						zipOut.write(IOUtils.toByteArray(new FileInputStream(file)));
						zipOut.closeEntry();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try {
					zipOut.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (fileZip.length() > 0 && !this.dropUpload.hasFileUploaded(fileZip.getName()))
					dropUpload.uploadFile(fileZip, labelTaskStatus, "/" + this.cleanCnpj + "/Backup/" + fileZip.getName());
				fileZip.delete();
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private List<File> getFiles(String path) {
		List<File> files = new ArrayList<File>();
		File fileBkp = new File(path + "\\" + "Backup.sql");
		if (fileBkp.exists())
			files.add(fileBkp);
		File fileBkpCaixa = new File(path + "\\" + "BackupCaixa.sql");
		if (fileBkpCaixa.exists())
			files.add(fileBkpCaixa);
		return files;
	}
	
}