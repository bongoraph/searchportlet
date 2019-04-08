package au.gov.qld.redland.objective;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Enum used to keep track of properties to display for an Objective object.
 * 
 * @author danielma
 * 
 */
public enum ObjectivePropType {

    APPLICATION_ID("Application ID", "Application ID"), CREATOR("Creator",  null), CAVEATS("Caveats", null), CLASSIFICATION("Classification", null), DATE_CREATED("Date Created",  null), DATE_UPDATED(
	    "Date Updated",  null), LEGACY_DW_ID("Legacy DataWorks ID", "Legacy Dataworks Document ID"), OBJECTIVE_ID("Objective ID",  ""), PRIVILEDGES_ASSIGNED(
		    "Priviledges Assigned",  ""), PRIVILEDGES_EFFECTIVE("Priviledges Effective",
			    ""), TYPE_DEFINITION("Type Definition",  ""),
			    UPDATED_BY("Updated by",  null);

    public static ObjectivePropType getObjectivePropType(String label) {
	for (final ObjectivePropType type : ObjectivePropType.values()) {
	    if (type.getLabel().equals(label)) {
		return type;
	    }
	}
	return null;
    }

    /**
     * Returns a list of all of the labels for the property types
     * @return	List of Strings
     */
    public static List<String> getAllPropLabels() {
	List<String> labels = new ArrayList<>();
	for (final ObjectivePropType type : ObjectivePropType.values()) {
	    labels.add(type.getLabel());
	}
	return labels;
    }

    public static List<ObjectivePropType> getAllPropTypes(List<String> labels) {
	final ArrayList<ObjectivePropType> properties = new ArrayList<>();
	ObjectivePropType.values();

	for (final String label : labels) {
	    final ObjectivePropType type = getObjectivePropType(label);
	    if (type != null) {
		properties.add(type);
	    }
	}
	return properties;
    }

    public static List<ObjectivePropType> getPropTypes(Collection<String> labels) {
	final ArrayList<ObjectivePropType> properties = new ArrayList<>();
	for (final String label : labels) {
	    final ObjectivePropType type = getObjectivePropType(label);
	    if (type != null) {
		properties.add(type);
	    }
	}
	return properties;
    }
    
//    public static List<ObjectivePropType> getPropTypes(List<String> labels) {
//	final ArrayList<ObjectivePropType> properties = new ArrayList<>();
//	for (final String label : labels) {
//	    final ObjectivePropType type = getObjectivePropType(label);
//	    if (type != null) {
//		properties.add(type);
//	    }
//	}
//	return properties;
//    }

    // human readable label
    private String label;

    // name of Objective property
    private String objectiveLabel;

    ObjectivePropType(String label,String objectiveLabel) {
	this.label = label;
	this.objectiveLabel = objectiveLabel;
    }

    public String getLabel() {
	return label;
    }

    public String getObjectiveLabel() {
	return objectiveLabel;
    }

}