package com.g2soft.g2portal.service;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.model.Apps;

public class G2AppsManager {

	private AppsBean appsBean;
	private static final Logger logger = (Logger) LogManager.getLogger(G2AppsManager.class.getName());
	String ip = "";
	private BufferedReader in;
	private Apps appG2FromServer;
	
	private List<String> listApps;
	
	public G2AppsManager() {
		listApps = Arrays.asList("G2Update", "G2Online", "G2NFeMonitor", "G2Transmissao");
		this.appsBean = new AppsBean();
		this.appsBean.setHostname();
		setIp();
		this.appG2FromServer = appsBean.getAppG2Server();
	}
	
	public void closeApp(String appName) {
		 	Process proc;
			try {
				System.out.println("Fechando app: " + appName);
				proc = Runtime.getRuntime().exec("TSKILL " + appName);
				proc.waitFor(); //Wait for it to finish
			} catch (IOException e) {
				logger.error(e.getMessage());
			} catch (InterruptedException e) {
				logger.error(e.getMessage());
			}
	}
	
	public Boolean isAppRunning(String g2ProcessName) {
		String line;
		String pidInfo ="";
		try {
			Process process = Runtime.getRuntime().exec(System.getenv("windir") + "\\System32\\"+"tasklist.exe");
			BufferedReader input =  new BufferedReader(new InputStreamReader(process.getInputStream()));
			while ((line = input.readLine()) != null) {
			    pidInfo+=line; 
			}
			input.close();
			if (pidInfo.contains(g2ProcessName)) {
				return true;
			} else {
				return false;
			}
			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	public void verifyUpdate(JLabel labelTaskStatus, JLabel labelG2, JLabel labelPDV, JLabel labelG2Version) {
		System.out.println("Verificando Atualização");
		labelTaskStatus.setText("Verificando Atualiza\u00E7\u00E3o");
		String g2TempNameAndVersion = labelG2.getText();
		String pdvTempNamAndVersion = labelPDV.getText();
		File filePathG2 = null;
		if (ip.contains("localhost")) {
			filePathG2 = new File("C:\\G2 Soft\\Updater\\Files");	
		} else {
			filePathG2 = new File("\\\\" + ip.replace(" ", "") + "\\G2 Soft\\Updater\\Files");
		}
		System.out.println("Caminho G2: " + filePathG2.toString());
		
		if (filePathG2 != null && filePathG2.exists()) {
			File[] listFiles = filePathG2.listFiles();
			if (listFiles != null && listFiles.length > 0) {
				labelG2.setText("Atualizando ...");
				labelG2.setForeground(Color.RED);
				labelG2.setEnabled(false);
				labelPDV.setText("Atualizando ...");
				labelPDV.setForeground(Color.RED);
				File dest = new File("C:\\G2 Soft");
				for (File file: listFiles) {
					try {
						System.out.println("Copiando: " + file.getName());
						labelTaskStatus.setText("Copiando Arquivo: " + file.getName());
						logger.info("Copiando arquivo: " + file.getName());
						if (file.isDirectory()) 
							FileUtils.copyDirectoryToDirectory(file, dest);
						else if (file.isFile())						
							copyFile(file, dest);
					} catch (IOException e) {
						logger.error(e.getMessage());
					}
				}
				writeCurrentVersionOnConfig(appG2FromServer.getVersionUp());
				writeTransferedVersion(appG2FromServer.getVersionUp());
				changeG2Version(appG2FromServer.getVersionUp(), labelG2Version);
				appsBean.updateAppVersion("G2", appG2FromServer.getVersionUp());
				labelG2.setText(g2TempNameAndVersion);
				labelG2.setForeground(Color.BLACK);
				labelG2.setEnabled(true);
				labelPDV.setText(pdvTempNamAndVersion);
				labelPDV.setForeground(Color.BLACK);
			} else {
				System.out.println("Diretório Vazio");
				logger.error("Diretório Vazio, não existe ou sem permissão");
			}
		} else {
			System.out.println("diretorio nao existe");
			logger.error("Diretório Vazio, não existe ou sem permissão");
		}
	}
	
	/***
	 * Verifica qual o hostname (localhost ou ip do servidor) 
	 */
	public void setIp() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String line;
			while((line = in.readLine()) != null) {
			    if (line.contains("hostname")) {
			    	ip = line.substring(line.indexOf("=") + 1, line.length());
			    	break;
			    }
			}
			in.close();
		} catch (IOException e1) {
			logger.error(e1.getMessage());
			e1.printStackTrace();
		}

	}
	
	public Boolean projectHasUpdate(String projectName) {
		logger.info("Verificando atualização");
		if (appG2FromServer == null) 
			appG2FromServer = this.appsBean.getAppByName("G2");
		
		File fileConfig = new File("C:\\G2 Soft\\config.ini");
		String contentConfig;
		try {			
			contentConfig = FileUtils.readFileToString(fileConfig, "UTF-8");
			if (appG2FromServer != null && appG2FromServer.getVersionUp() != null ) {
				// config não tem versao atual e nenhum transferencia
				if (!contentConfig.contains("versao_atual_g2")) {
					System.out.println("Não tem versao no config");					
					return true;
				} else {
					Integer currentVersionFromConfig = getCurrentVersionFromConfig();
					if (currentVersionFromConfig != null && appG2FromServer.getVersionUp() > currentVersionFromConfig)
						return true;
				}				 
			}
			System.out.println("Nenhuma Atualização disponível");
			logger.info("Nenhuma Atualização disponível");
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
		return false;
	}
	
	public void openApp(String appName) {
		try {
			File fileToOpen = new File("C:/G2 Soft/" + appName + ".exe");
			if (fileToOpen.exists()) {
				System.out.println("Abrindo app: " + fileToOpen);
				java.awt.Desktop.getDesktop().open(fileToOpen);
			}			
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	
	public void copyFile(File file, File folderDest) {
		try {
			if (file.getName().contains(".exe")) {
				String appName = file.getName().replaceAll(".exe", "");
				if (listApps.contains(appName) && isAppRunning(appName)) {
					closeApp(appName);
					Thread.sleep(500);
				} 
			}
			FileUtils.copyFileToDirectory(file, folderDest);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (InterruptedException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public Boolean isClientBillsLate() {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		String clientCNPJ = appsBean.getClientCnpj();
		if (clientCNPJ != null && !clientCNPJ.isEmpty()) {
			HttpGet httpGet = new HttpGet("http://177.75.66.175:6464/bill-to-pay/is-client-bills-late?cnpj=" + clientCNPJ);
			CloseableHttpResponse response = null;
			try {
				response = httpClient.execute(httpGet);
				return Boolean.parseBoolean(EntityUtils.toString(response.getEntity()));
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					response.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
	
	public void writeCurrentVersionOnConfig(Integer appVersion) {
		try {
			in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String path = "C:\\G2 Soft\\config.ini";
		try {
			String contentConfig = FileUtils.readFileToString(new File(path), "UTF-8");
			BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
			if (contentConfig.contains("versao_atual_g2")) {
				contentConfig = contentConfig.replace("versao_atual_g2=" + getCurrentVersionFromConfig(), "versao_atual_g2=" + appVersion);
				FileUtils.write(new File(path), contentConfig, "UTF-8");
			} else {
				output.newLine();
				output.write("versao_atual_g2=" + appVersion);
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeTransferedVersion(Integer appVersion) {
		try {
			File logFile = new File("C:\\G2 Soft\\logs\\logG2Portal.txt");			
			SimpleDateFormat dataFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String strDateNow = dataFormat.format(new Date());
			if (appVersion != null) {
				if (!logFile.exists()) {
					logFile.createNewFile();
				}				
				BufferedWriter output = new BufferedWriter(new FileWriter(logFile, true));
				output.newLine();
				output.write("transferencia=(versao=" + appVersion + ", data=" + strDateNow + ")");
				output.flush();
				output.close();			
			}			
		} catch (IOException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
	}
	
	public Integer getCurrentVersionFromConfig() {
		try {
			in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String line;
			while ((line = in.readLine()) != null) {				
				if (line.contains("versao_atual_g2")) {
					return Integer.parseInt(line.substring(line.indexOf("=") + 1, line.length()));
				} 
			}
			in.close();
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		} 
		return null;
	}
	
	public Boolean hasVersionTransfered(Integer version) {
		try {
			File logFile = new File("C:\\G2 Soft\\logs\\logG2Portal.txt");
			if (!logFile.exists()) {
				return false;
			}
			in = new BufferedReader(new FileReader(logFile));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains("transferencia=") && line.contains("versao=")) {
					Integer currentVersion = 
							Integer.parseInt(line.substring(line.indexOf("versao=") + 7, line.indexOf(",")));
					if (version.equals(currentVersion)) {
						return true;
					}
				} 
			}
			in.close();
		} catch (NumberFormatException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	public Boolean isServer() {
		try {
			return FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8").contains("hostname=localhost");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	public boolean isVersionDownloadGreatherThanCurrent() {
		File fileConfig = new File("C:\\G2 Soft\\config.ini");
		BufferedReader buffer;
		String downloadedVersion = null;
		String currentVersion = null;
		try {
			buffer = new BufferedReader(new FileReader(fileConfig));
			String line;
		    while ((line = buffer.readLine()) != null) {
		    	if (line.contains("versao_baixada")) 
		    		downloadedVersion = line.substring(line.indexOf("=") + 1, line.length());
		    	if (line.contains("versao_atual_g2")) 
		    		currentVersion = line.substring(line.indexOf("=") + 1, line.length());
		    }
		    buffer.close();
		    return (downloadedVersion != null && currentVersion != null && Integer.parseInt(downloadedVersion) > Integer.parseInt(currentVersion));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void changeG2Version(Integer version, JLabel labelG2Version) {
		StringBuilder versionFormated = new StringBuilder(version.toString());
		int cont = 1;
		for (int i = 0; i < version.toString().length(); i++) {
			if (i < version.toString().length() - 1) {
				versionFormated.insert(i + cont, ".");
				cont++;
			}
		}
		labelG2Version.setText(" - " + versionFormated);
	}
	
}
