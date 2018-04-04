package com.g2soft.g2portal.service.dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;

public class DropboxDeleteFile {

private static final String DROPBOX_ACCESS_TOKEN = "qG4Cd10pSGAAAAAAAAAADmqNd19bjOgL1O5z3RcdNVm9vn6CEl3mr5QPbHdYF2S1";
	
	DbxRequestConfig configDropbox = new DbxRequestConfig("dropbox/g2adv");
	DbxClientV2 clientDropbox = new DbxClientV2(configDropbox, DROPBOX_ACCESS_TOKEN);

	private static final String LOG_PATH = "C:\\G2 Soft\\logs\\logG2Portal.txt";
	
	private static final Logger logger = (Logger) LogManager.getLogger(DropboxDeleteFile.class.getName());
	
	public void deleteFile(String path, JLabel labelTaskStatus) {
		try {
			if (canDeleteFile(path)) {
				clientDropbox.files().getMetadata(path);
				labelTaskStatus.setText("Deletando arquivo: " + path);
				clientDropbox.files().deleteV2(path);
				writeDeleteOnLog("arquivo_deletado=" + path);
			}			
		} catch (GetMetadataErrorException e) {
			if (e.errorValue.isPath() && e.errorValue.getPathValue().isNotFound()) {
				System.out.println("Arquivo não encontrado: " + path);
				logger.error("Arquivo não encontrado: " + path);
				writeDeleteOnLog("arquivo_nao_encontrado=" + path);
			}
		} catch (DbxException e) {
			logger.error(e);
			e.printStackTrace();
		}
	}
	
	private boolean canDeleteFile(String path) {
		BufferedReader buffReader = null;
		try {
			File fileLog = new File(LOG_PATH);
			buffReader = new BufferedReader(new FileReader(fileLog));
			String currentLine;
			while((currentLine = buffReader.readLine()) != null) {
				if ((currentLine.contains("arquivo_nao_encontrado") || currentLine.contains("arquivo_deletado")) &&
						currentLine.contains(path)) {
					return false;
				}
			}
			buffReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buffReader != null)
				try {
					buffReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		
		return true;
	}
	
	public void writeDeleteOnLog(String text) {
		try {
			File logFile = new File(LOG_PATH);			
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
	
}
