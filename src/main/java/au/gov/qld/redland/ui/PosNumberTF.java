package au.gov.qld.redland.ui;

import com.vaadin.data.validator.RegexpValidator;

/**
 * A PreferenceTextField which only allows positive numbers to be entered.
 * 
 * 
 * @author danielma
 * 
 */
public class PosNumberTF extends PrefTF {

    private static final long serialVersionUID = 489881653558867405L;

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
     *        Max Length
     * @param prefType
     *        Type of Object preference represented field
     */
    public PosNumberTF(String caption, String description, String prefName, String defaultValue,
	    String prompt, boolean required, int maxLength) {
	super(caption, description, prefName, defaultValue, required, maxLength, ObjectType.INT);
	setRequired(required);
	setInputPrompt(prompt);
	addValidator(new RegexpValidator("[0-9]{1," + Integer.toString(maxLength) + "}",
		"The field can only contain positive integers, such as 1, 5, 10, 100, etc"));
    }

    /**
     * Sets the value of the TextField
     * 
     * @param value
     */
    public void setValue(int value) {
	super.setValue(Integer.toString(value));
    }

}
