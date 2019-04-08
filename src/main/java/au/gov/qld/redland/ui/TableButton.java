/**
 * 
 */
package au.gov.qld.redland.ui;

import com.vaadin.ui.Button;

/**
 * A button subclass that overrides the toString method to return the caption
 * 
 * @author danielma
 *
 */
public class TableButton extends Button {

    public TableButton(String caption) {
	super(caption);
    }
    
    @Override
    public String toString() {
	// TODO Auto-generated method stub
	return getCaption();
    }

}
