package au.gov.qld.redland.objective;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

import au.gov.qld.redland.objective.ui.ObjectiveIdTF;
import au.gov.qld.redland.objective.util.ObjectiveUtil;
import au.gov.qld.redland.ui.BaseUI;
import au.gov.qld.redland.ui.PosNumberTF;
import au.gov.qld.redland.ui.PrefField.ObjectType;
import au.gov.qld.redland.ui.PrefTF;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.FileResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Portlet which displays a preview to a selected document in Objective.
 * 
 * @author danielma
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
@Theme("objective")
@Widgetset("au.gov.qld.redland.objective.AppWidgetSet")
public class DocumentViewUI extends BaseUI {

    private static final String HINT_DOC_TITLE = "This is the Title for the document that is embedded into a the page, can be blank.";
    private static final String HINT_DOC_WIDTH = "This is the width for the document in pixels";
    private static final String HINT_OBJECTIVE_FILE_ID = "This is the Objective File Id which contains the documents to display, e.g. qA3326";
    private static Log logger = LogFactoryUtil.getLog(FolderUI.class.getName());
    private static final String PREF_DOCUMENT_DEF_HEIGHT = "400";
    private static final String PREF_DOCUMENT_DEF_WIDTH = "400";
    private static final String PREF_DOCUMENT_HEIGHT = "DOCUMENT_HEIGHT";
    private static final String PREF_DOCUMENT_ID = "DOCUMENT_ID";
    private static final String PREF_DOCUMENT_TITLE = "DOCUMENT_TILE";
    private static final String PREF_DOCUMENT_TITLE_DEF = "";
    private static final String PREF_DOCUMENT_WIDTH = "DOCUMENT_WIDTH";
    private static final String PROMPT_DOC_WIDTH = "e.g. 300";
    private static final String PROMPT_DOCUMENT_HEIGHT = "e.g. 400";
    private static final long serialVersionUID = -5244484490003840602L;
    private PosNumberTF docHeightTextField;
    private ObjectiveIdTF docIdTextField;
    private PrefTF docTitleTextField;
    private PosNumberTF docWidthTextField;

    private Embedded embeddedDocument;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEditLayout() {
	editLayout = new VerticalLayout();
	docTitleTextField = new PrefTF("Window Title", HINT_DOC_TITLE, PREF_DOCUMENT_TITLE,
		PREF_DOCUMENT_TITLE_DEF, false, 30, ObjectType.STRING);
	docIdTextField = new ObjectiveIdTF("Document ID", HINT_OBJECTIVE_FILE_ID, PREF_DOCUMENT_ID,
		"", true);
	docWidthTextField = new PosNumberTF("Window width", HINT_DOC_WIDTH, PREF_DOCUMENT_WIDTH,
		PREF_DOCUMENT_DEF_WIDTH, PROMPT_DOC_WIDTH, false, 10);
	docHeightTextField = new PosNumberTF("Window height", HINT_DOC_WIDTH, PREF_DOCUMENT_HEIGHT,
		PREF_DOCUMENT_DEF_HEIGHT, PROMPT_DOCUMENT_HEIGHT, false, 10);
	createEditLayoutForm(docTitleTextField, docIdTextField, docWidthTextField,
		docHeightTextField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createHelpLayout() {
	helpLayout = new VerticalLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createViewLayout() {
	viewLayout = new VerticalLayout();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
	super.init(request);
	initDocument();
    }

    /**
     * Initialises the portlet view to display a preview of the selected
     * Objective document.
     * 
     */
    private void initDocument() {
	viewLayout.removeAllComponents();
	final String documentId = getPreferences().getValue(PREF_DOCUMENT_ID, "");
	if (getPreferences() != null && StringUtils.isNotBlank(documentId)) {
	    try {
		final String filePath = ObjectiveUtil.getDocPath(documentId, ObjectiveUtil.getOjiPRDSession());
		if (filePath != null) {
		    final FileResource resource = new FileResource(new File(filePath));
		    logger.debug("Displaying Objective Document Id: " + documentId + " with path: "
			    + filePath);
		    embeddedDocument = new Embedded(getPreferences().getValue(PREF_DOCUMENT_TITLE, null), resource);
		    embeddedDocument.setType(Embedded.TYPE_BROWSER);
		    embeddedDocument.setWidth("99%");
		    embeddedDocument.setHeight("99%");
		    viewLayout.addComponent(embeddedDocument);
		} else if (StringUtils.isBlank(documentId)) {
		    viewLayout.addComponent(new Label("Not document has been set to be displayed"));
		} else {
		    viewLayout.addComponent(new Label(
			    "Error occurrent retrieving document with id: " + documentId));
		}
	    } catch (final Exception e) {
		addError(viewLayout, e);
	    }
	} else {
	    addPortletPrefsNotSetError();
	}
	viewLayout
		.setWidth(getPreferences().getValue(PREF_DOCUMENT_WIDTH, PREF_DOCUMENT_DEF_WIDTH));
	viewLayout.setHeight(getPreferences().getValue(PREF_DOCUMENT_HEIGHT,
		PREF_DOCUMENT_DEF_HEIGHT));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void refreshEditLayout() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void refreshHelpLayout() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void refreshViewLayout() {
	initDocument();
    }

}