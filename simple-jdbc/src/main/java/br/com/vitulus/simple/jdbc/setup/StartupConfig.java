package br.com.vitulus.simple.jdbc.setup;

import static br.com.vitulus.simple.jdbc.setup.StartupConfig.CONFIG_PROPERTY_ID.DRIVER;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.CONFIG_PROPERTY_ID.PWD;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.CONFIG_PROPERTY_ID.URL;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.CONFIG_PROPERTY_ID.USER;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.CONFIG_PROPERTY_ID.USE_POOL;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.POOL_PROPERTY_ID.MAX_ACTIVE;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.POOL_PROPERTY_ID.MAX_IDLE;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.POOL_PROPERTY_ID.MAX_WAIT;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.POOL_PROPERTY_ID.NAME;
import static br.com.vitulus.simple.jdbc.setup.StartupConfig.POOL_PROPERTY_ID.PROPERTIES;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import br.com.vitulus.simple.jdbc.util.StringUtils;

public class StartupConfig {

	ConnectionConfig 	connConfig;
	PoolConfig       	poolConfig;
	
	private StartupConfig() {
	}
	
	public static StartupConfig getConfigProperties(Properties properties){
		StartupConfig config = new StartupConfig();
		config.connConfig = buildConnConfig(
				DRIVER.getPropValue(properties),
				URL.getPropValue(properties),
				PWD.getPropValue(properties),
				USER.getPropValue(properties),
				USE_POOL.getPropValue(properties) != null);		
		if(config.connConfig.usePool){
			config.poolConfig = buildPoolConfig(
				MAX_ACTIVE.getPropValue(properties),
				MAX_IDLE.getPropValue(properties),
				MAX_WAIT.getPropValue(properties),
				NAME.getPropValue(properties),
				PROPERTIES.getPropValue(properties));
		}		
		return config;
	}
	
	public static StartupConfig getConfigProperties(String pathname){
		return getConfigProperties(new File(pathname));
	}
	
	public static StartupConfig getConfigProperties(File file){
		try {
			return getConfigProperties(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static StartupConfig getConfigProperties(InputStream in){
		Properties properties = new Properties();;
		try {
			properties.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return getConfigProperties(properties);
	}
	
	public static StartupConfig getConfigProperties(byte[] config){				
		return getConfigProperties(new ByteArrayInputStream(config));
	}
	
	public static StartupConfig getConfig(Document context){
		StartupConfig config = new StartupConfig();		
		config.connConfig = buildConnConfig(
				DRIVER.getXvalue(context),
				URL.getXvalue(context),
				PWD.getXvalue(context),
				USER.getXvalue(context),
				USE_POOL.getXvalue(context) != null);		
		if(config.connConfig.usePool){
			config.poolConfig = buildPoolConfig(
				MAX_ACTIVE.getXvalue(context),
				MAX_IDLE.getXvalue(context),
				MAX_WAIT.getXvalue(context),
				NAME.getXvalue(context),
				PROPERTIES.getXvalue(context));
		}		
		return config;
	}
	
	public static StartupConfig getConfig(String filepath){
		return getConfig(new File(filepath));
	}
	
	public static StartupConfig getConfig(File file){
		try {
			return getConfig(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static StartupConfig getConfig(InputStream in){
	    Document doc = null;
		try {
		    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		    doc = dBuilder.parse(in);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}	    
	    return getConfig(doc);
	}
	
	public static StartupConfig getConfig(byte[] context){
		return getConfig(new ByteArrayInputStream(context));
	}	
	
	public static PoolConfig buildPoolConfig(String maxActive, String maxIdle, String maxWait,String name,String propString){
		Properties properties = new Properties();
		if(!StringUtils.isNullOrTrimEmpty(propString)){	
			propString = propString.replace(";", "\r\n");
			StringReader reader = new StringReader(propString);
			try {
				properties.load(reader);
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				reader.close();
			}
		}
		return new PoolConfig(maxActive, maxIdle, maxWait,name,properties);
	}
	
	public static ConnectionConfig buildConnConfig(String driver, String url, String password,String user, boolean usePool){
		return new ConnectionConfig(driver, url, password, user, usePool);
	}
	
	public boolean usePool(){
		return connConfig != null && connConfig.usePool;
	}
	
	public ConnectionConfig getConnConfig() {
		return connConfig;
	}

	public PoolConfig getPoolConfig() {
		return poolConfig;
	}

	public static class ConnectionConfig{
		String driver;
		String url;
		String password;
		String user;
		boolean usePool;
		public ConnectionConfig(String driver, String url, String password,String user, boolean usePool) {
			this.driver = driver;
			this.url = url;
			this.password = password;
			this.user = user;
			this.usePool = usePool;
		}
		public String getDriver() {
			return driver;
		}
		public String getUrl() {
			return url;
		}
		public String getPassword() {
			return password;
		}
		public String getUser() {
			return user;
		}
		public boolean isUsePool() {
			return usePool;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((driver == null) ? 0 : driver.hashCode());
			result = prime * result
					+ ((password == null) ? 0 : password.hashCode());
			result = prime * result + ((url == null) ? 0 : url.hashCode());
			result = prime * result + (usePool ? 1231 : 1237);
			result = prime * result + ((user == null) ? 0 : user.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ConnectionConfig other = (ConnectionConfig) obj;
			if (driver == null) {
				if (other.driver != null)
					return false;
			} else if (!driver.equals(other.driver))
				return false;
			if (password == null) {
				if (other.password != null)
					return false;
			} else if (!password.equals(other.password))
				return false;
			if (url == null) {
				if (other.url != null)
					return false;
			} else if (!url.equals(other.url))
				return false;
			if (usePool != other.usePool)
				return false;
			if (user == null) {
				if (other.user != null)
					return false;
			} else if (!user.equals(other.user))
				return false;
			return true;
		}		
	}
	
	public static class PoolConfig{
		String 		maxActive;
		String 		maxIdle;
		String 		maxWait;
		String 		name;
		Properties 	properties;
		public PoolConfig(String maxActive, String maxIdle, String maxWait,String name,Properties properties) {
			this.maxActive = maxActive;
			this.maxIdle = maxIdle;
			this.maxWait = maxWait;
			if(name == null || !name.startsWith("jdbc/")){
				throw new IllegalArgumentException("The attribute name must start with 'jdbc/'");
			}
			this.name = name;
			this.maxActive = maxActive;
			this.maxIdle = maxIdle;
			this.maxWait = maxWait;
			this.properties = properties;
		}
		public String getMaxActive() {
			return maxActive;
		}
		public String getMaxIdle() {
			return maxIdle;
		}
		public String getMaxWait() {
			return maxWait;
		}		
		public String getName() {
			return name;
		}
		public Properties getProperties() {
			return properties;
		}		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((maxActive == null) ? 0 : maxActive.hashCode());
			result = prime * result
					+ ((maxIdle == null) ? 0 : maxIdle.hashCode());
			result = prime * result
					+ ((maxWait == null) ? 0 : maxWait.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			result = prime * result
					+ ((properties == null) ? 0 : properties.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PoolConfig other = (PoolConfig) obj;
			if (maxActive == null) {
				if (other.maxActive != null)
					return false;
			} else if (!maxActive.equals(other.maxActive))
				return false;
			if (maxIdle == null) {
				if (other.maxIdle != null)
					return false;
			} else if (!maxIdle.equals(other.maxIdle))
				return false;
			if (maxWait == null) {
				if (other.maxWait != null)
					return false;
			} else if (!maxWait.equals(other.maxWait))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			if (properties == null) {
				if (other.properties != null)
					return false;
			} else if (!properties.equals(other.properties))
				return false;
			return true;
		}		
	}
	
	private static XPath xpath;
	
	static{
		xpath = XPathFactory.newInstance().newXPath();
	}
	
	enum POOL_PROPERTY_ID{
		MAX_ACTIVE("pool.maxActive","/Context/Resource/@maxActive"),
		MAX_IDLE("pool.maxIdle","/Context/Resource/@maxIdle"),
		MAX_WAIT("pool.maxWait","/Context/Resource/@maxWait"),
		NAME("pool.name","/Context/Resource/@name"),
		PROPERTIES  ("pool.properties","/Context/Resource/@properties");
		
		private String 					property;
		private XPathExpression 		expression;
		
		private POOL_PROPERTY_ID(String property, String expression) {
			this.property = property;
			this.expression = StartupConfig.compileExpression(expression);
		}
		
		public String getXvalue(Document context){
			return StartupConfig.getXvalue(expression,context);
		}
		
		public String getPropValue(Properties properties){
			return properties.getProperty(property,"");
		}
	}
	
	enum CONFIG_PROPERTY_ID{
		DRIVER("conn.driver","/Context/Resource/@driverClassName"),
		URL("conn.url","/Context/Resource/@url"),
		PWD("conn.pwd","/Context/Resource/@password"),
		USER("conn.user","/Context/Resource/@username"),
		USE_POOL(POOL_PROPERTY_ID.NAME.property,POOL_PROPERTY_ID.NAME.expression);
		
		private String 					property;
		private XPathExpression 		expression;		
		
		private CONFIG_PROPERTY_ID(String property,XPathExpression expression){
			this.property = property;
			this.expression = expression;
		}
		
		private CONFIG_PROPERTY_ID(String property,String expression){
			this.property = property;
			this.expression = StartupConfig.compileExpression(expression);
		}
		
		public String getXvalue(Document context){
			return StartupConfig.getXvalue(expression,context);
		}
		
		public String getPropValue(Properties properties){
			return properties.getProperty(property,null);
		}
		
	}

	
	private static XPathExpression compileExpression(String expression){
		try {
			return xpath.compile(expression);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static String getXvalue(XPathExpression expression,Document context){
		try {
			return new XPathNavigation(expression,context).getAttributeValue();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((connConfig == null) ? 0 : connConfig.hashCode());
		result = prime * result
				+ ((poolConfig == null) ? 0 : poolConfig.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StartupConfig other = (StartupConfig) obj;
		if (connConfig == null) {
			if (other.connConfig != null)
				return false;
		} else if (!connConfig.equals(other.connConfig))
			return false;
		if (poolConfig == null) {
			if (other.poolConfig != null)
				return false;
		} else if (!poolConfig.equals(other.poolConfig))
			return false;
		return true;
	}	
	
}