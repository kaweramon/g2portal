package com.g2soft.g2portal.service.dropbox;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.g2soft.g2portal.model.Apps;
import com.g2soft.g2portal.service.G2AppsManager;

public class DropboxDownloadPackage {

	private static final String DROPBOX_ACCESS_TOKEN = "qG4Cd10pSGAAAAAAAAAADmqNd19bjOgL1O5z3RcdNVm9vn6CEl3mr5QPbHdYF2S1";
	private static final String G2SoftUpdateDir = "C:\\G2 Soft\\Updater";
	
	DbxRequestConfig configDropbox = new DbxRequestConfig("dropbox/g2adv");
	DbxClientV2 clientDropbox = new DbxClientV2(configDropbox, DROPBOX_ACCESS_TOKEN);
	G2AppsManager appManages = new G2AppsManager();
	private BufferedReader in;
	
	public void downloadPackage(JLabel labelTaskStatus, Apps app) {
		OutputStream out = null;
		try {
			File tempFolder = new File(G2SoftUpdateDir + "\\Temp");
			if (!tempFolder.exists())
				tempFolder.mkdir();
			out = new FileOutputStream(G2SoftUpdateDir + "\\Temp" + "\\" + app.getLink());
			DbxDownloader<FileMetadata> dl = clientDropbox.files().download("/" + app.getLink());
			long size = dl.getResult().getSize();
			
			dl.download(new ProgressOutputStream(size, out, new ProgressOutputStream.Listener() {
				@Override
				public void progress(long completed, long totalSize) {
					float percent = ((((float)completed)/totalSize) * 100);
					if (percent < 100) 
						labelTaskStatus.setText("Fazendo download do pacote de Atualiza\u00E7\u00E3o: " + Math.ceil(percent) + " %");
					else {
						labelTaskStatus.setText("");
						writeOnLog("download_concluido=" + app.getVersionUp());
						deleteFilesFolder();
						unpackUpdatePackage(app);						
					}
				}
			}));
		} catch (DbxException | IOException e) {
			e.printStackTrace();
		}
	}
	
	private void unpackUpdatePackage(Apps app) {
		ZipFile zip;
		Enumeration entries;
		String dest = G2SoftUpdateDir + "\\Files\\";
		try {
			zip = new ZipFile(G2SoftUpdateDir + "\\Temp\\" + app.getLink());
			entries = zip.entries();
			while(entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry)entries.nextElement();
				if (entry.isDirectory()) {
					(new File(dest + "\\" + entry.getName())).mkdir();
				} else {
					InputStream in = zip.getInputStream(entry);
					FileUtils.copyInputStreamToFile(in, new File(dest + "\\" + entry.getName()));
				}
			}
			writeOnLog("pacote_descompactado=" + app.getVersionUp());
			writeLastVersionDownloadedOnConfig(app);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeOnLog(String text) {
		try {
			File logFile = new File("C:\\G2 Soft\\logs\\logG2Portal.txt");			
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
			System.out.println(e.getMessage());
		}
	}
	
	private void writeLastVersionDownloadedOnConfig(Apps appG2) {
		File fileConfig = new File("C:\\G2 Soft\\config.ini");
		try {
			String contentConfig = FileUtils.readFileToString(fileConfig, "UTF-8");
			if (contentConfig.contains("versao_baixada")) {
				contentConfig = contentConfig.replaceAll("versao_baixada=" + getCurrentDownloadedVersionFromConfig(), "versao_baixada=" + appG2.getVersionUp());
				FileUtils.write(fileConfig, contentConfig, "UTF-8");
			} else { 
				BufferedWriter output = new BufferedWriter(new FileWriter("C:\\G2 Soft\\config.ini", true));
				output.newLine();
				output.write("versao_baixada=" + appG2.getVersionUp());
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Integer getCurrentDownloadedVersionFromConfig() {
		try {
			in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String line;
			while ((line = in.readLine()) != null) {				
				if (line.contains("versao_baixada")) {
					return Integer.parseInt(line.substring(line.indexOf("=") + 1, line.length()));
				} 
			}
			in.close();
		} catch (NumberFormatException e) {
			System.out.println(e);
		} catch (IOException e) {
			System.out.print(e);
		} 
		return null;
	}
	
	private void deleteFilesFolder() {
		File filesFolder = new File("C:\\G2 Soft\\Updater\\Files");
		try {
			if (FileUtils.waitFor(filesFolder, 2)) {
				System.out.println("Limpando pasta Files");
				FileUtils.cleanDirectory(filesFolder);
				Thread.sleep(2000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
