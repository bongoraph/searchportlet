package au.gov.qld.redland.objective;

import java.io.File;

import au.gov.qld.redland.objective.util.ObjectiveTreeItemDetails;
import au.gov.qld.redland.objective.util.ObjectiveUtil;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.objective.client.ObjAlias;
import com.objective.client.ObjDocument;
import com.objective.oji.OjiAlias;
import com.objective.oji.OjiDocument;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * A windows that displays the contains of an Objective Document. The document can be of type ObjDocument or OjiDocument.
 * 
 * It uses the ObjectiveUtil to get a local preview copy of the Objective Document, see it's JavaDoc for
 * details. Note that Word documents are previewed as PDF files, and Excel documents are previewed as HTML
 * documents because the browser cannot preview them in their native format.
 * 
 * @author danielma
 *
 */
@SuppressWarnings("serial")
@Theme("liferay")
public class DocPreviewWindow extends Window {
    private static Log logger = LogFactoryUtil.getLog(DocPreviewWindow.class
	    .getName());

    /**
     * Constructor
     * 
     * @param document  To display
     * @param height	Height of preview
     * @param width	Width of preview
     */
    public DocPreviewWindow(ObjectiveTreeItemDetails objectItem, String height, String width) {
	setCaption(objectItem.docName);
	setResizable(true);
	setWidth(width);
	setHeight(height);
	center();
	VerticalLayout mainLayout = new VerticalLayout();
	mainLayout.setSizeFull();
	try {
	    String filePath = ObjectiveUtil.getDocPath(objectItem.objectId, ObjectiveUtil.getOjiPRDSession());
	    if (filePath != null) {
		FileResource resource = new FileResource(new File(filePath));
		logger.info("Displaying Objective Document Id: " + objectItem.objectId + " with path: " + filePath);
		Embedded embeddedDoc = new Embedded(null, resource);
		embeddedDoc.setType(Embedded.TYPE_BROWSER);
		embeddedDoc.setHeight("99%");
		embeddedDoc.setWidth("99%");
		mainLayout.addComponent(embeddedDoc);
	    }	
	} catch (Exception e) {
	    logger.error(e);
	    mainLayout.addComponent(new Label("A general Application error occurred, please contact the web administrator."));
	}
	setContent(mainLayout);
    }

}