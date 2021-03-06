package com.g2soft.g2portal.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JLabel;

public class DeleteTempFilesTask {

	Timer timer;
	
	public DeleteTempFilesTask(JLabel labelStatus, G2AppsManager g2AppsManager) {
		timer = new Timer(true);
		timer.schedule(new DeleteFiles(labelStatus, g2AppsManager), 30 * 1000);
	}
}

class DeleteFiles extends TimerTask {

	G2AppsManager g2AppsManager;
	JLabel labelStatus;
	
	public DeleteFiles(JLabel labelStatus, G2AppsManager g2AppsManager) {
		this.g2AppsManager = g2AppsManager;
		this.labelStatus = labelStatus;
	}
	
	@Override
	public void run() {
		labelStatus.setText("Excluindo arquivos tempor\u00E1rios");
		System.out.println("Excluindo arquivos tempor\u00E1rios");
		Integer currentVersion = g2AppsManager.getCurrentVersionFromConfig();
		if (currentVersion != null) {
			List<File> oldFiles = getOldFiles(currentVersion);
			if (oldFiles != null && oldFiles.size() > 0) {
				for (File file : oldFiles) {
					try {
						labelStatus.setText("Excluindo arquivo: " + file.getName());
						System.out.println("Excluindo arquivo: " + file.getName());
						if (file.exists()) {
							file.delete();	
							Thread.sleep(500);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}			
		}
		labelStatus.setText("");
	}
	
	private List<File> getOldFiles(Integer currentVersion) {
		List<File> oldFiles = new ArrayList<File>();
		File tempFolder = new File("C:\\G2 Soft\\Updater\\Temp");
		if (tempFolder.exists()) {
			for (File file : tempFolder.listFiles()) {
				Integer versionDownload = getVersionDownload(file.getName());
				if (versionDownload != null && versionDownload < currentVersion)
					oldFiles.add(file);
			}
			return oldFiles;
		}
		return null;
	}
	
	private Integer getVersionDownload(String versionDownloaded) {
		if (versionDownloaded != null && versionDownloaded.length() > 0)
			return Integer.parseInt(versionDownloaded.substring(versionDownloaded.indexOf("_") + 1, versionDownloaded.indexOf(".")));
		else
			return null;
	}
	
}