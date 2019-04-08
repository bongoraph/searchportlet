/**
 * 
 */
package au.gov.qld.redland.ui;

import com.vaadin.server.Resource;
import com.vaadin.ui.Link;

/**
 * A Link subclass that overrides the toString method to return the caption
 * 
 * @author danielma
 *
 */
public class TableLink extends Link {

    public TableLink(String caption, Resource resource) {
	super(caption, resource);
    }
    
    @Override
    public String toString() {
	// TODO Auto-generated method stub
	return getCaption();
    }

}
