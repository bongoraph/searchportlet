package au.gov.qld.redland.objective.util;

/**
 * To distinguish between communication exceptions between client and server.
 * 
 * OjiException seems to be thrown otherwise by most methods. 
 * 
 * @author danielma
 *
 */
public class OjiServerException extends Exception {

    private static final long serialVersionUID = -3937663571890699582L;
    
    public OjiServerException(Exception e) {
	super(e);
    }
    
    public OjiServerException(String message) {
	super(message);
    }
    

}
