package au.gov.qld.redland.ui;

import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.TextField;

/**
 * A TextField which has an associated Portlet preference value.
 * 
 * @author danielma
 * 
 */
public class PrefTF extends TextField implements PrefField {

    private static final long serialVersionUID = 1L;;

    /**
     * Default value for field
     */
    private final String defaultValue;

    /**
     * Name of preference
     */
    private final String prefName;

    /**
     * Type of Preference
     */
    private final ObjectType prefType;

    /**
     * Constructor
     * 
     * @param caption
     *        Caption for Field
     * @param description
     *        Popup Help
     * @param prefName
     *        Name of the preference associated with field
     * @param defaultValue
     *        Default value
     * @param maxLength
     * 		Max Length       
     * @param prefType
     *        Type of Object preference represented field
     */
    public PrefTF(String caption, String description, String prefName,
	    String defaultValue, boolean required, int maxLength, ObjectType prefType) {
	super(caption);
	setDescription(description);
	setMaxLength(maxLength);
	setRequired(required);
	this.prefName = prefName;
	this.prefType = prefType;
	this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initWithPrefValue(PortletPreferences preferences) {
	if (preferences != null) {
	    this.setValue(preferences.getValue(prefName, defaultValue));
	} else {
	    this.setValue(defaultValue);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storePref(PortletPreferences preferences) throws javax.portlet.ReadOnlyException {
	Validate.notNull(preferences);
	if (StringUtils.isNotBlank(this.getValue())) {
	    preferences.setValue(prefName, this.getValue());
	} else {
	    preferences.reset(prefName);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws InvalidValueException {
	super.validate();
    }

}
