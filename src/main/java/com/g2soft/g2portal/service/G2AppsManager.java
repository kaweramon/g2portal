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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

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
	Calendar calendar;
	SimpleDateFormat simpleDateFormat;
	G2Tasks g2Tasks;
	private boolean msgNetworkErrorConfigShowed = false;
	
	private List<String> listApps;
	
	public G2AppsManager() {
		listApps = Arrays.asList("G2Update", "G2Online", "G2NFeMonitor", "G2Transmissao");
		this.appsBean = new AppsBean();
		this.appsBean.setHostname();
		setIp();
		this.appG2FromServer = appsBean.getAppG2Server();
		this.calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT-3"));
		this.simpleDateFormat = new SimpleDateFormat("MMMM");
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
			return pidInfo.contains(g2ProcessName);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	public void verifyUpdate(JLabel labelTaskStatus, JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, 
			String pcType) {
		System.out.println("Verificando Atualização");
		labelTaskStatus.setText("Verificando Atualiza\u00E7\u00E3o");
		String g2TempNameAndVersion = labelG2.getText();
		String pdvTempNamAndVersion = labelPDV.getText();
		File filePathG2 = null;
		if (ip.contains("localhost") || isPendingTransfer()) {
			filePathG2 = new File("C:\\G2 Soft\\Updater\\Files");	
		} else {
			filePathG2 = new File("\\\\" + ip.replace(" ", "") + "\\G2 Soft\\Updater\\Files");
		}
		System.out.println("Caminho G2: " + filePathG2.toString());
		
		if (!filePathG2.exists())
			filePathG2.mkdirs();
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
						labelTaskStatus.setText("");
		    			final JOptionPane pane = new JOptionPane("N\u00E3o foi poss\u00EDvel transferir o arquivo "
		        	    		+ file.getName() + "\n Motivo: " + e.getMessage());
		        	    final JDialog dialog = pane.createDialog((JFrame)null, "Erro ao transferir");
		        	    dialog.setLocation(200 ,200);
		        	    dialog.setVisible(true);
		        	    return;
					}
				}
				writeChangesAfterUpdate(labelG2, labelPDV, labelG2Version, pcType, g2TempNameAndVersion,
						pdvTempNamAndVersion);
			} else {
				System.out.println("Diretório Vazio");
				logger.error("Diretório Vazio, não existe ou sem permissão");
				writeUpdaterStatusOkOnConfig();
			}
		} else {
			System.out.println("diretorio nao existe");
			logger.error("Diretório Vazio, não existe ou sem permissão");
		}
	}

	public void writeChangesAfterUpdate(JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, String pcType,
			String g2TempNameAndVersion, String pdvTempNamAndVersion) {
		writeCurrentVersionOnConfig(appG2FromServer.getVersionUp());
		writeTransferedVersion(appG2FromServer.getVersionUp());
		changeG2Version(appG2FromServer.getVersionUp(), labelG2Version);
		if (pcType == "Servidor" || pcType == "PDV")
			writeUpdaterStatusOkOnConfig();
		labelG2.setText(g2TempNameAndVersion);
		labelG2.setForeground(Color.BLACK);
		labelG2.setEnabled(true);
		labelPDV.setText(pdvTempNamAndVersion);
		labelPDV.setForeground(Color.BLACK);
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
	
	public Boolean projectHasUpdate() {
		logger.info("Verificando atualização");
		if (appG2FromServer == null) 
			appG2FromServer = this.appsBean.getAppG2Server();
		
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
					// Versão do servidor é maior do que config
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
	
	public boolean rearguardOrPDVHasUpdate() {
		logger.info("Verificando atualização no retaguarda");
		Integer currentVersionFromConfig = getCurrentVersionFromConfig();
		Apps g2App = this.appsBean.getAppByName("G2");
		if (currentVersionFromConfig != null)
			return g2App.getCurrentVersion() > currentVersionFromConfig;
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
				System.out.println("Erro na conexão com o servidor g2: " + e.getMessage());
				logger.error("Erro na conexão com o servidor g2: " + e.getMessage());
			} catch (IOException e) {
				System.out.println("Erro na conexão com o servidor g2: " + e.getMessage());
				logger.error("Erro na conexão com o servidor g2: " + e.getMessage());
			} finally {
				try {
					if (response != null) 
						response.close();
				} catch (IOException e) {
					System.out.println("Erro na conexão com o servidor g2: " + e.getMessage());
					logger.error("Erro na conexão com o servidor g2: " + e.getMessage());
				}
			}
		}
		return false;
	}
	
	public void writeCurrentVersionOnConfig(Integer appVersion) {
		if (appVersion == null)
			return;
		try {
			in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String path = "C:\\G2 Soft\\config.ini";
		try {
			String contentConfig = FileUtils.readFileToString(new File(path), "UTF-8");
			BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
			logger.info("Escrevendo versão transferida no config: " + appVersion);
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
				logger.info("Escrevendo versão transferida: " + appVersion);
				output.write("transferencia=(versao=" + appVersion + ", data=" + strDateNow + ")");
				output.flush();
				output.close();			
			}			
		} catch (IOException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
	}
	
	public void writeUpdaterStatusOkOnConfig() {
		String path = "C:\\G2 Soft\\config.ini";
		try {
			String contentConfig = FileUtils.readFileToString(new File(path), "UTF-8");
			BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
			if (contentConfig.contains("updater=")) {
				if (contentConfig.contains("updater=s")) {
					contentConfig = contentConfig.replace("updater=s", "updater=n");
					System.out.println("Atualizado updater para n");
					logger.info("Atualizado updater para n");
					FileUtils.write(new File(path), contentConfig, "UTF-8");
				}
			} else {
				output.newLine();
				output.write("updater=n");
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
	}
	
	public Integer getCurrentVersionFromConfig() {
		try {
			in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String line;
			while ((line = in.readLine()) != null) {				
				if (line.contains("versao_atual_g2"))
					return Integer.parseInt(line.substring(line.indexOf("=") + 1, line.length()));
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
			if (!logFile.exists())
				return false;
			in = new BufferedReader(new FileReader(logFile));
			String line;
			while ((line = in.readLine()) != null) {
				if (line.contains("transferencia=") && line.contains("versao=")) {
					Integer currentVersion = 
							Integer.parseInt(line.substring(line.indexOf("versao=") + 7, line.indexOf(",")));
					return version.equals(currentVersion);
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
	
	public Boolean isServerG2() {
		try {
			return FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8").contains("servidor=G2");
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return false;
	}
	
	public Boolean isSpecialG2() {
		try {
			return FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8").contains("especial=G2");
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
			logger.error(e);
		}
		return false;
	}
	
	private void changeG2Version(Integer version, JLabel labelG2Version) {
		if (version == null)
			return;
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
	
	public String getPcType() {
		try {
			String configContent = FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8");
			
			if (configContent.contains("hostname=localhost"))
				return "Servidor";
			
			if (!configContent.contains("hostname=localhost") && !configContent.contains("hostnamepdv=localhost"))
				return "Retaguarda";
			
			if (!configContent.contains("hostname=localhost") && configContent.contains("hostnamepdv=localhost"))
				return "PDV";
			
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return null;
	}
	
	public boolean isPendingTransfer() {
		try {
			String contentConfig = FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8");
			return contentConfig.contains("updater=s");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return false;
	}
	
	public void deleteFilesFolder(JLabel labelTaskStatus) {
		try {
			System.out.println("Apagando Arquivos da Pasta Files");
			logger.info("Apagando Arquivos da Pasta Files");
			labelTaskStatus.setText("Apagando Arquivos da Pasta Files");
			File filesFolder = new File("C:\\G2 Soft\\Updater\\Files");
			if (FileUtils.waitFor(filesFolder, 2)) 
				FileUtils.cleanDirectory(filesFolder);
		} catch (IOException e) {
			logger.error("Error ao apagar arquivos da pasta Files: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void transferPackageNetworkToLocalFileFolder(JLabel labelTaskStatus) {
		String pathNetwork = "\\\\" + ip.replace(" ", "") + "\\G2 Soft\\Updater\\Files";
		String pathLocal = "C:\\G2 Soft\\Updater";
		File filePathNetwork = new File(pathNetwork);
		File filePathLocal = new File(pathLocal);
		
		if (!filePathLocal.exists())
			filePathLocal.mkdirs();
		
		try {
			labelTaskStatus.setText("Copiando Pasta Files pela rede, ip: " + ip);
			FileUtils.copyDirectoryToDirectory(filePathNetwork, filePathLocal);
		} catch (IOException e) {
			logger.error("Erro ao transferir Pasta Files pela rede: " + e.getMessage());
		}
	}
	
	public void transferFileNetworkPdvAndRetaguarda(JLabel labelTaskStatus, 
			JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, String pcType,
			String g2TempNameAndVersion, String pdvTempNamAndVersion) {
		File filePathNetwork = new File("\\\\" + ip.replace(" ", "") + "\\G2 Soft\\Updater\\Files");
		File filePathLocal = new File("C:\\G2 Soft");
		
		if (!filePathLocal.exists())
			filePathLocal.mkdirs();
		
		try {
			labelTaskStatus.setText("Copiando Arquivos pela rede, ip: " + ip);
			FileUtils.copyDirectoryToDirectory(filePathNetwork, filePathLocal);
			writeChangesAfterUpdate(labelG2, labelPDV, labelG2Version, pcType, 
					g2TempNameAndVersion, pdvTempNamAndVersion);
		} catch (IOException e) {
			logger.error("Erro ao transferir Pasta Files pela rede: " + e.getMessage());
		}
	}
	
	public String getPathDate(Date dateToConvert) {
		calendar.setTimeInMillis(dateToConvert.getTime());
		return calendar.get(Calendar.YEAR) + "/"  + this.simpleDateFormat.format(calendar.getTime());
	}
	
	public void deleteG2OldFiles() {
		
		File g2OnlineFile = new File("C:\\G2 Soft\\G2Online.exe");
		if (g2OnlineFile.exists()) {
			if (isAppRunning("G2Online"))
				closeApp("G2Online");
			g2OnlineFile.delete();
		}
			
		File g2UpdateFile = new File("C:\\G2 Soft\\G2Update.exe");
		if (g2UpdateFile.exists()) {
			if (isAppRunning("G2Update"))
				closeApp("G2Update");
			g2UpdateFile.delete();
		}
			
		File g2Gerenciador = new File("C:\\G2 Soft\\G2Gerenciador.exe");
		if (g2Gerenciador.exists()) {
			if (isAppRunning("G2Gerenciador"))
				closeApp("G2Gerenciador");
			g2Gerenciador.delete();
		}
		
		File g2ChamadoFile = new File("C:\\G2 Soft\\G2Chamado.exe");
		if (g2ChamadoFile.exists()) {
			if (isAppRunning("G2Chamado"))
				closeApp("G2Chamado");
			g2ChamadoFile.delete();
		}
			
		File g2AtualizaFile = new File("C:\\G2 Soft\\G2Atualiza.exe");
		if (g2AtualizaFile.exists()) {
			if (isAppRunning("G2Atualiza"))
				closeApp("G2Atualiza");
			g2AtualizaFile.delete();
		}
	}
	
	public boolean pdvHasUpdate(G2AppsManager g2AppsManager, JLabel labelTaskStatus, 
			JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, String pcType,
			String g2TempNameAndVersion, String pdvTempNamAndVersion) {
		Integer currentConfigVersion = getCurrentVersionFromConfig();
		Integer currentVersionServerConfig = null;
		try {
			in = new BufferedReader(new FileReader("\\\\" + ip.replace(" ", "") + "\\G2 Soft\\config.ini"));
			String line;
			while ((line = in.readLine()) != null) {				
				if (line.contains("versao_atual_g2")) {
					currentVersionServerConfig = Integer.parseInt(line.substring(line.indexOf("=") + 1, line.length()));
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			logger.error(e);
			showMsgErrorAndCallTaskUpdatePdv(e, g2AppsManager, labelTaskStatus, 
					labelG2, labelPDV, labelG2Version, pcType, 
					labelG2.getText(), labelPDV.getText());			
		} catch (IOException e) {
			logger.error(e);
			showMsgErrorAndCallTaskUpdatePdv(e, g2AppsManager, labelTaskStatus, 
					labelG2, labelPDV, labelG2Version, pcType, 
					labelG2.getText(), labelPDV.getText());
		}

		if (currentConfigVersion != null && currentVersionServerConfig != null) {
			return currentVersionServerConfig > currentConfigVersion;
		}
		return false;
	}
	
	private void showMsgErrorAndCallTaskUpdatePdv(Exception e, G2AppsManager g2AppsManager,
			JLabel labelTaskStatus, 
			JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, String pcType,
			String g2TempNameAndVersion, String pdvTempNamAndVersion) {
		if (!this.msgNetworkErrorConfigShowed) {
			final JOptionPane pane = new JOptionPane("N\u00E3o foi poss\u00EDvel ler o arquivo de "
					+ "configura\u00E7\u00E3o do servidor pela rede, "
		    		+ "por favor entre em contato com o suporte (83) 3292-3886 \n"
					+ "Motivo: " + e.getMessage());
		    final JDialog dialog = pane.createDialog((JFrame)null, "Error de Conex\u00E3o");
		    dialog.setLocation(200 ,200);
		    dialog.setVisible(true);
		    this.msgNetworkErrorConfigShowed = true;
		}
		
	    if (this.g2Tasks == null)
	    	this.g2Tasks = new G2Tasks();
	    this.g2Tasks.updatePdvAfterNetworkError(g2AppsManager, labelG2, labelPDV, labelG2Version, 
	    		labelTaskStatus);
	}
	
}
