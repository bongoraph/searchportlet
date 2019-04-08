package au.gov.qld.redland.ui;

import javax.portlet.PortletPreferences;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Component;

/**
 * A preference field is one that has an associated value to a stored Portlet preference.
 * 
 * @author danielma
 *
 */
public interface PrefField extends Component {

    enum ObjectType {
	INT, STRING, BOOLEAN
    }
    
    /**
     * Initialises the field with the value held in the preference
     * 
     * @param preferences
     */
    public void initWithPrefValue(PortletPreferences preferences);

    /**
     * Saves to value held in the field to the preferences
     * 
     * @param preferences List of preferences
     * 
     * @throws javax.portlet.ReadOnlyException
     */
    public void storePref(PortletPreferences preferences) throws javax.portlet.ReadOnlyException;
    
    /**
     * Validates the field.
     */
    public void validate() throws InvalidValueException;
    
    
}
