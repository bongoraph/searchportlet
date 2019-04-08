package au.gov.qld.redland.ui;

import java.util.List;

import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * A ComboBox which has an associated Portlet preference value.
 * 
 * @author danielma
 * 
 */
public class PrefCombo extends CustomComponent implements PrefField {

    private static final long serialVersionUID = 1L;

    private final ComboBox comboBox = new ComboBox();;

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
     * @param values	List of pre-populated values       
     * @param prefType
     *        Type of Object preference represented field
     */
    public PrefCombo(String caption, String description, String prefName, String defaultValue,
	    List<String> values, boolean required, ObjectType prefType) {
	this.setCaption(caption);
	final HorizontalLayout layout = new HorizontalLayout();
	this.setCompositionRoot(layout);
	layout.addComponent(comboBox);
	setDescription(description);
	comboBox.setRequired(required);
	this.prefName = prefName;
	this.prefType = prefType;
	this.defaultValue = defaultValue;
	comboBox.setItemCaptionMode(ItemCaptionMode.ID);
	comboBox.addItems(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initWithPrefValue(PortletPreferences preferences) {
	if (preferences != null) {
	    comboBox.setValue(preferences.getValue(prefName, defaultValue));
	} else {
	    comboBox.setValue(defaultValue);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storePref(PortletPreferences preferences) throws javax.portlet.ReadOnlyException {
	Validate.notNull(preferences);
	if (StringUtils.isNotBlank((String) comboBox.getValue())) {
	    preferences.setValue(prefName, (String) comboBox.getValue());
	} else {
	    preferences.reset(prefName);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws InvalidValueException {
	comboBox.validate();
    }

}
