package au.gov.qld.redland.objective;

import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.TextField;

/**
 * A text field used to capture CSS dimensions.
 * @author danielma
 *
 */
public class DimensionTextField extends TextField {

    /**
     * Constructor
     * 
     */
    public DimensionTextField(String caption, String description, boolean required) {
	super(caption, description);
	setRequired(required);
	setMaxLength(10);
	addValidator(new RegexpValidator("[0-9]{1,4}(px|em|ex|pt|in|pc|mm|cm)", 
		"The field can only contain a valid CSS size expression, such as 100px, 50em"));
    }

}
