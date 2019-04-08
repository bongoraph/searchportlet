/**
 * Stores the parent for an Objective object that is displayed in a TreeTable
 * 
 * @author danielma
 * 
 */
package au.gov.qld.redland.objective.util;


public class ParentObjectDetails {
    public String id;
    public String name;
    public String type;
    public String effectivePriviledges;
    public String assignedPriviledges;
    
    
    /**
     * @param id
     * @param name
     */
    public ParentObjectDetails(String id, String name, String type, String effectivePriviledges, String assignedPriviledges) {
	super();
	this.id = id;
	this.name = name;
	this.type = type;
	this.effectivePriviledges = effectivePriviledges;
	this.assignedPriviledges = assignedPriviledges;
    }
    
    public String getNameTreeLabel() {
	return name + "-Name";
    }
    
    public String getIdTreeLabel() {
	return id  + "-ID"; 
		
    }
    
    public String getPrivilegesAssignedTreeLabel() {
	return assignedPriviledges + "-Priviledges Assigned";
    }
    
}