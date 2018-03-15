package com.g2soft.g2portal.ui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.g2soft.g2portal.model.Apps;
import com.g2soft.g2portal.service.DeleteTempFilesTask;
import com.g2soft.g2portal.service.G2AppsManager;
import com.g2soft.g2portal.service.G2Tasks;
import com.g2soft.g2portal.service.UploadNfeTask;

public class MainMenu implements MouseListener {

	private static Integer IMG_WIDTH = 64; 
	private static Integer IMG_HEIGH = 64;
	
	private JLabel labelG2;
	private JLabel labelG2Version;
	private JLabel labelPDV;
	private JLabel labelBillet;
	private JLabel labelGraphics;
	private JLabel labelTef;
	private JLabel labelSuport;
	private JLabel labelConnectionStatus;
	private JRadioButton radioButtonG2;
	private JRadioButton radioButtonPDV;	
	private JPanel rightPanel;
	private JPanel footerPanel;
	private JFrame mainJFrame;
	private List<Apps> apps;
	private G2Tasks g2Tasks;
	private G2AppsManager g2AppsManager;
	private JLabel labelTaskStatus;
	private JLabel labelStatus;
	private JLabel labelPcType;
	
	public MainMenu() {
		g2AppsManager = new G2AppsManager();
	}
	
	public MainMenu(List<Apps> apps) {
		this.apps = apps;
		g2AppsManager = new G2AppsManager();
	}
	
	public void drawMainMenu() {
		this.mainJFrame = new JFrame("Portal G2");
		this.mainJFrame.setResizable(false);
		this.mainJFrame.setBackground(Color.WHITE);
		this.mainJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		labelConnectionStatus = new JLabel();
		List<Image> icons = new ArrayList<Image>();
		icons.add(createImage("/images/iconeG2_18x18.jpg"));
		icons.add(createImage("/images/iconeG2_40x40.jpg"));
		mainJFrame.setIconImages(icons);
		createHeaderPane();
		createLeftPanel();
		createRightPanel();
		createFooterPanel();		
		mainJFrame.setSize(800,  500);
		mainJFrame.setLayout(null);
		mainJFrame.setVisible(true);
		g2Tasks = new G2Tasks(footerPanel);
		g2Tasks.connectToDB(g2AppsManager, labelG2, labelPDV, labelG2Version, 
				labelConnectionStatus, labelTaskStatus, labelBillet, labelPcType);
		new DeleteTempFilesTask(labelTaskStatus, g2AppsManager);
		g2Tasks.checkIsBilletLate(labelBillet);
		g2Tasks.deleteG2Update(g2AppsManager);
		if (!g2AppsManager.isServerG2())
			g2Tasks.updateLiberation(labelTaskStatus);
		UploadNfeTask uploadNfeTask = new UploadNfeTask(labelTaskStatus);
		uploadNfeTask.uploadNfeXmls();
//		uploadNfeTask.uploadNFCeXmls();
	}
	
	private void createHeaderPane() {
		ImageIcon imgPanelG2 = createImageIcon("/images/painel_g2_765x130.jpg", "panel g2");
		JLabel imgJLabel = new JLabel(imgPanelG2);
		JPanel headerPanel = new JPanel(); // Panel G2 Info
		headerPanel.setVisible(true);
		headerPanel.setBounds(10, 0, 765, 130);
		headerPanel.add(imgJLabel);
		headerPanel.setBackground(Color.WHITE);		
		Container mainContainer = mainJFrame.getContentPane();		
		mainContainer.setBackground(Color.WHITE);
		mainContainer.add(headerPanel, BorderLayout.PAGE_START);
	}
	
	// Panel Apps
	private void createLeftPanel() {
		Image imgG2 = createImage("/images/iconeG2_64x64.jpg");
		ImageIcon iconG2 = new ImageIcon(imgG2.getScaledInstance(IMG_WIDTH, IMG_HEIGH, java.awt.Image.SCALE_DEFAULT));
		labelG2 = new JLabel(iconG2, JLabel.LEFT);
		labelG2.addMouseListener((MouseListener) this);
		labelG2.setVerticalTextPosition(JLabel.BOTTOM);
		labelG2.setHorizontalTextPosition(JLabel.CENTER);
		labelG2.setBounds(15, 10, 140, 94);
		
		labelG2Version = new JLabel();
		labelG2Version.setBounds(35, 104, 60, 10);
		
		Image imgG2PDV = createImage("/images/G2PDV_64x64.jpg");
		ImageIcon iconG2PDV = new ImageIcon(imgG2PDV.getScaledInstance(IMG_WIDTH, IMG_HEIGH, java.awt.Image.SCALE_SMOOTH));
		labelPDV = new JLabel(iconG2PDV, JLabel.CENTER);
		labelPDV.addMouseListener((MouseListener) this);
		labelPDV.setVerticalTextPosition(JLabel.BOTTOM);
		labelPDV.setHorizontalTextPosition(JLabel.CENTER);
		labelPDV.setBounds(140, 10, 75, 94);
		
		Image imgGraphics = createImage("/images/graficos_58x55.png");
		ImageIcon iconGraphics = new ImageIcon(imgGraphics.getScaledInstance(IMG_WIDTH, IMG_HEIGH, java.awt.Image.SCALE_SMOOTH));
		labelGraphics = new JLabel(iconGraphics);
		labelGraphics.addMouseListener((MouseListener) this);
		labelGraphics.setHorizontalTextPosition(JLabel.CENTER);
		labelGraphics.setVerticalTextPosition(JLabel.BOTTOM);
		labelGraphics.setText("Gr" + "\u00E1" + "ficos");
		labelGraphics.setBounds(260, 10, 64, 94);
		
		Image imgTef = createImage("/images/tef_64x85.png");
		ImageIcon iconTef = new ImageIcon(imgTef.getScaledInstance(85, IMG_HEIGH, java.awt.Image.SCALE_SMOOTH));
		labelTef = new JLabel(iconTef);
		labelTef.addMouseListener((MouseListener) this);
		labelTef.setHorizontalTextPosition(JLabel.CENTER);
		labelTef.setVerticalTextPosition(JLabel.BOTTOM);
		labelTef.setText("TEF");
		labelTef.setBounds(20, 114, 85, 94);
		
		Image imgSuport = createImage("/images/suporte_95x64.jpg");
		ImageIcon iconSuport = new ImageIcon(imgSuport.getScaledInstance(85, IMG_HEIGH, java.awt.Image.SCALE_SMOOTH));
		labelSuport = new JLabel(iconSuport);
		labelSuport.addMouseListener((MouseListener) this);
		labelSuport.setHorizontalTextPosition(JLabel.CENTER);
		labelSuport.setVerticalTextPosition(JLabel.BOTTOM);
		labelSuport.setText("Suporte");
		labelSuport.setBounds(125, 114, 95, 94);

		Image imgBillet = createImage("/images/boleto-facil-1.jpg");
		ImageIcon iconBillet = new ImageIcon(imgBillet.getScaledInstance(IMG_WIDTH, IMG_HEIGH, java.awt.Image.SCALE_SMOOTH));
		labelBillet = new JLabel(iconBillet);
		labelBillet.addMouseListener((MouseListener) this);
		labelBillet.setHorizontalTextPosition(JLabel.CENTER);
		labelBillet.setVerticalTextPosition(JLabel.BOTTOM);
		labelBillet.setText("Boletos");
		labelBillet.setBounds(240, 114, 110, 94);
		
		JPanel leftJPanel = new JPanel();
		leftJPanel.setLayout(null);
		leftJPanel.setBounds(10, 135, 370, 270);
		leftJPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Adding Labels
		leftJPanel.add(labelG2, JPanel.LEFT_ALIGNMENT);
		leftJPanel.add(labelG2Version, JPanel.LEFT_ALIGNMENT);
		leftJPanel.add(labelPDV, JPanel.LEFT_ALIGNMENT);		
		leftJPanel.add(labelGraphics, JPanel.CENTER_ALIGNMENT);
		leftJPanel.add(labelTef, JPanel.CENTER_ALIGNMENT);
		leftJPanel.add(labelSuport, JPanel.CENTER_ALIGNMENT);
		leftJPanel.add(labelBillet, JPanel.CENTER_ALIGNMENT);
		leftJPanel.setBackground(Color.WHITE);
		Container mainContainer = mainJFrame.getContentPane();
		mainContainer.add(leftJPanel, BorderLayout.LINE_START);
	}
	
	// Panel Services
	private void createRightPanel() {
		rightPanel = new JPanel();
		rightPanel.setBounds(395, 135, 380, 270);
		rightPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		rightPanel.setName("rightPanel");
		rightPanel.setBackground(Color.WHITE);
		ImageIcon imgDigitalCertificate = createImageIcon("/images/banner-portal.png", "Banner Portal Certificado");
		JLabel imgJLabel = new JLabel(imgDigitalCertificate);
		rightPanel.add(imgJLabel);
		Container mainContainer = mainJFrame.getContentPane();
		mainContainer.add(rightPanel, BorderLayout.LINE_END);
	}
	/***
	 * Painel de Serviços
	 */
	private void createFooterPanel() {
		footerPanel = new JPanel();
		footerPanel.setBounds(10, 410, 765, 60);
		footerPanel.setBorder(BorderFactory.createLineBorder(Color.black));
		footerPanel.setLayout(null);

		JLabel labelServices = new JLabel("Servi\u00E7os:");		
		Insets insets = footerPanel.getInsets();
        Dimension size = labelServices.getPreferredSize();
		labelServices.setFont(new Font("Serif", Font.BOLD, 20));
		labelServices.setBounds(10 + insets.left, 13 + insets.top,
                80, size.height + 5);
		
		radioButtonG2 = new JRadioButton("G2 Empresarial");
		radioButtonG2.setName("radioButtonG2");
		radioButtonG2.setBounds(100 + insets.left, 3 + insets.top, 115, size.height + 10);
		
		radioButtonPDV = new JRadioButton("PDV");
		radioButtonPDV.setName("radioButtonPDV");
		radioButtonPDV.setBounds(210 + insets.left, 3 + insets.top, 55, size.height + 10);
		
		JRadioButton radioButtonG2Online = new JRadioButton("G2 Online");
		radioButtonG2Online.setName("radioButtonG2Online");
		radioButtonG2Online.setBounds(260 + insets.left, 3 + insets.top, 85, size.height + 10);
		
		JRadioButton radioButtonG2Transmissao = new JRadioButton("G2 Transmiss\u00E3o");
		radioButtonG2Transmissao.setName("radioButtonG2Transmissao");
		radioButtonG2Transmissao.setBounds(340 + insets.left, 3 + insets.top, 120, 
				size.height + 10);
		
		JRadioButton radioButtonG2NfeMonitor = new JRadioButton("G2 Nfe Monitor");
		radioButtonG2NfeMonitor.setName("radioButtonG2NfeMonitor");
		radioButtonG2NfeMonitor.setBounds(460 + insets.left, 3 + insets.top, 110, 
				size.height + 10);
		
		// Segunda Linha de Serviços
		JRadioButton radioButtonG2Recepcao = new JRadioButton("G2 Recep\u00E7\u00E3o");
		radioButtonG2Recepcao.setName("radioButtonG2Recepcao");
		radioButtonG2Recepcao.setBounds(100 + insets.left, 23 + insets.top, 110, 
				size.height + 9);
		
		JRadioButton radioButtonG2Update = new JRadioButton("G2 Update");
		radioButtonG2Update.setName("radioButtonG2Update");
		radioButtonG2Update.setBounds(210 + insets.left, 23 + insets.top, 110, 
				size.height + 9);
		
		JLabel labelPortalVersion = new JLabel("Vers\u00E3o 0.1.4");
		labelPortalVersion.setBounds(685 + insets.left, 35 + insets.top, 100, size.height + 10);
		
		labelConnectionStatus.setBounds(685 + insets.left, 15 + insets.top, 90, size.height + 10);
		
		if (apps != null && apps.size() > 0) {
			labelConnectionStatus.setText("Online");
			labelConnectionStatus.setForeground(Color.GREEN);
		} else {
			labelConnectionStatus.setText("Offline");
			labelConnectionStatus.setForeground(Color.RED);
		}
		
		labelStatus = new JLabel("Status: ");
		labelStatus.setBounds(10 + insets.left, 39 + insets.top, 60, size.height + 10);
		labelTaskStatus = new JLabel("");
		labelTaskStatus.setBounds(70 + insets.left, 39 + insets.top, 400, size.height + 10);
		
		labelPcType = new JLabel("");
		labelPcType.setBounds(685 + insets.left, 5, 70, 13);
		
		footerPanel.add(labelServices, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonPDV, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2Online, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2Transmissao, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2NfeMonitor, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2Recepcao, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(radioButtonG2Update, JPanel.LEFT_ALIGNMENT);
		footerPanel.add(labelPortalVersion, JPanel.RIGHT_ALIGNMENT);		
		footerPanel.add(labelConnectionStatus);
		footerPanel.add(labelStatus);
		footerPanel.add(labelTaskStatus);
		footerPanel.add(labelPcType);
		Container mainContainer = mainJFrame.getContentPane();
		footerPanel.setBackground(Color.WHITE);
		mainContainer.add(footerPanel, BorderLayout.SOUTH);
	}
	
	protected ImageIcon createImageIcon(String path,
            String description) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
	
	protected Image createImage(String path) {
		java.net.URL imgURL = getClass().getResource(path);
		if (imgURL != null) {
			try {
				return ImageIO.read(imgURL);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
		return null;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() > 1) {
			return;
		}
		
		JLabel labelClicked = (JLabel) e.getSource();
		labelClicked.setEnabled(false);
		
		new G2Tasks(labelClicked);
		 if(e.getSource() == this.labelG2) {
			 g2AppsManager.openApp("Project1");
        } else if (e.getSource() == this.labelPDV) {
        	try {
				java.awt.Desktop.getDesktop().open(new File("C:/G2 Soft/G2PDV.exe"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
        } else if (e.getSource() == this.labelBillet)  {
        	try {
				Desktop.getDesktop().browse(new URI("http://177.75.66.175:6464/"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
        } else if (e.getSource() == this.labelGraphics) {
        	try {
				Desktop.getDesktop().browse(new URI("http://g2soft.com.br/graficos/"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
        } else if (e.getSource() == this.labelSuport) {
        	try {
				Desktop.getDesktop().browse(new URI("https://sec.beanywhere.com/start/base/webchat/"
						+ "webchat.php?duuid=ZisE0Veb61a6Qqjnv0yTYNQ3ifaJQNm7aY6ILCEMVLiPdOzsDQL9%2"
						+ "B2UHxWkqqCIiIPMaznU3PCg%3D&l=pt"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
        } else if (e.getSource() == this.labelTef) {
        	try {
				Desktop.getDesktop().browse(new URI("http://scopeweb.getcard.com.br:8080/scopeweb/index.do;"
						+ "jsessionid=606D087304F711C94369D78A56AE6031"));
			} catch (IOException | URISyntaxException e1) {
				e1.printStackTrace();
			}
        }
	}
	
	public void checkClientBillsStatus() {
		g2AppsManager.isClientBillsLate();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	} 
	
}
