package com.gbm;

import com.ibm.mfp.adapter.api.ConfigurationAPI;
import com.ibm.mfp.adapter.api.MFPJAXRSApplication;
import org.apache.commons.dbcp.BasicDataSource;

import javax.ws.rs.core.Context;
import java.util.logging.Logger;

public class UserInsertDBApplication extends MFPJAXRSApplication{

	static Logger logger = Logger.getLogger(UserInsertDBApplication.class.getName());

	public BasicDataSource dataSource = null;

	@Context
	ConfigurationAPI configurationAPI;

	@Override
	protected void init() throws Exception {
		logger.info("Adapter initialized!");

		dataSource= new BasicDataSource();
		dataSource.setDriverClassName(configurationAPI.getPropertyValue("DB_driver"));
		dataSource.setUrl(configurationAPI.getPropertyValue("DB_url"));
		dataSource.setUsername(configurationAPI.getPropertyValue("DB_username"));
		dataSource.setPassword(configurationAPI.getPropertyValue("DB_password"));
	}
	protected void destroy() throws Exception {
		logger.info("Adapter destroyed!");
	}

	@Override
	protected String getPackageToScan() {
		return getClass().getPackage().getName();
	}
}