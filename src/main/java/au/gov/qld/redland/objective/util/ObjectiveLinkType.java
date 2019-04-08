/**
 * 
 */
package au.gov.qld.redland.objective.util;

/**
 * The different document link types that can be displayed
 */
public enum ObjectiveLinkType {
    BrowserPreview("Preview in Browser"), OBR("OBR Link to Objective - requires Objective app"), WebLink(
    	"Web Link - requires authentication");

    public static ObjectiveLinkType getLinkType(String label) {
        for (final ObjectiveLinkType linkType : ObjectiveLinkType.values()) {
    	if (linkType.getLabel().equalsIgnoreCase(label)) {
    	    return linkType;
    	}
        }
        return ObjectiveLinkType.BrowserPreview;
    }

    String label;

    ObjectiveLinkType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}