package au.gov.qld.redland.ui;

import java.util.Collection;
import java.util.List;

import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.Validate;
import org.vaadin.tepi.listbuilder.ListBuilder;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;

/**
 * A ListBuilder which has an associated Portlet preference value.
 * 
 * @author danielma
 * 
 */
public class PrefListBuilder extends CustomComponent implements PrefField {

    private static final long serialVersionUID = 1L;

    private final ListBuilder listBuilder = new ListBuilder();

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
    public PrefListBuilder(String caption, String description, String prefName, String defaultValue,
	    List<String> values, boolean required, ObjectType prefType) {
	this.setCaption(caption);
	final HorizontalLayout layout = new HorizontalLayout();
	this.setCompositionRoot(layout);
	layout.addComponent(listBuilder);
	setDescription(description);
	listBuilder.setRequired(required);
	this.prefName = prefName;
	this.prefType = prefType;
	this.defaultValue = defaultValue;
	listBuilder.setItemCaptionMode(ItemCaptionMode.ID);
	listBuilder.addItems(values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initWithPrefValue(PortletPreferences preferences) {
	if (preferences != null) {
	    listBuilder.setValue(preferences.getValue(prefName, defaultValue));
	} else {
	    listBuilder.setValue(defaultValue);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storePref(PortletPreferences preferences) throws javax.portlet.ReadOnlyException {
	Validate.notNull(preferences);
	Collection<String> values = (Collection<String>)listBuilder.getValue();
	if (values != null && values.size() > 0) {
	    preferences.setValue(prefName, values.toString());
	} else {
	    preferences.reset(prefName);
	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws InvalidValueException {
	listBuilder.validate();
    }

}
