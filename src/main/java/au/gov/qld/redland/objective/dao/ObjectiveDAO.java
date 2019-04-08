package au.gov.qld.redland.objective.dao;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import au.gov.qld.redland.objective.service.AppSettingsService;

/**
 * @author DanielMa
 *
 */
public class ObjectiveDAO {

    // for logging
    private static Logger logger = Logger.getGlobal();

    private static final String PU_NAME_DW_STAGING = "dwStagingPU";
    
    /**
     * Returns an initialised Entity Manager
     * 
     * @param properties Properties with DB connection details
     * @return entity manager
     */
    private static EntityManager getEntityManager(String persistanceUnit) throws IOException {
	Properties portletProperties = AppSettingsService.getProperties();
	Properties connectionProps = new Properties();
	Enumeration<Object> keys = portletProperties.keys();
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    if (key.startsWith(persistanceUnit)) {
		String value = portletProperties.getProperty(key);
		connectionProps.setProperty(key.substring(persistanceUnit.length()+1), value);
	    }
	}
	EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistanceUnit, connectionProps);
	return emf.createEntityManager();
    }
    
    /**
     * Returns an initialised Entity Manager that is connected to the Liferay DB
     * @return EntityManager
     * @throws IOException
     */
    public static EntityManager getDWStagingEntityManager() throws IOException {
	return getEntityManager(PU_NAME_DW_STAGING);
    }

}

