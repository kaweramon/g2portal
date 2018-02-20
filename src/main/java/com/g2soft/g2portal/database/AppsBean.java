package com.g2soft.g2portal.database;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.g2soft.g2portal.model.Apps;
import com.mysql.jdbc.DatabaseMetaData;

public class AppsBean {

	private String db_url = "";
	private String user_password = "";
	
	private static String JDBC_DRIVER = "com.mysql.jdbc.Driver";	
	private static final String USER = "root";
	private static final String SQL_SELECT_APPS = "SELECT * FROM apps";
	private static final String SQL_SELECT_APPS_BY_NAME = "SELECT * FROM apps WHERE nome = ?";
	private static final String CREATE_TABLE_APPS_IF_NOT_EXISTS = 
			"CREATE TABLE IF NOT EXISTS apps (id BIGINT(20) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			+ "data_add DATETIME, link_download VARCHAR(255), nome VARCHAR(255), versao_atual INT(11), "
			+ "versao_up INT(11), CONSTRAINT nome_unique UNIQUE (nome))";
	
	private static final String SELECT_CAD_USUARIO = "SELECT CNPJ FROM cadusuario";
	private static final String INSERT_INTO_APPS = "INSERT INTO apps "
			+ "(data_add, link_download, nome, versao_atual, versao_up) values ";
	
	private static final String UPDATE_VERSION_APPS = "UPDATE apps SET versao_atual = ";
	
	private Connection connection;
	private PreparedStatement statement;
	private ResultSet resultSet;
	
	private static final Logger logger = (Logger) LogManager.getLogger(AppsBean.class.getName());
	
	public AppsBean() {
		setHostname();
	}
	
	public boolean connectToDB() {
		try {
			Class.forName(JDBC_DRIVER);
			if (this.db_url == null || this.db_url.isEmpty()) {
				setHostname();
			}
			if (user_password.length() > 0) {	
				this.connection = DriverManager.getConnection(db_url, USER, user_password);
				this.statement = connection.prepareStatement(SQL_SELECT_APPS);	
				return true;
			}
		} catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;			
		}
		return false;
	}

	
	public void createAppsTable() {
		try {
			this.statement = connection.prepareStatement(CREATE_TABLE_APPS_IF_NOT_EXISTS);
			statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
	}
	
	public List<Apps> getApps() {
		List<Apps> listApps = new ArrayList<>();
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_APPS);
			this.resultSet = statement.executeQuery();
			
			while (this.resultSet.next()) {
				Apps apps = new Apps();
				apps.setId(resultSet.getInt("id"));
				apps.setName(resultSet.getString("nome"));
				apps.setCurrentVersion(resultSet.getInt("versao_atual"));
				apps.setVersionUp(resultSet.getInt("versao_up"));
				apps.setAddedDate(resultSet.getDate("data_add"));
				apps.setLink(resultSet.getString("link_download"));
				if (apps != null) {
					listApps.add(apps);
				}				
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return listApps;
	}
	
	public Apps getAppG2Server() {
		
		Apps appG2 = new Apps();
		
		String pathServer = "";
		String contentConfig;
		try {
			contentConfig = FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8");
			if (contentConfig != null && contentConfig.length() > 0 && contentConfig.contains("servidor=G2"))
				pathServer = "http://localhost:6464/apps/2";
			else
				pathServer = "http://177.75.66.175:6464/apps/2";
		} catch (IOException e1) {
			logger.error(e1);
			pathServer = "http://177.75.66.175:6464/apps/2";
			e1.printStackTrace();
		}
		
		try {
			URL url = new URL(pathServer);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			if (conn.getResponseCode() != 200) {
				System.out.println("Erro na conexão com o servidor g2 : " + conn.getResponseCode());
				logger.error("Erro na conexão com o servidor g2 : " + conn.getResponseCode());
				return null;
			}
			
			InputStream inputStream = conn.getInputStream();			
			JsonParser jsonParser = Json.createParser(inputStream);
			while(jsonParser.hasNext()) {
				Event e = jsonParser.next();
				if (e == Event.KEY_NAME) {
                    switch (jsonParser.getString()) {
                        case "id":
                        	jsonParser.next();
                        	appG2.setId(jsonParser.getInt());
                            break;
                        case "versao_atual":
                        	jsonParser.next();
                        	appG2.setCurrentVersion(jsonParser.getInt());
                            break;
                        case "versao_up":
                        	jsonParser.next();
                        	appG2.setVersionUp(jsonParser.getInt());
                            break;
                        case "link_download":
                        	jsonParser.next();
                        	appG2.setLink(jsonParser.getString());
                        	break;
                    }
                }
			}
			
			conn.disconnect();
		}  catch (IOException e) {
			System.out.println("Erro na conexão com o servidor g2 : " + e.getMessage());
			logger.error("Erro na conexão com o servidor g2 : " +  e.getMessage());
		}	
		return appG2;
	}
	
	public Apps getAppByName(String appName) {
		Apps projectApp = new Apps();
		
		try {
			if (connection == null) {
				connectToDB();
			} 
			this.statement = connection.prepareStatement(SQL_SELECT_APPS_BY_NAME);
			this.statement.setString(1, appName);
			this.resultSet = statement.executeQuery();
			
			if (this.resultSet.next()) {
				projectApp.setId(resultSet.getInt("id"));
				projectApp.setName(resultSet.getString("nome"));
				projectApp.setCurrentVersion(resultSet.getInt("versao_atual"));
				projectApp.setVersionUp(resultSet.getInt("versao_up"));
				projectApp.setAddedDate(resultSet.getDate("data_add"));
				projectApp.setLink(resultSet.getString("link_download"));
			}
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
		
		return projectApp;
	}
	
	public void insertValuesApps(String link_download, String nome, Integer versao_atual, Integer versao_up) {
		try {
			String sql = INSERT_INTO_APPS;
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sql += "(" + "'" + df.format(new Date()) + "'" + ", '" + link_download + "'" + ", '" + nome + "', " + versao_atual + ", " + versao_up + ")";
			if (connection == null) 
				connectToDB();
			this.statement = connection.prepareStatement(sql);
			this.statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public Boolean hasTable(String tableName) {
		DatabaseMetaData dbm;
		Boolean toReturn = false;
		try {
			dbm = (DatabaseMetaData) connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableName, null);
			toReturn = tables.next();
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}
		return toReturn;
	}
	
	public void updateAppVersion(String appName, Integer appVersion) {
		if (appName == null || appVersion == null) {
			return;
		}
		String sql = UPDATE_VERSION_APPS  + appVersion + " WHERE nome = '" + appName + "'";
		try {
			if (connection == null) {
				connectToDB();
			}
			this.statement = this.connection.prepareStatement(sql);
			this.statement.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage());
		}
	}
	
	public void setHostname() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String content = FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8");
			if (content.contains("especial=G2")) {
				user_password = "paulo85";
			} else {
				user_password = "Shispirito85";
			}
			String line;
			while((line = in.readLine()) != null) {
			    if (line.contains("hostname")) {
			    	this.db_url = "jdbc:mysql://" + line.substring(line.indexOf("=") + 1, line.length()) + ":3306/bancr?connectTimeout=10000";
			    	break;
			    }
			}
			in.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getClientCnpj() {
		
		String clientCnpj = "";
		
		if (connection == null) {
			connectToDB();
		}
		
		try {
			this.statement = connection.prepareStatement(SELECT_CAD_USUARIO);
			this.resultSet = this.statement.executeQuery();
			
			if (this.resultSet.next()) {
				clientCnpj = this.resultSet.getString("CNPJ");
			} 
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return clientCnpj;
	}
}
