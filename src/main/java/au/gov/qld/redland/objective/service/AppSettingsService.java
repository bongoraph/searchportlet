package au.gov.qld.redland.objective.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.liferay.portal.kernel.util.PropsUtil;

/**
 * Manages the settings for the Objective Portlets.
 * 
 * @author danielma
 *
 * @todo implement load/save from db  
 */
public class AppSettingsService {

    public static final String PROP_NAME_OBJ_PRD_USER = "prd.objective.server.user";
    public static final String PROP_NAME_OBJ_PRD_PASSWORD = "prd.objective.server.password";
    public static final String PROP_NAME_OBJ_PRD_PORT = "prd.objective.server.port";
    public static final String PROP_NAME_OBJ_PRD_URL = "prd.objective.server.url";
    public static final String PROP_NAME_OBJ_PRD_SESSION_TIMEOUT = "prd.objective.server.session.timetolive";
    public static final String PROP_NAME_OBJ_UAT_USER = "uat.objective.server.user";
    public static final String PROP_NAME_OBJ_UAT_PASSWORD = "uat.objective.server.password";
    public static final String PROP_NAME_OBJ_UAT_PORT = "uat.objective.server.port";
    public static final String PROP_NAME_OBJ_UAT_URL = "uat.objective.server.url";
    public static final String PROP_NAME_OBJ_UAT_SESSION_TIMEOUT = "uat.objective.server.session.timetolive";
    public static final String PROP_NAME_OBJ_UAT_FIELD_DW_DOC_ID = "uat.field.dw.doc.id";
    public static final String PROP_NAME_OBJ_PRD_FIELD_DW_DOC_ID = "prd.field.dw.doc.id";    
    
    private static AppSettingsService instance;
    // for logging
    private static Logger logger = Logger.getGlobal();
    /**
     * Name of property that points to the configuration directory for portlets, as found in the portal-ext.properties.
     */
    private static final String PROP_RCC_CONF_DIR = "rcc.conf.dir";

    // name of properties with default values
    private static String PROPERTIES_FILENAME = "objective.properties";

    public static synchronized AppSettingsService getinstance() {
	if (instance == null) {
	    instance = new AppSettingsService();
	} 
	return instance;
    }

    /**
     * Returns the list of default applications properties, it overrides the properties bundled with the Portlet by
     * looking first at a location specified in rcc.conf.dir for the properties. The rcc.conf.dir is set in the portal-ext.properties
     * file located at the \webpps\ROOT\WEB-INF\classes directory of liferay.
     * 
     * @return Properties
     * 
     * @throws IOException if an error occurs loading the properties file bundled with the portlets
     */
    public static Properties getProperties() throws IOException {
	Properties properties = new Properties();
	try {
	    properties.load(new FileInputStream(PropsUtil.get(PROP_RCC_CONF_DIR) + PROPERTIES_FILENAME));
	} catch (IOException ex1) {
	    logger.warning("Could not load properties file: " + PropsUtil.get(PROP_RCC_CONF_DIR) + PROPERTIES_FILENAME);
	    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
	    InputStream stream = classLoader.getResourceAsStream(PROPERTIES_FILENAME);
	    if (stream == null) {
	    } else {
		properties.load(stream);
	    }
	}
	return properties;
    }

}
