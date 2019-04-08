package au.gov.qld.redland.objective;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.haijian.ExcelExporter;

import au.gov.qld.redland.objective.ui.ObjectiveIdTF;
import au.gov.qld.redland.objective.util.ObjectiveLinkType;
import au.gov.qld.redland.objective.util.ObjectiveServer;
import au.gov.qld.redland.objective.util.ObjectiveUtil;
import au.gov.qld.redland.ui.BaseUI;
import au.gov.qld.redland.ui.PosNumberTF;
import au.gov.qld.redland.ui.PrefCB;
import au.gov.qld.redland.ui.PrefCombo;
import au.gov.qld.redland.ui.PrefField.ObjectType;
import au.gov.qld.redland.ui.PrefListBuilder;
import au.gov.qld.redland.ui.PrefTF;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Portlet which displays the documents for a specific Objective Folder/File/Divider.
 * 
 * @author danielma
 * 
 */
@Theme("objective")
@Widgetset("au.gov.qld.redland.objective.AppWidgetSet")
public class FolderUI extends BaseUI {

    private static final String CAPTION_DISPLAY_HIERARCHY = "Display Hierarchy";
    private static final String CAPTION_DOC_LINK_TYPE = "Document Link Type";
    private static final String CAPTION_DOC_TABLE_HEIGHT = "Documents Table Height";
    private static final String CAPTION_LINK_LENGTH = "Link char length";
    private static final String CAPTION_OBJ_COLS_DISPLAY = "Objective Properties to display";
    private static final String CAPTION_OBJECTIVE_FILE_ID = "Objective File Id";
    private static final String CAPTION_PREVIEW_HEIGHT = "Preview Window Height";
    private static final String CAPTION_PREVIEW_WINDOW_WIDTH = "Preview Window Width";
    private static final String CAPTION_TITLE = "Window Title";
    private static final String HINT_DOC_LINK_TYPE = "Document link, this can be configured for different behaviour when the document is clicked.";
    private static final String HINT_DOC_PREVIEW_HEIGHT = "Height of document preview window";
    private static final String HINT_DOC_PREVIEW_WIDTH = "Width of document preview window";
    private static final String HINT_LINK_CHAR_LIMIT = "Number of characters to display for link";
    private static final String HINT_LIST_TITLE = "This is used to display a title for the documents listed.";
    private static final String HINT_OBJ_COLS_DISPLAY = "Select the Objective properties to display as columns in the table";
    private static final String HINT_OBJECTIVE_FILE_ID = "This is the Objective File Id which contains the documents to display, e.g. qA3326";
    private static final String HINT_TABLE_HEIGHT = "Height in pixels of documents table";
    public static final String PORTLET_NAME = "ObjectiveFolderPortlet";
    private static final int PREF_DEFAULT_LINK_LENGTH = 30;
    private static final String PREF_DISPLAY_HIERARCHY = "DISPLAY HIERARCHY";
    private static boolean PREF_DISPLAY_HIERARCHY_DEF = true;
    private static final String PREF_DOC_LINK_TYPE = "DOC_LINK_TYPE";
    private static final String PREF_DOC_LIST_TBL_DEF_HEIGHT = "400";
    private static final String PREF_DOC_LIST_TBL_HEIGHT = "DOC_LIST_HEIGHT";
    private static final String PREF_DOC_PREVIEW_HEIGHT = "DOC_PREVIEW_HEIGHT";
    private static final String PREF_DOC_PREVIEW_HEIGHT_DEF = "500";
    private static final String PREF_DOC_PREVIEW_WIDTH = "DOC_PREVIEW_WIDTH";
    private static final String PREF_DOC_PREVIEW_WIDTH_DEF = "400";
    private static final String PREF_LINK_LENGTH = "LINK_LENGTH";
    private static final String PREF_LINK_LENGTH_DEF = "40";
    private static final String PREF_OBJ_COLS_DISPLAY = "OBJ_COLS_DISPLAY";
    public static final String PREF_PARENT_NODE_ID = "PARENT_ID";
    public static final String PREF_PARENT_NODE_ID_DEF = "";
    private static final String PREF_TITLE = "PARENT_TITLE";
    private static final String PREF_TITLE_DEF = "";
    private static final String PROMPT_DOC_LIST_TBL_HEIGHT = "eg. 400";
    private static final String PROMPT_DOC_PREVIEW_HEIGHT = "e.g. 500";
    private static final String PROMPT_DOC_PREVIEW_WIDTH = "e.g. 300";
    private static final String PROMPT_LINK_LENGTH = "e.g. 20";
    private static final long serialVersionUID = -7080706157336540568L;

    private PrefCB displayHierarchyCB;
    private VerticalLayout objFolderLayout;
    private PosNumberTF docPreviewHeightTF;
    private PosNumberTF docPreviewWidthTF;
    private ObjectiveIdTF fileIdTF;
    private PosNumberTF linkLengthTF;
    private PrefCombo linkTypeCombo;
    private TreeTable objectTree;
    private PosNumberTF tblHeightTF;
    private PrefTF titleTF;
    private PrefListBuilder twinColSelect;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEditLayout() {
	titleTF = new PrefTF(CAPTION_TITLE, HINT_LIST_TITLE, PREF_TITLE, PREF_TITLE_DEF, false, 50,
		ObjectType.STRING);
	fileIdTF = new ObjectiveIdTF(CAPTION_OBJECTIVE_FILE_ID, HINT_OBJECTIVE_FILE_ID,
		PREF_PARENT_NODE_ID, PREF_PARENT_NODE_ID_DEF, true);
	linkLengthTF = new PosNumberTF(CAPTION_LINK_LENGTH, HINT_LINK_CHAR_LIMIT, PREF_LINK_LENGTH,
		PREF_LINK_LENGTH_DEF, PROMPT_LINK_LENGTH, false, 3);
	displayHierarchyCB = new PrefCB(CAPTION_DISPLAY_HIERARCHY, PREF_DISPLAY_HIERARCHY,
		PREF_DISPLAY_HIERARCHY_DEF);
	tblHeightTF = new PosNumberTF(CAPTION_DOC_TABLE_HEIGHT, HINT_TABLE_HEIGHT,
		PREF_DOC_LIST_TBL_HEIGHT, PREF_DOC_LIST_TBL_DEF_HEIGHT, PROMPT_DOC_LIST_TBL_HEIGHT,
		false, 4);
	docPreviewHeightTF = new PosNumberTF(CAPTION_PREVIEW_HEIGHT, HINT_DOC_PREVIEW_HEIGHT,
		PREF_DOC_PREVIEW_HEIGHT, PREF_DOC_PREVIEW_HEIGHT_DEF, PROMPT_DOC_PREVIEW_HEIGHT,
		false, 4);
	docPreviewWidthTF = new PosNumberTF(CAPTION_PREVIEW_WINDOW_WIDTH, HINT_DOC_PREVIEW_WIDTH,
		PREF_DOC_PREVIEW_WIDTH, PREF_DOC_PREVIEW_WIDTH_DEF, PROMPT_DOC_PREVIEW_WIDTH,
		false, 4);
	final List<String> docLinkValues = new ArrayList<>();
	for (final ObjectiveLinkType type : ObjectiveLinkType.values()) {
	    docLinkValues.add(type.getLabel());
	}
	linkTypeCombo = new PrefCombo(CAPTION_DOC_LINK_TYPE, HINT_DOC_LINK_TYPE,
		PREF_DOC_LINK_TYPE, ObjectiveLinkType.BrowserPreview.getLabel(), docLinkValues,
		true, ObjectType.STRING);
	final List<String> objPropertyValues = new ArrayList<>();
	for (final ObjectivePropType type : ObjectivePropType.values()) {
	    objPropertyValues.add(type.getLabel());
	}
	twinColSelect = new PrefListBuilder(CAPTION_OBJ_COLS_DISPLAY, HINT_OBJ_COLS_DISPLAY,
		PREF_OBJ_COLS_DISPLAY, "", objPropertyValues, false, ObjectType.STRING);
	createEditLayoutForm(titleTF, fileIdTF, linkLengthTF, displayHierarchyCB, tblHeightTF,
		docPreviewHeightTF, docPreviewWidthTF, linkTypeCombo, twinColSelect);
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
	viewLayout.removeAllComponents();
	objFolderLayout = new VerticalLayout();
	viewLayout.addComponent(objFolderLayout);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
	super.init(request);
	if (mode == PortletMode.VIEW) {
	    initObjectTree();
	}
    }

    /**
     * Populates the Links layout with a tree table containing links to the
     * documents in objective which belong to the selected Folder ID.
     * 
     */
    private void initObjectTree() {
	final PortletPreferences preferences = getPreferences();
	final String parentNodeId = getPrefsStringValue(PREF_PARENT_NODE_ID, "");
	final ObjectiveLinkType linkType = ObjectiveLinkType.getLinkType(getPrefsStringValue(
		PREF_DOC_LINK_TYPE, ObjectiveLinkType.BrowserPreview.getLabel()));
	final int linkLength = getPrefsIntValue(PREF_LINK_LENGTH, PREF_DEFAULT_LINK_LENGTH);
	final String tableHeight = getPrefsStringValue(PREF_DOC_LIST_TBL_HEIGHT,
		PREF_DOC_LIST_TBL_DEF_HEIGHT);
	final String docPreviewHeight = getPrefsStringValue(PREF_DOC_PREVIEW_HEIGHT,
		PREF_DOC_PREVIEW_HEIGHT_DEF);
	final String docPreviewWidth = getPrefsStringValue(PREF_DOC_PREVIEW_WIDTH,
		PREF_DOC_PREVIEW_WIDTH_DEF);
	final List<ObjectivePropType> properties = ObjectivePropType
		.getPropTypes(getPrefsStringValues(PREF_OBJ_COLS_DISPLAY, null));
	viewLayout.removeAllComponents();
	final ProgressBar progress = new ProgressBar();
	progress.setIndeterminate(true);
	progress.setEnabled(true);
	final VerticalLayout vl = new VerticalLayout();
	final HorizontalLayout hl = new HorizontalLayout();
	hl.addComponent(progress);
	final Label status = new Label();
	hl.addComponent(status);
	vl.addComponent(new Label("Generating Table for Objective item: " + parentNodeId));
	vl.addComponent(hl);
	viewLayout.addComponent(vl);

	// A thread to do some work
	class WorkThread extends Thread {
	    // Volatile because read in another thread in access()

	    @Override
	    public void run() {
		if (preferences != null && StringUtils.isNotBlank(parentNodeId)) {
		    try {
			objectTree = ObjectiveUtil.createObjectTreeTable(ObjectiveServer.PRD, parentNodeId,
				linkType, properties, linkLength, tableHeight,
				docPreviewHeight, docPreviewWidth, status);
		    } catch (final Exception e) {
			addError(objFolderLayout, e);
		    }
		} else {
		    addPortletPrefsNotSetError();
		}

		// Update the UI thread-safely
		UI.getCurrent().access(new Runnable() {
		    @Override
		    public void run() {
			// Restore the state to initial
			progress.setValue(new Float(0.0));
			progress.setEnabled(false);

			// Stop polling
			UI.getCurrent().setPollInterval(-1);
			viewLayout.removeAllComponents();
			objFolderLayout.removeAllComponents();
			objFolderLayout.addComponent(objectTree);
			viewLayout.addComponent(objFolderLayout);
			final ExcelExporter exportExcelBtn = new ExcelExporter(objectTree);
			exportExcelBtn.setCaption("");
			exportExcelBtn.setIcon(new ThemeResource("images/export_excel_16.png"));
			exportExcelBtn.setDescription("Export results to Excel");
			viewLayout.addComponent(exportExcelBtn);
			progress.setEnabled(true);
		    }
		});
	    }
	}
	final WorkThread thread = new WorkThread();
	thread.start();

	UI.getCurrent().setPollInterval(500);
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
	initObjectTree();
    }

}