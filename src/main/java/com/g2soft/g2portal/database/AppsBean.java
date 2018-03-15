package com.g2soft.g2portal.database;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.g2soft.g2portal.model.Apps;
import com.g2soft.g2portal.model.Liberation;
import com.g2soft.g2portal.model.QuickSell;
import com.g2soft.g2portal.model.SightSale;
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
	
	private static final String SQL_SELECT_COUNT_LIBERATION = "SELECT COUNT(Codigo) as count FROM liberacao l;";
	private static final String SQL_SELECT_LAST_REGISTER_LIBERATION = "SELECT MAX(Codigo) as Codigo FROM liberacao l;";
	private static final String SQL_DELETE_LIBERATION = "DELETE FROM liberacao WHERE Codigo != ?";
	private static final String UPDATE_LIBERATION = "UPDATE liberacao SET Liberacao_sistema = ?, Operador = ?, Liberacao_temp = ?,"
			+ "Obs = ? ";
	
	private Connection connection;
	private Connection connectionG2Mensagem;
	private PreparedStatement statement;
	private ResultSet resultSet;
	
	private static final String SQL_SELECT_SIGHT_SALE_NFE_NOT_UPLOADED = 
			"SELECT Codigo, Dt_Venda, Chave_NFE, Envio_online, Envio_online_dt_hr, SincNuvem, Status, Situacao_NFE FROM bancr.vendaavista WHERE Status = 'Concluido' AND Situacao_NFE = 'Enviada' AND "
			+ "(Envio_online = 'Nao Enviada' OR Envio_online IS NULL);";
	
	private static final String SQL_UPDATE_SIGHT_SALE_UPLOADED_OK = "UPDATE bancr.vendaavista SET Envio_online = ?, Envio_online_dt_hr = ?, sincNuvem = ? WHERE codigo = ?";
	
	private static final String SQL_SELECT_SIGHT_SALE_CANCELED = "SELECT Codigo, Dt_Venda, Chave_NFE, Envio_online, Envio_online_dt_hr, SincNuvem, Status, Situacao_NFE FROM bancr.vendaavista WHERE Situacao_NFE = 'Cancelada';";
	
	private static final String SQL_SELECT_QUICK_SELL_NFE_NOT_UPLOADED = "SELECT codigo, Dt_venda, Cancelado, status, NFCe_Chave, NFCe_Protocolo, Envio_online, Envio_online_dt_hr "
			+ "FROM bancr.vendarapida WHERE status = 'Autorizado o uso da NF-e' AND NFCe_Protocolo IS NOT NULL AND Cancelado = 0\r\n" + 
			"AND (Envio_online = 'Nao Enviada' OR Envio_online IS NULL);";
	
	private static final String SQL_UPDATE_QUICK_SELL_UPLOADED_OK = "UPDATE bancr.vendarapida SET Envio_online = ?, Envio_online_dt_hr = ?, sincNuvem = ? WHERE codigo = ?";
	
	private static final String SQL_SELECT_QUICK_SELL_CANCELED = "SELECT codigo, Dt_venda, Cancelado, status, NFCe_Chave, NFCe_Protocolo, Envio_online, Envio_online_dt_hr FROM bancr.vendarapida WHERE Cancelado IS TRUE;";
	
	private static final String SQL_SELECT_PATH_BACKUP_CONFIG = "SELECT Config_Destino_Backup FROM bancr.configuracoes;";

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

	public boolean connectToDBG2Mensagem() {
		try {
			Class.forName(JDBC_DRIVER);
			if (this.db_url == null || this.db_url.isEmpty()) {
				setHostname();
			}
			if (user_password.length() > 0) {	
				this.connectionG2Mensagem = DriverManager.getConnection(getHostNameG2Mensagem(), USER, user_password);
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
				if (apps != null)
					listApps.add(apps);
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
			if (connection == null)
				connectToDB();
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
		if (appName == null || appVersion == null)
			return;
		String sql = UPDATE_VERSION_APPS  + appVersion + " WHERE nome = '" + appName + "'";
		try {
			if (connection == null)
				connectToDB();
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
			if (content.contains("especial=G2"))
				user_password = "paulo85";
			else 
				user_password = "Shispirito85";
			String line;
			while((line = in.readLine()) != null) {
			    if (line.contains("hostname")) {
			    	this.db_url = "jdbc:mysql://" + line.substring(line.indexOf("=") + 1, line.length()) 
			    		+ ":3306/bancr?connectTimeout=10000";
			    	break;
			    }
			}
			in.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getHostNameG2Mensagem() {
		String hostname = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader("C:\\G2 Soft\\config.ini"));
			String content = FileUtils.readFileToString(new File("C:\\G2 Soft\\config.ini"), "UTF-8");
			if (content.contains("especial=G2"))
				user_password = "paulo85";
			else 
				user_password = "Shispirito85";
			String line;
			while((line = in.readLine()) != null) {
			    if (line.contains("hostname")) {
			    	hostname = "jdbc:mysql://" + line.substring(line.indexOf("=") + 1, line.length()) 
			    		+ ":3306/g2mensagem?connectTimeout=10000";
			    	break;
			    }
			}
			in.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return hostname;
	}
	
	public String getClientCnpj() {
		
		String clientCnpj = "";
		
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SELECT_CAD_USUARIO);
			this.resultSet = this.statement.executeQuery();
			
			if (this.resultSet.next())
				clientCnpj = this.resultSet.getString("CNPJ");
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return clientCnpj;
	}
	
	public Liberation getLiberationByCnpj(String cnpj) {
		
		String pathServer = "http://177.75.66.175:8081/liberation/by-cnpj?cnpj=" + cnpj;
		Liberation liberation = new Liberation();
		URL url;
		try {
			url = new URL(pathServer);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			
			System.out.println(conn.getResponseCode());
			
			InputStream inputStream = conn.getInputStream();			
//			JsonParser jsonParser = Json.createParser(inputStream);
			
			JSONParser jsonParser = new JSONParser();
			try {
				JSONObject jsonObject = (JSONObject) jsonParser.parse(new InputStreamReader(inputStream));
				liberation.setOperator((String)jsonObject.get("operator"));
				liberation.setId((long) jsonObject.get("id"));
				liberation.setObs((String) jsonObject.get("obs"));
				if (jsonObject.get("systemLiberationDate") != null) {
					long sysLibDate = (long) jsonObject.get("systemLiberationDate");
					if (sysLibDate > 0)
						liberation.setSystemLiberationDate(new java.sql.Date(sysLibDate));
				}				
				if (jsonObject.get("tempLiberationDate") != null) {
					long tempLibDate = (long) jsonObject.get("tempLiberationDate");
					if (tempLibDate > 0)
						liberation.setTempLiberationDate(new java.sql.Date(tempLibDate));
				}
				if (jsonObject.get("verificationDate") != null) {
					long verDate = (long) jsonObject.get("verificationDate");
					if (verDate > 0)
						liberation.setVerificationDate(new Timestamp(verDate));
				}				
			} catch (org.json.simple.parser.ParseException e) {
				e.printStackTrace();
			}
			
			/*while(jsonParser.hasNext()) {
				Event e = jsonParser.next();
				if (e == Event.KEY_NAME) {
                    switch (jsonParser.getString()) {
                        case "id":
                        	jsonParser.next();
                        	liberation.setId(jsonParser.getInt());
                            break;
                        case "systemLiberationDate":
                        	jsonParser.next();
                        	Timestamp timeStamp = new Timestamp(jsonParser.getLong());
                        	liberation.setSystemLiberationDate(new Date(timeStamp.getTime()));
                            break;
                        case "verificationDate":
                        	jsonParser.next();
                        	Timestamp timeStampVerificationDate = new Timestamp(jsonParser.getLong());
                        	liberation.setVerificationDate(new Date(timeStampVerificationDate.getTime()));
                            break;
                        case "tempLiberationDate":
                        	jsonParser.next();
                        	
                        	Timestamp timeStampTempLiberationDate = new Timestamp(jsonParser.getLong());
                        	liberation.setTempLiberationDate(new Date(timeStampTempLiberationDate.getTime()));
                        	break;
                        case "obs":
                        	jsonParser.next();
                        	liberation.setObs(jsonParser.getString());
                        	break;
                        case "operator":
                        	jsonParser.next();
                        	liberation.setOperator(jsonParser.getString());
                        	break;
                    }
                }
			}*/
			
			conn.disconnect();
			
		} catch (MalformedURLException e) {
			logger.error(e.getMessage());
			System.out.println(e);
		} catch (IOException e) {
			logger.error(e.getMessage());
			System.out.println(e);
		}
		
		return liberation;
	}
	
	
	public Integer getLiberationRegisterCounts() {
		if (connectionG2Mensagem == null)
			connectToDBG2Mensagem();
		
		try {
			this.statement = connectionG2Mensagem.prepareStatement(SQL_SELECT_COUNT_LIBERATION);
			this.resultSet = this.statement.executeQuery();
			
			if (this.resultSet.next())
				return this.resultSet.getInt("count");
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public Integer getLiberationLastId() {
		if (connectionG2Mensagem == null)
			connectToDBG2Mensagem();
		
		try {
			this.statement = connectionG2Mensagem.prepareStatement(SQL_SELECT_LAST_REGISTER_LIBERATION);
			this.resultSet = this.statement.executeQuery();
			
			if (this.resultSet.next())
				return this.resultSet.getInt("Codigo");
			
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public void deleteOldLiberation(Integer lastId) {
		if (connectionG2Mensagem == null)
			connectToDBG2Mensagem();
		
		try {
			this.statement = connectionG2Mensagem.prepareStatement(SQL_DELETE_LIBERATION);
			this.statement.setInt(1, lastId);
			System.out.println(SQL_DELETE_LIBERATION);
			this.statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void updateLiberation(Liberation liberation, Integer lastIndexId) {
		if (connectionG2Mensagem == null)
			connectToDBG2Mensagem();

		String sql = UPDATE_LIBERATION;
		if (liberation.getVerificationDate() != null)
			sql += ", data_verificacao = ?";
		sql +=  " WHERE Codigo = " + lastIndexId;
		System.out.println(sql);
		try {
			this.statement = connectionG2Mensagem.prepareStatement(sql);
			this.statement.setDate(1, liberation.getSystemLiberationDate());
			this.statement.setString(2, liberation.getOperator());
			this.statement.setDate(3, liberation.getTempLiberationDate());
			this.statement.setString(4, liberation.getObs());
			if (liberation.getVerificationDate() != null)
				this.statement.setTimestamp(5, liberation.getVerificationDate());
			this.statement.executeUpdate();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
	public List<SightSale> getListSightSaleNotUploaded() {
		List<SightSale> listSightSale = new ArrayList<SightSale>();
		
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_SIGHT_SALE_NFE_NOT_UPLOADED);
			this.resultSet = this.statement.executeQuery();
			
			while(this.resultSet.next()) {
				SightSale sightSale = new SightSale();
				sightSale.setId(this.resultSet.getInt("Codigo"));
				sightSale.setSellDate(this.resultSet.getTimestamp("Dt_Venda"));
				sightSale.setNfeKey(this.resultSet.getString("Chave_NFE"));
				sightSale.setNfeStatus(this.resultSet.getString("Situacao_NFE"));
				sightSale.setStatus(this.resultSet.getString("Status"));
				sightSale.setUploaded(this.resultSet.getBoolean("sincNuvem"));
				sightSale.setOnlineShipping(this.resultSet.getString("Envio_online"));
				sightSale.setOnlineShippingDate(this.resultSet.getDate("Envio_online_dt_hr"));
				listSightSale.add(sightSale);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listSightSale;
	}
	
	public List<SightSale> getListSightSaleCanceled() {
		List<SightSale> listSightSale = new ArrayList<SightSale>();

		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_SIGHT_SALE_CANCELED);
			this.resultSet = this.statement.executeQuery();
			
			while(this.resultSet.next()) {
				SightSale sightSale = new SightSale();
				sightSale.setId(this.resultSet.getInt("Codigo"));
				sightSale.setSellDate(this.resultSet.getTimestamp("Dt_Venda"));
				sightSale.setNfeKey(this.resultSet.getString("Chave_NFE"));
				sightSale.setNfeStatus(this.resultSet.getString("Situacao_NFE"));
				sightSale.setStatus(this.resultSet.getString("Status"));
				sightSale.setUploaded(this.resultSet.getBoolean("sincNuvem"));
				sightSale.setOnlineShipping(this.resultSet.getString("Envio_online"));
				sightSale.setOnlineShippingDate(this.resultSet.getDate("Envio_online_dt_hr"));
				listSightSale.add(sightSale);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listSightSale;
	}
	
	public void updateNfeStatusUploadedOk(SightSale sightSale) {
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_UPDATE_SIGHT_SALE_UPLOADED_OK);
			this.statement.setString(1, "Enviada");
			this.statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			this.statement.setBoolean(3, true);
			this.statement.setInt(4, sightSale.getId());
			System.out.println("Atualizando status nfe, upload ok: " + sightSale.getId());
			this.statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<QuickSell> getListQuickSellNotUploaded() {
		List<QuickSell> listQuickSell = new ArrayList<QuickSell>();
		
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_QUICK_SELL_NFE_NOT_UPLOADED);
			this.resultSet = this.statement.executeQuery();
			
			while(this.resultSet.next()) {
				QuickSell quickSell = new QuickSell();
				quickSell.setId(this.resultSet.getInt(1));
				quickSell.setSellDate(this.resultSet.getDate(2));
				quickSell.setCancel(this.resultSet.getBoolean(3));
				quickSell.setStatus(this.resultSet.getString(4));
				quickSell.setNfceKey(this.resultSet.getString(5));
				quickSell.setNfceProtocol(this.resultSet.getString(6));
				quickSell.setOnlineShipping(this.resultSet.getString(7));
				quickSell.setOnlineShippingDate(this.resultSet.getTimestamp(8));
				listQuickSell.add(quickSell);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listQuickSell;
	}
	
	public void updateNfceQuickSellStatusUploadedOk(QuickSell quickSell) {
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_UPDATE_QUICK_SELL_UPLOADED_OK);
			this.statement.setString(1, "Enviada");
			this.statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
			this.statement.setBoolean(3, true);
			this.statement.setInt(4, quickSell.getId());
			System.out.println("Atualizando status nfce, upload ok: " + quickSell.getId());
			this.statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public List<QuickSell> getListQuickSellCanceled() {
		if (connection == null)
			connectToDB();
		
		List<QuickSell> listQuickSellCanceled = new ArrayList<QuickSell>();
		
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_QUICK_SELL_CANCELED);
			this.resultSet = this.statement.executeQuery();
			
			while(this.resultSet.next()) {
				QuickSell quickSell = new QuickSell();
				quickSell.setId(this.resultSet.getInt(1));
				quickSell.setSellDate(this.resultSet.getDate(2));
				quickSell.setCancel(this.resultSet.getBoolean(3));
				quickSell.setStatus(this.resultSet.getString(4));
				quickSell.setNfceKey(this.resultSet.getString(5));
				quickSell.setNfceProtocol(this.resultSet.getString(6));
				quickSell.setOnlineShipping(this.resultSet.getString(7));
				quickSell.setOnlineShippingDate(this.resultSet.getTimestamp(8));
				listQuickSellCanceled.add(quickSell);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.error(e);
		}
		return listQuickSellCanceled;
	}	
	
	public String getBackupPath() {
		
		String path = null;
		
		if (connection == null)
			connectToDB();
		
		try {
			this.statement = connection.prepareStatement(SQL_SELECT_PATH_BACKUP_CONFIG);
			this.resultSet = statement.executeQuery();
			
			if (this.resultSet.next()) {
				path = this.resultSet.getString(1);
			}
			
		} catch (SQLException e) {
			logger.error(e);
			e.printStackTrace();
		}
		
		return path;
	}
	
}
