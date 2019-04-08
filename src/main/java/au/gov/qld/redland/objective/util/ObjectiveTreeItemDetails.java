/**
 * 
 */
package au.gov.qld.redland.objective.util;

/**
 * Object to store summary of Objective item stored in a ObjectiveHierarchicalContainer
 * 
 * @author danielma
 *
 */
public class ObjectiveTreeItemDetails implements Cloneable {
    public String objectId;
    public String docName;
    public String iconName;
    public String description;
    public boolean isDocument;
    public String url;
    
    public ObjectiveTreeItemDetails() {};
    
    public ObjectiveTreeItemDetails(String objectId, String url, String docName, String iconName, String description, boolean isDocument) {
	this.objectId = objectId;
	this.url = url;
	this.docName = docName;
	this.iconName = iconName;
	this.description = description;
	this.isDocument = isDocument;
    }
    
    public ObjectiveTreeItemDetails clone() {
	ObjectiveTreeItemDetails oClone = new ObjectiveTreeItemDetails(this.objectId, this.url, 
		this.docName, this.iconName, this.description, this.isDocument);
	return oClone;
    }

}
