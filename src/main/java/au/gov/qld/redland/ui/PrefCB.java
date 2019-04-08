package au.gov.qld.redland.ui;

import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * A Custom CheckBox which has an associated Portlet preference value. The
 * caption is displayed in the CustomComponent to fix an issue with captions
 * being displayed to the right and in a large font in Liferay.
 * 
 * 
 * @author danielma
 * 
 */
public class PrefCB extends CustomComponent implements PrefField {

    private static final long serialVersionUID = 1L;

    private final CheckBox checkBox = new CheckBox();

    /**
     * Default value for field
     */
    private final boolean defaultValue;

    /**
     * Name of preference
     */
    private final String prefName;

    /**
     * 
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
     */
    public PrefCB(String caption, String prefName, boolean defaultValue) {
	this.setCaption(caption);
	final HorizontalLayout layout = new HorizontalLayout();
	this.setCompositionRoot(layout);
	layout.addComponent(checkBox);
	this.prefName = prefName;
	this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initWithPrefValue(PortletPreferences preferences) {
	if (preferences != null) {
	    boolean booleanValue = defaultValue;
	    final String storedValue = preferences.getValue(prefName, "");
	    if (StringUtils.isNotBlank(storedValue)) {
		try {
		    booleanValue = Boolean.parseBoolean(storedValue);
		} catch (final Exception e) {
		}
	    }
	    checkBox.setValue(booleanValue);
	} else {
	    checkBox.setValue(defaultValue);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storePref(PortletPreferences preferences) throws javax.portlet.ReadOnlyException {
	Validate.notNull(preferences);
	preferences.setValue(prefName, Boolean.toString(checkBox.getValue()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.gov.qld.redland.ui.PrefField#validate()
     */
    @Override
    public void validate() throws InvalidValueException {
    }

}
