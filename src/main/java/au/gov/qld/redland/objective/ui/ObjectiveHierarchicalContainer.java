/**
 * 
 */
package au.gov.qld.redland.objective.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import au.gov.qld.redland.objective.DocPreviewWindow;
import au.gov.qld.redland.objective.ObjectivePropType;
import au.gov.qld.redland.objective.util.Constants;
import au.gov.qld.redland.objective.util.ObjectiveLinkType;
import au.gov.qld.redland.objective.util.ObjectiveServer;
import au.gov.qld.redland.objective.util.ObjectiveTreeItemDetails;
import au.gov.qld.redland.objective.util.ObjectiveUtil;
import au.gov.qld.redland.objective.util.ParentObjectDetails;
import au.gov.qld.redland.ui.TableButton;
import au.gov.qld.redland.ui.TableLink;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.objective.client.ObjAlias;
import com.objective.client.ObjDocument;
import com.objective.client.ObjFolder;
import com.objective.client.ObjObject;
import com.objective.client.ObjPhysicalFile;
import com.objective.oji.OjiCatalogueableObject;
import com.objective.oji.OjiDocument;
import com.objective.oji.OjiFolder;
import com.objective.oji.OjiObject;
import com.objective.oji.OjiSession;
import com.vaadin.data.Item;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.BaseTheme;

/**
 * @author danielma
 *
 */
public class ObjectiveHierarchicalContainer extends HierarchicalContainer {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String FOLDER_TBL_COL_NAME = "Name";
    // Objective Server
    private ObjectiveServer server;
    private static Log logger = LogFactoryUtil.getLog(ObjectiveHierarchicalContainer.class.getName());
    //Maximum char length of hyperlinks
    private int linkMaxLength;
    //Type of link that documents will have
    private ObjectiveLinkType linkType;
    // Summary of Objective items being held
    private HashMap<String, ObjectiveTreeItemDetails> objectiveItems;
    //Height of preview document window
    private  String previewHeight;
    //Width of preview document window
    private  String previewWidth;
    //Objective properties to display
    private List<ObjectivePropType> propTypes;
    //Objective ID of root object for container
    private String rootObjectId;
    //Label used to relay back progress
    private Label status;
    // maximum number of items that are displayed
    private int MAX_NUMBER_ITEMS = 1000;


    /**
     * Constructor
     * 
     * @param server		Objective server to retrieve data from
     * @param rootObjectId	Objective ID of root object for container
     * @param linkType		Type of link that documents will have
     * @param propTypes		Objective properties to display
     * @param linkMaxLength	Maximum char length of hyperlinks 
     * @param previewHeight	Height of preview document window
     * @param previewWidth	Width of preview document window
     * @param status	Label used to relay back progress
     */
    public ObjectiveHierarchicalContainer(ObjectiveServer server, String rootObjectId, ObjectiveLinkType linkType, List<ObjectivePropType> propTypes, int linkMaxLength, 
	    String previewHeight, String previewWidth, Label status) {
	super();
	objectiveItems = new HashMap<String, ObjectiveTreeItemDetails>();
	this.server = server;
	this.rootObjectId = rootObjectId;
	this.linkType = linkType;
	this.propTypes = new ArrayList<ObjectivePropType>();
	this.propTypes.addAll(propTypes);
	this.linkMaxLength = linkMaxLength;
	this.previewHeight = previewHeight;
	this.previewWidth = previewWidth;
	addContainerProperty(FOLDER_TBL_COL_NAME, Component.class, null);
	for (final ObjectivePropType type : propTypes) {
	    addContainerProperty(type.getLabel(), String.class, null);
	}
    }

    /**
     * Adds a tree node to the dataContainer with an appropriate icon and link. 
     * 
     * @param dataContainer	To add item to
     * @param object	Object to add
     * @param linkType	link type as set in preference
     * @param propTypes	Objective properties to display
     * @param maxCharLength	Max char length of link
     * @param previewWinHeight	Height of preview window if used
     * @param previewWinWidth	Width of preview window if used
     * @param parents	Map containing the parents for the object
     */
    @SuppressWarnings("unchecked")
    public void addObject(Object object, Map<String, ParentObjectDetails> parents, OjiSession session) {
	ObjectiveTreeItemDetails objectItem = getTreeItemDetails(object);
	objectiveItems.put(objectItem.objectId, objectItem);
	final String id = ObjectiveUtil.getObjectId(object);
	Component link = getObjectHyperlink(objectItem);
	Item item = super.addItem(id);
	item.getItemProperty("Name").setValue(link);
	for (final ObjectivePropType type : propTypes) {
	    if (type.equals(ObjectivePropType.CAVEATS)) {
		item.getItemProperty(ObjectivePropType.CAVEATS.getLabel()).setValue(ObjectiveUtil.getObjectCaveats(object));
	    } else if (type.equals(ObjectivePropType.CLASSIFICATION)) {
		item.getItemProperty(ObjectivePropType.CLASSIFICATION.getLabel()).setValue(ObjectiveUtil.getObjectClassification(object));
	    } else if (type.equals(ObjectivePropType.CREATOR)) {
		item.getItemProperty(ObjectivePropType.CREATOR.getLabel()).setValue(ObjectiveUtil.getCreator(object));
	    } else if (type.equals(ObjectivePropType.UPDATED_BY)) {
		item.getItemProperty(ObjectivePropType.UPDATED_BY.getLabel()).setValue(ObjectiveUtil.getUpdatedBy(object));
	    } else if (type.equals(ObjectivePropType.DATE_CREATED)) {
		item.getItemProperty(ObjectivePropType.DATE_CREATED.getLabel()).setValue(ObjectiveUtil.getDateCreate(object));
	    } else if (type.equals(ObjectivePropType.DATE_UPDATED)) {
		item.getItemProperty(ObjectivePropType.DATE_UPDATED.getLabel()).setValue(ObjectiveUtil.getDateCreate(object));
	    } else if (type.equals(ObjectivePropType.OBJECTIVE_ID)) {
		item.getItemProperty(ObjectivePropType.OBJECTIVE_ID.getLabel()).setValue(ObjectiveUtil.getObjectId(object));
	    } else if (type.equals(ObjectivePropType.PRIVILEDGES_EFFECTIVE)) {
		item.getItemProperty(ObjectivePropType.PRIVILEDGES_EFFECTIVE.getLabel()).setValue(ObjectiveUtil.getObjectEffectivePriviledges(object).toString());
	    } else if (type.equals(ObjectivePropType.PRIVILEDGES_ASSIGNED)) {
		item.getItemProperty(ObjectivePropType.PRIVILEDGES_ASSIGNED.getLabel()).setValue(ObjectiveUtil.getObjectAssignedPriviledges(object).toString());
	    } else if (type.equals(ObjectivePropType.TYPE_DEFINITION)) {
		item.getItemProperty(ObjectivePropType.TYPE_DEFINITION.getLabel()).setValue(ObjectiveUtil.getObjectTypeDefn(object));
	    } else if (StringUtils.isNotBlank(type.getObjectiveLabel())) {
		    item.getItemProperty(type.getLabel()).setValue(
			    ObjectiveUtil.getFieldValue(object, type.getObjectiveLabel(), session));
	    }
	}
    }

    /**
     * Adds the Objective items to container starting at the parentObject. 
     * 
     * @param parentObject	ID of object to add
     * @param parents	Ancestor information of object to add to container
     * @param objectsAdded	count of objects added to tree so far	
     * @return	int	Number of objects added
     */
    public int addObjectsToObjectTree(OjiObject parentObject,
	    Map<String, ParentObjectDetails> parents, int objectsAdded, OjiSession session) {
	try {
	    final Method getContentsMethod = parentObject.getClass().getMethod("getContents",
		    (Class[]) null);
	    getContentsMethod.setAccessible(true);
	    final Collection children = (Collection) getContentsMethod.invoke(parentObject,
		    (Object[]) null);
	    if (children != null && children.size() > 0) {
		final Iterator it = children.iterator();
		while (it.hasNext() && objectsAdded < MAX_NUMBER_ITEMS) {
		    final OjiObject child = (OjiObject) it.next();
		    objectsAdded++;
		    final int reportNumber = objectsAdded;
		    try {
			//			UI.getCurrent().access(new Runnable() {
			//			    @Override
			//			    public void run() {
			//				status.setCaption("Adding object " + reportNumber + " with ID: " + ObjectiveUtil.getObjectId(child));
			//			    } 
			//			});		    
			final String className = child.getClass().getName();
			boolean canHaveChildren = false;
			final Field mObjectField = ObjectiveUtil.getField(child, "mObject"); 
			final ObjObject objObject = (ObjObject) mObjectField.get(child);
			addObject(objObject, parents, session);
			if (className.equalsIgnoreCase("com.objective.objapi.ObjApiPhysicalFile")
				|| className.equalsIgnoreCase("com.objective.objapi.ObjApiFolder")) {
			    Map<String, ParentObjectDetails> childParents = new HashMap<>();
			    childParents.putAll(parents);
			    childParents.put(ObjectiveUtil.getObjectTypeDefn(child), ObjectiveUtil.getObjectDetails(child));
			    objectsAdded = addObjectsToObjectTree(child, childParents, objectsAdded, session);
			    canHaveChildren = true;
			} 
			setChildrenAllowed(child.getObjId(), canHaveChildren);
			setParent(child.getObjId(), parentObject.getObjId());
		    } catch (final Exception e) {
			logger.debug("Could not add object to tree of type: "
				+ child.getClass().getName());
		    }
		}
	    }
	} catch (final Exception e) {
	    logger.error(e);
	}
	logger.debug("End getObjectTree - getting object tree from Objective");
	return objectsAdded;
    }

    /**
     * Returns a copy of the container
     * 
     * @return	HierarchicalContainer
     */
    public HierarchicalContainer getCopy() {
	ObjectiveHierarchicalContainer newContainer = new ObjectiveHierarchicalContainer(server, this.rootObjectId, this.linkType, this.propTypes, this.linkMaxLength, this.previewHeight, 
		this.previewWidth, this.status);
	List<String> itemids = (List<String>)this.getItemIds();
	for (String itemId : itemids) {
	    ObjectiveTreeItemDetails objectItem = this.getObjectiveItems().get(itemId);
	    Item origItem = this.getItem(itemId);
	    Item newItem = newContainer.addItem(itemId);
	    Collection<String> propIds = (Collection<String>) origItem.getItemPropertyIds();
	    for (String id : propIds) {
		if (!id.equalsIgnoreCase("Name")) {
		    if (origItem.getItemProperty(id) != null && origItem.getItemProperty(id).getValue() != null) {
			newItem.getItemProperty(id).setValue(origItem.getItemProperty(id).getValue().toString());
		    }
		} else {
		    newItem.getItemProperty(id).setValue(getObjectHyperlink(objectItem));
		}
	    }
	    String parentId = (String)this.getParent(itemId);
	    if (parentId != null) {
		newContainer.setParent(itemId, parentId);
	    }
	    newContainer.setChildrenAllowed(itemId, this.areChildrenAllowed(itemId));
	    newContainer.getObjectiveItems().put(itemId, objectItem);
	}
	return newContainer;
    } 

    public int getLinkMaxLength() {
	return linkMaxLength;
    }

    public ObjectiveLinkType getLinkType() {
	return linkType;
    }

    /**
     * Returns a component that has a link to the Objective Objective. Depending on the linkType, this link will be either:
     * 
     * - link to OBR file download
     * - direct link to objective
     * - 
     * 
     * @param object
     * @return
     */
    @SuppressWarnings("serial")
    private Component getObjectHyperlink(final ObjectiveTreeItemDetails objectItem) {
	try {
	    final ThemeResource icon = new ThemeResource(objectItem.iconName);
	    if (linkType.equals(ObjectiveLinkType.OBR)) {
		// Create OBR download link
		final TableButton obrLink = new TableButton(objectItem.docName);
		obrLink.setIcon(icon);
		final StreamResource sr = ObjectiveUtil.getOBRFileStream(objectItem.objectId);
		final FileDownloader fileDownloader = new FileDownloader(sr);
		fileDownloader.extend(obrLink);
		obrLink.setStyleName(BaseTheme.BUTTON_LINK);
		return obrLink;
	    } else if (linkType.equals(ObjectiveLinkType.BrowserPreview)) {
		// Create web browser preview which opens up a preview window.
		final TableButton browserPreviewLink = new TableButton(objectItem.docName);
		browserPreviewLink.setIcon(icon);
		browserPreviewLink.addClickListener(new Button.ClickListener() {
		    @Override
		    public void buttonClick(ClickEvent event) {
			try {
			    if (ObjectiveUtil.getOjiSession(server).getObject(objectItem.objectId) instanceof OjiDocument) {
				UI.getCurrent()
				.addWindow(new DocPreviewWindow(objectItem, previewHeight, previewWidth));
			    }
			} catch (Exception e) {
			    logger.error(e);
			}
		    }
		});
		browserPreviewLink.setStyleName(BaseTheme.BUTTON_LINK);
		return browserPreviewLink;
	    } else {
		// Create direct link to Objective to access the object (WebTalk)
		TableLink objectiveLink = new TableLink(objectItem.docName, null);
		if (objectItem.isDocument) {
		    objectiveLink.setResource(new ExternalResource(objectItem.url));
		} else {
		    objectiveLink = new TableLink(objectItem.docName, null);
		}
		objectiveLink.setIcon(icon);
		objectiveLink.setDescription(objectItem.description);
		return objectiveLink;
	    } 
	} catch (final Exception e) {
	    logger.error(e);
	    return new Label("Error, could not generate hyperlink for unknown object");
	}
    }

    public HashMap<String, ObjectiveTreeItemDetails> getObjectiveItems() {
	return objectiveItems;
    }

    public String getPreviewHeight() {
	return previewHeight;
    }

    public String getPreviewWidth() {
	return previewWidth;
    }

    public List<ObjectivePropType> getPropTypes() {
	return propTypes;
    }

    public String getRootObjectId() {
	return rootObjectId;
    }

    public Label getStatus() {
	return status;
    }

    /**
     * Returns an ObjectiveTreeItemDetails object populated with the details of the Objective object
     * @param object	Object, either OjiObject or ObjObject based
     * @return ObjectiveTreeItemDetails
     */
    private ObjectiveTreeItemDetails getTreeItemDetails(Object object) {
	String url = "";
	String docName = "";
	String iconName = Constants.IMAGES_UNKNOWN_PNG;
	String description = "";
	String objectId = "";
	boolean isDocument = false;

	ObjectiveTreeItemDetails itemDetails = new ObjectiveTreeItemDetails();
	try {
	    if (object instanceof ObjDocument) {
		final ObjDocument doc = (ObjDocument) object;
		objectId = doc.getObjId();
		docName = ObjectiveUtil.getDocName(doc.getName(),
			doc.getLastPublishedVersion().getFileSize(), linkMaxLength);
		iconName = ObjectiveUtil.getDocIconName(doc.getLastPublishedVersion().getFileType());
		description = doc.getCommentText();
		isDocument = true;
	    } else if (object instanceof OjiDocument) {
		final OjiDocument doc = (OjiDocument) object;
		objectId = doc.getObjId();
		docName = ObjectiveUtil.getDocName(doc.getName(),
			doc.getLastPublishedVersion().getFileSize(), linkMaxLength);
		iconName = ObjectiveUtil.getDocIconName(doc.getLastPublishedVersion().getFileType());
		description = doc.getCommentText();
		isDocument = true;
	    } else if (object instanceof OjiFolder) {
		final OjiFolder folder = (OjiFolder) object;
		objectId = folder.getObjId();
		docName = ObjectiveUtil.getTruncatedObjectName(folder.getName(), linkMaxLength);
		iconName = ObjectiveUtil.getFolderIconName(folder.getTypeDefinition().getSingularName());
		description = docName;
	    } else if (object instanceof ObjFolder) {
		final ObjFolder folder = (ObjFolder) object;
		objectId = folder.getObjId();
		docName = ObjectiveUtil.getTruncatedObjectName(folder.getName(), linkMaxLength);
		iconName = ObjectiveUtil.getFolderIconName(folder.getTypeDefn().getName());
		description = docName;
	    } else if (object instanceof ObjPhysicalFile) {
		final ObjPhysicalFile file = (ObjPhysicalFile) object;
		objectId = file.getObjId();
		docName = ObjectiveUtil.getTruncatedObjectName(file.getName(), linkMaxLength);
		iconName = Constants.IMAGES_RCC_FILE_PNG;
	    } else if (object instanceof ObjAlias) {
		final ObjAlias alias = (ObjAlias) object;
		objectId = alias.getObjId();
		docName = ObjectiveUtil.getTruncatedObjectName(alias.getName(), linkMaxLength);
		if (alias.getOriginal() instanceof ObjDocument) {
		    final ObjDocument doc = (ObjDocument) alias.getOriginal();
		    url = ObjectiveUtil.getObjectDownloadLink(doc.getObjId());
		    docName = ObjectiveUtil.getDocName(doc.getName() + " (Alias)", doc
			    .getLastPublishedVersion().getFileSize(), linkMaxLength);
		    iconName = ObjectiveUtil.getDocIconName(doc.getLastPublishedVersion().getFileType());
		    description = doc.getCommentText();
		    isDocument = true;
		} else if (alias.getOriginal() instanceof ObjFolder) {
		    final ObjFolder folder = (ObjFolder) alias.getOriginal();
		    objectId = folder.getObjId();
		    docName = ObjectiveUtil.getTruncatedObjectName(folder.getName() + " (Alias)", linkMaxLength);
		    iconName = ObjectiveUtil.getFolderIconName(folder.getTypeDefn().getName());
		    description = docName;
		}
	    }
	    if (StringUtils.isBlank(url)) { // URL is only set if object was an alias
		url = ObjectiveUtil.getObjectDownloadLink(objectId);  
	    }
	    itemDetails = new ObjectiveTreeItemDetails(objectId, url, docName, iconName, description, isDocument);
	} catch (Exception e) {
	    logger.error(e);
	}
	return itemDetails;
    }

    /**
     * Populates the container with objects from Objective
     * 
     * @throws Exception
     */
    public void init() throws Exception {
	logger.debug("Start getObjectTree - getting object tree from Objective");
	final OjiSession session = ObjectiveUtil.getOjiSession(server);
	final OjiObject rootObject = ObjectiveUtil.getOjiSession(server).getObject(rootObjectId);
	if (rootObject == null) {
	    throw new Exception("Could not find Object with ID: " + rootObjectId);
	}
	final Field mObjectField = ObjectiveUtil.getField(rootObject, "mObject"); 
	final ObjObject objObject = (ObjObject) mObjectField.get(rootObject);
	Map<String, ParentObjectDetails> parents = new HashMap<>();
	List<OjiObject> ancestors = ObjectiveUtil.getParents(objObject, ObjectiveUtil.getOjiSession(server));
	for (OjiObject ancestor : ancestors) {
	    parents.put(ObjectiveUtil.getObjectTypeDefn(ancestor), ObjectiveUtil.getObjectDetails(ancestor));
	}
	addObject(objObject, parents, session);
	parents.put(ObjectiveUtil.getObjectTypeDefn(objObject), ObjectiveUtil.getObjectDetails(objObject));
	addObjectsToObjectTree(rootObject, parents, 0, session);
	logger.debug("End getObjectTree - finished getting object tree from Objective");
    }

    public void setLinkMaxLength(int linkMaxLength) {
	this.linkMaxLength = linkMaxLength;
    }

    public void setLinkType(ObjectiveLinkType linkType) {
	this.linkType = linkType;
    }

    public void setObjectiveItems(HashMap<String, ObjectiveTreeItemDetails> objectiveItems) {
	this.objectiveItems = objectiveItems;
    }

    public void setPreviewHeight(String previewHeight) {
	this.previewHeight = previewHeight;
    }

    public void setPreviewWidth(String previewWidth) {
	this.previewWidth = previewWidth;
    }

    public void setPropTypes(List<ObjectivePropType> propTypes) {
	this.propTypes = propTypes;
    }

    public void setRootObjectId(String rootObjectId) {
	this.rootObjectId = rootObjectId;
    }

    public void setStatus(Label status) {
	this.status = status;
    }


}
