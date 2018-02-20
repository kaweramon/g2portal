package com.g2soft.g2portal.service;

import java.awt.Color;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.apache.commons.io.FileUtils;

import com.g2soft.g2portal.database.AppsBean;
import com.g2soft.g2portal.model.Apps;
import com.g2soft.g2portal.service.dropbox.DropboxDownloadPackage;

public class G2Tasks {
	
	Timer timer;
	AppsBean appsBean;
	
	public G2Tasks(JPanel rightPanel) {
		timer = new Timer(true);
		timer.schedule(new CheckServicesTask(rightPanel), 0, 10 * 1000);
	}
	
	public G2Tasks(JLabel label) {
		timer = new Timer(true);
		timer.schedule(new EnableLabelTask(label), 5 * 1000);
	}
	
	public void checkIsBilletLate(JLabel labelBillet) {
		timer = new Timer(true);
		timer.schedule(new CheckBilletIsLateTask(labelBillet), 35 * 1000);
	}
	
	public void connectToDB(G2AppsManager g2AppsManager, JLabel labelG2, 
			JLabel labelPDV, JLabel labelG2Version, JLabel labelConnectionStatus, JLabel labelTaskStatus, 
			JLabel labelBillet, JLabel labelPcType) {
		timer = new Timer(true);
		timer.scheduleAtFixedRate(new ConnectToDBTask(g2AppsManager, labelG2, labelPDV, labelG2Version, 
				labelConnectionStatus, labelTaskStatus, labelBillet, labelPcType), new Date(), 7200 * 1000);
	}
	
	public void setAppVersion(JLabel labelG2, JLabel labelPDV) {
		timer = new Timer(true);
		timer.schedule(new SetAppsVersionTask(labelG2, labelPDV), 6 * 1000);
	}
	
	public void deleteG2Update(G2AppsManager g2AppsManager) {
		timer = new Timer(true);
		timer.schedule(new DeleteG2Update(g2AppsManager), 40 * 1000);
	}
	
}

class CheckServicesTask extends TimerTask {

	G2AppsManager g2AppsManager = new G2AppsManager();
	JPanel footerPanel;
	private static final String GREEN_SIGNAL_IMG_URL = "/images/sinal_verde_15x15.png";
	private static final String RED_SIGNAL_IMG_URL = "/images/sinal_vermelho_15x15.png";
	
	public CheckServicesTask(JPanel footerPanel) {
		this.footerPanel = footerPanel;
	}
	
	@Override
	public void run() {
		Component[] components = footerPanel.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] instanceof JRadioButton) {
				JRadioButton radioBtn = (JRadioButton) components[i];
				switch (radioBtn.getName()) {
				case "radioButtonG2":
					if (g2AppsManager.isAppRunning("Project1")) {
						radioBtn.setIcon(createImageIcon(GREEN_SIGNAL_IMG_URL, "Sinal Verde"));
					} else {
						radioBtn.setIcon(createImageIcon(RED_SIGNAL_IMG_URL, "Sinal Vermelho"));
					}
					break;
				case "radioButtonPDV":
					if (g2AppsManager.isAppRunning("PDV")) 
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				case "radioButtonG2Online":
					if (g2AppsManager.isAppRunning("G2Online"))
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				case "radioButtonG2Transmissao":
					if (g2AppsManager.isAppRunning("G2Transmissao"))
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				case "radioButtonG2NfeMonitor":
					if (g2AppsManager.isAppRunning("G2NFeMonitor"))
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				case "radioButtonG2Recepcao":
					if (g2AppsManager.isAppRunning("G2Recepcao"))
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				case "radioButtonG2Update":
					if (g2AppsManager.isAppRunning("G2Update"))
						radioBtn.setIcon(createImageIcon("/images/sinal_verde_15x15.png", "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon("/images/sinal_vermelho_15x15.png", "Sinal Vermelho"));
					break;
				default:
					if (g2AppsManager.isAppRunning("Project1"))
						radioBtn.setIcon(createImageIcon(GREEN_SIGNAL_IMG_URL, "Sinal Verde"));
					else
						radioBtn.setIcon(createImageIcon(RED_SIGNAL_IMG_URL, "Sinal Vermelho"));
					break;
				}				
			}
		}
	}

	protected ImageIcon createImageIcon(String path,
            String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null)
			return new ImageIcon(imgURL, description);
		else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
}

class EnableLabelTask extends TimerTask {

	JLabel label;
	
	public EnableLabelTask(JLabel label) {
		this.label = label;
	}
	
	@Override
	public void run() {
		if (label != null) 
			label.setEnabled(true);
	}
	
}

class CheckBilletIsLateTask extends TimerTask {

	G2AppsManager g2AppsManager;
	JLabel labelBillet;
	
	public CheckBilletIsLateTask(JLabel labelBillet) {
		this.labelBillet = labelBillet;
		if (g2AppsManager == null)
			g2AppsManager = new G2AppsManager();
	}
	
	@Override
	public void run() {
		if (g2AppsManager.isClientBillsLate()) {
			System.out.println("atrasado!!!");
			labelBillet.setText("Boletos em atraso");
			labelBillet.setForeground(Color.RED);
		} else {
			System.out.println("Boleto pago!!!");
			labelBillet.setText("Boletos");
		}
	}
	
}

class ConnectToDBTask extends TimerTask {

	AppsBean appsBean;
	G2AppsManager g2AppsManager;
	JLabel labelG2;
	JLabel labelG2Version;
	JLabel labelPDV;
	JLabel labelConnectionStatus;
	JLabel labelTaskStatus;
	JLabel labelBillet;
	private boolean msgShowed = false;
	private boolean isConnect;
	private JLabel labelPcType;
	String pcType;
	Timer timer;
	
	public ConnectToDBTask(G2AppsManager g2AppsManager, 
			JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, JLabel labelConnectionStatus,
			JLabel labelTaskStatus, JLabel labelBillet, JLabel labelPcType) {
		this.appsBean = new AppsBean();
		this.g2AppsManager = g2AppsManager;
		this.labelG2 = labelG2;
		this.labelG2Version = labelG2Version;
		this.labelPDV = labelPDV;
		this.labelConnectionStatus = labelConnectionStatus;
		this.labelTaskStatus = labelTaskStatus;
		this.labelBillet = labelBillet;
		this.labelPcType = labelPcType;
		this.timer = new Timer(true);
	}
	
	@Override
	public void run() {
		System.out.println("calling task connect and verify update");
		if (g2AppsManager == null) {
			g2AppsManager = new G2AppsManager();
		}
		
		if (this.appsBean == null) {
			this.appsBean = new AppsBean();
		}
		pcType = g2AppsManager.getPcType();
		labelPcType.setText(pcType);
		System.out.println("PCTYPE: " + pcType);
		if (pcType != null && (pcType == "Retaguarda")) {
			labelG2.setEnabled(false);
        	labelPDV.setEnabled(false);
		}
		
		if (!isConnect) {
			if (!msgShowed)
				this.labelTaskStatus.setText("Conectando com o Banco de dados...");
			isConnect = appsBean.connectToDB();
		}
    	
    	if (isConnect) {    		
    		labelConnectionStatus.setText("Online");
			labelConnectionStatus.setForeground(Color.GREEN);
            this.setAppsVersions();
            labelTaskStatus.setText("Verificando se existe Atualiza\u00E7\u00E3o pendente...");
            
            if (pcType == "Retaguarda") {
            	if (!g2AppsManager.isAppRunning("Project1") && !g2AppsManager.isAppRunning("PDV")) {
                	if (g2AppsManager.rearguardOrPDVHasUpdate())
                		g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
            	}
            }
            
            if (pcType == "Servidor") {
            	if (g2AppsManager.isPendingTransfer()) {
                	if (!g2AppsManager.isAppRunning("Project1") && !g2AppsManager.isAppRunning("PDV")) {
                		g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
                		timer.cancel();
                		timer.purge();
                	}
                } else if (g2AppsManager.projectHasUpdate()) {
        			Apps appG2 = appsBean.getAppG2Server();
        			if (!isVersionDownloaded(appG2) && g2AppsManager.isServer()) {
        				DropboxDownloadPackage dbDownloadPackage = new DropboxDownloadPackage();
        				dbDownloadPackage.downloadPackage(labelTaskStatus, appG2);
        			}
        			if (g2AppsManager.isAppRunning("Project1") || g2AppsManager.isAppRunning("PDV")) {    				
        				writeUpdaterStatusOnConfig();
        				timer.schedule(new CheckProjectOrPDVToTransferFiles(g2AppsManager, 
        						labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType), 0, 10 * 1000);
        			} else {
        				g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
        				timer.cancel(); 
        				timer.purge();
        			}
        		}
            }
            
            if (pcType == "PDV") { 
            	if (g2AppsManager.isPendingTransfer()) {
            		if (!g2AppsManager.isAppRunning("Project1") && !g2AppsManager.isAppRunning("PDV"))
            			g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
            	} else if (g2AppsManager.rearguardOrPDVHasUpdate()) {
            		if (!g2AppsManager.isAppRunning("Project1") && !g2AppsManager.isAppRunning("PDV")) {
            			g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
            		} else {
            			writeUpdaterStatusOnConfig();
            			g2AppsManager.deleteFilesFolder(labelTaskStatus);
            			g2AppsManager.transferFileNetworkToLocal(labelTaskStatus);
            		}
            	}
            }
            
    		this.labelTaskStatus.setText("");
    		
    	} else {
    		labelConnectionStatus.setText("Offline");
			labelConnectionStatus.setForeground(Color.RED);
    		if (!msgShowed) {
    			this.labelTaskStatus.setText("");
    			final JOptionPane pane = new JOptionPane("N\u00E3o foi poss\u00EDvel se conectar com o servidor, "
        	    		+ "por favor entre em contato com o suporte (83) 3292-3886");
        	    final JDialog dialog = pane.createDialog((JFrame)null, "Error de Conex\u00E3o");
        	    dialog.setLocation(200 ,200);
        	    dialog.setVisible(true);
        	    msgShowed = true;
    		}
    	}
    	labelG2.setEnabled(true);
    	labelPDV.setEnabled(true);
    	
	}
	
	private boolean isVersionDownloaded(Apps appG2) {
		File fileConfig = new File("C:\\G2 Soft\\config.ini");
		try {
			String contentConfig = FileUtils.readFileToString(fileConfig, "UTF-8");
			if (contentConfig.contains("versao_baixada=" + appG2.getVersionUp())) {
				return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private void setAppsVersions() {
		for (Apps app : this.appsBean.getApps()) {
			if (app != null && app.getCurrentVersion() != null) {
				if (app.getName().equals("G2")) {
					setAppNameAndVersion(app);
				}
				if (app.getName().equals("PDV")) {
					setAppNameAndVersion(app);
				}
			}
		}
	}
	
	private void setAppNameAndVersion(Apps app) {
		StringBuilder versionFormated = null;
		
		if (app.getCurrentVersion() != null) {
			versionFormated = new StringBuilder(app.getCurrentVersion().toString());
			int cont = 1;
			for (int i = 0; i < app.getCurrentVersion().toString().length(); i++) {
				if (i < app.getCurrentVersion().toString().length() - 1) {
					versionFormated.insert(i + cont, ".");
					cont++;
				}
			}
		}
		
		if (app.getName() != null && !app.getName().isEmpty()) {
			if (app.getName().contains("G2")) {
				this.labelG2.setText(app.getName() + " Empresarial");
				if (versionFormated != null && !versionFormated.toString().isEmpty()) {
					this.labelG2Version.setText(" - " + versionFormated);
				}
				
			} else if (app.getName().contains("PDV")) {
				if (versionFormated != null && !versionFormated.toString().isEmpty()) {
					this.labelPDV.setText(app.getName() + " - " + versionFormated);
				} else {
					this.labelPDV.setText(app.getName());
				}				
			}
		}
	}
	
	private void writeUpdaterStatusOnConfig() {
		String path = "C:\\G2 Soft\\config.ini";
		File fileConfig = new File(path);
		try {
			String contentConfig = FileUtils.readFileToString(fileConfig, "UTF-8");
			BufferedWriter output = new BufferedWriter(new FileWriter(path, true));
			System.out.println("Escrevendo 'updater=s' no config");
			if (contentConfig.contains("updater=")) {
				if (contentConfig.contains("updater=n")) {
					contentConfig = contentConfig.replace("updater=n", "updater=s");
					FileUtils.write(fileConfig, contentConfig, "UTF-8");
				}					
			} else {
				output.newLine();
				output.write("updater=s");
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}

class CheckProjectOrPDVToTransferFiles extends TimerTask {

	private G2AppsManager g2AppsManager;
	JLabel labelTaskStatus; 
	JLabel labelG2; 
	JLabel labelPDV; 
	JLabel labelG2Version;
	String pcType;
	
	public CheckProjectOrPDVToTransferFiles(G2AppsManager g2AppsManager, 
			JLabel labelTaskStatus, JLabel labelG2, JLabel labelPDV, JLabel labelG2Version, String pcType) {
		this.g2AppsManager = g2AppsManager;
		this.labelTaskStatus = labelTaskStatus;
		this.labelG2 = labelG2;
		this.labelPDV = labelPDV;
		this.labelG2Version = labelG2Version;
		this.pcType = pcType;
	}
	
	@Override
	public void run() {
		System.out.println("CheckProjectOrPDVToTransferFiles");
		if (!g2AppsManager.isAppRunning("Project1") && !g2AppsManager.isAppRunning("PDV") && 
				g2AppsManager.isPendingTransfer())
			g2AppsManager.verifyUpdate(labelTaskStatus, labelG2, labelPDV, labelG2Version, pcType);
	}
	
}

class SetAppsVersionTask extends TimerTask {

	AppsBean appsBean;
	JLabel labelG2;
	JLabel labelPDV;
	
	public SetAppsVersionTask(JLabel labelG2, JLabel labelPDV) {
		this.appsBean = new AppsBean();
	}
	
	@Override
	public void run() {
		for (Apps app : this.appsBean.getApps()) {
			if (app != null && app.getCurrentVersion() != null) {
				if (app.getName() == "G2") {
					this.labelG2.setText(this.getAppNameAndVersion(app));
				}
				if (app.getName() == "PDV") {
					this.labelPDV.setText(this.getAppNameAndVersion(app));
				}
			}
		}
	}
	
	private String getAppNameAndVersion(Apps app) {
		String toReturn = "";
		StringBuilder versionFormated = null;
		
		if (app.getCurrentVersion() != null) {
			versionFormated = new StringBuilder(app.getCurrentVersion().toString());
			int cont = 1;
			for (int i = 0; i < app.getCurrentVersion().toString().length(); i++) {
				if (i < app.getCurrentVersion().toString().length() - 1) {
					versionFormated.insert(i + cont, ".");
					cont++;
				}
			}
		}
		
		if (app.getName() != null && !app.getName().isEmpty()) {
			if (app.getName().contains("G2")) {
				toReturn += app.getName() + " Empresarial";
				return toReturn;
			} else {
				toReturn += app.getName();
			}
			if (versionFormated != null && !versionFormated.toString().isEmpty()) {
				toReturn += " - " + versionFormated.toString();
			}
		}
		System.out.println(toReturn);
		return toReturn;
	}
	
}


class DeleteG2Update extends TimerTask {

	G2AppsManager g2AppsManager;
	
	public DeleteG2Update(G2AppsManager g2AppsManager) {
		this.g2AppsManager = g2AppsManager;
	}
	
	@Override
	public void run() {
		if (!g2AppsManager.isAppRunning("G2Update.exe")) {
			File fileG2Update = new File("C:\\G2 Soft\\G2Update.exe");
			if (fileG2Update.exists()) {
				System.out.println("Deletando G2Update.exe");
				fileG2Update.delete();
			}
		}
	}
	
}

