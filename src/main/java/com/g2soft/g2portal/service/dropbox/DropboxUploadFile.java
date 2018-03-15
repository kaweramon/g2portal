package com.g2soft.g2portal.service.dropbox;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.UploadErrorException;

public class DropboxUploadFile {

	private static final String DROPBOX_ACCESS_TOKEN = "qG4Cd10pSGAAAAAAAAAADmqNd19bjOgL1O5z3RcdNVm9vn6CEl3mr5QPbHdYF2S1";
	
	DbxRequestConfig configDropbox = new DbxRequestConfig("dropbox/g2adv");
	DbxClientV2 clientDropbox = new DbxClientV2(configDropbox, DROPBOX_ACCESS_TOKEN);
	private static final Logger logger = (Logger) LogManager.getLogger(DropboxUploadFile.class.getName());
	private static final String PATHLOG = "C:\\G2 Soft\\logs\\logG2Portal.txt";
	
	public boolean uploadFile(File file, JLabel labelTaskStatus, String path) {
		try {
			InputStream in = new FileInputStream(file);
			try {
				labelTaskStatus.setText("Enviando: " + file.getName());
				System.out.println("Fazendo upload para: " + path);
				logger.info("Fazendo upload para: " + path);
				this.clientDropbox.files().uploadBuilder(path).uploadAndFinish(in);
				Thread.sleep(1000);
				this.writeUploadOnLog("upload_concluido=" + file.getName());
			} catch (InterruptedException e) {
				logger.error(e);
				e.printStackTrace();
			}
			return true;
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			logger.error(e);
		} catch (UploadErrorException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			logger.error(e);
		} catch (DbxException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			logger.error(e);
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return false;
	}
	
	public void writeUploadOnLog(String text) {
		try {
			File logFile = new File(PATHLOG);			
			SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String strDateNow = dataFormat.format(new Date());
			if (!logFile.exists()) 
				logFile.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
			output.newLine();
			output.write(text + ", data=" + strDateNow);
			output.flush();
			output.close();			
		} catch (IOException e) {
			logger.error(e);
		}
	}
	
	public void uploadReports(JLabel labelTaskStatus, String cleanCnpj) {
		File pathReports = new File("C:\\G2 Soft\\Relatorios");
		if (pathReports.exists() && pathReports.listFiles().length > 0) {
			for (File file : pathReports.listFiles()) {
				try {
					if (!FileUtils.readFileToString(new File(PATHLOG), "UTF-8").contains(file.getName()))
						uploadFile(file, labelTaskStatus, "/" + cleanCnpj + "/Relatorios/" + file.getName());
				} catch (IOException e) {
					logger.error(e);
					e.printStackTrace();
				}
			}
		}
	}
	
	public boolean hasFileUploaded(String fileName) {
		try {
			return FileUtils.readFileToString(new File(PATHLOG), "UTF-8").contains(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
}
