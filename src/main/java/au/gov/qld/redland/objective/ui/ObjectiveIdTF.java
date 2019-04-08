package au.gov.qld.redland.objective.ui;

import au.gov.qld.redland.ui.PrefTF;

import com.vaadin.data.validator.RegexpValidator;

/**
 * A text field used to capture Objective IDs.
 * 
 * @author danielma
 * 
 */
public class ObjectiveIdTF extends PrefTF {

    private static final int OBJECTIVE_ID_MAX_LENGTH = 10;
    private static final long serialVersionUID = 1632221931163452149L;

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
    public ObjectiveIdTF(String caption, String description, String prefName, String defaultValue,
	    boolean required) {
	super(caption, description, prefName, defaultValue, required, OBJECTIVE_ID_MAX_LENGTH,
		ObjectType.STRING);
	setInputPrompt("eg. qA3326");
	addValidator(new RegexpValidator("[a-zA-Z]{1,3}[0-9]{2,7}",
		"The id must only contain alphanumerical characters and must be between 5 and 8 characters long."));
	setRequiredError("The ID must be entered");
    }

}
