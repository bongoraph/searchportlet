package au.gov.qld.redland.objective;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import au.gov.qld.redland.objective.util.ObjectiveUtil;
import au.gov.qld.redland.ui.BaseUI;
import au.gov.qld.redland.ui.PosNumberTF;
import au.gov.qld.redland.ui.PrefCB;

import com.objective.oji.OjiDocument;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * Portlet that allows the searching of Objective documents which have been
 * linked to the web site. Linked documents are those that are contained by
 * Objective Folder Portlets in the web site. So documents, whose folders have
 * not been linked in a ObjectiveFolderPortet will not appear in the search
 * results. See ObjectiveUtil for more details.
 * 
 * @author danielma
 * 
 */
@SuppressWarnings({ "serial", "deprecation" })
@Theme("objective")
@Widgetset("au.gov.qld.redland.objective.AppWidgetSet")
public class DocumentSearchUI extends BaseUI {
    private static final String CAPTION_DISPLAY_EDRMS_LINK = "Display EDRMS Link";
    private static final String CAPTION_DOC_TABLE_HEIGHT = "Documents Table Height";
    private static final String CAPTION_LINK_LENGTH = "Link char length";
    private static final String CAPTION_PREVIEW_DOCS = "Preview Docs";
    private static final String CAPTION_PREVIEW_HEIGHT = "Preview Window Height";
    private static final String CAPTION_PREVIEW_WINDOW_WIDTH = "Preview Window Width";
    private static final String HINT_DOC_PREVIEW_HEIGHT = "Height of document preview window";
    private static final String HINT_DOC_PREVIEW_WIDTH = "Width of document preview window";
    private static final String HINT_LINK_CHAR_LIMIT = "Number of characters to display for link";
    private static final String HINT_TABLE_HEIGHT = "Height in pixels of documents table";
    public static final String PORTLET_NAME = "ObjectiveFolderPortlet";
    private static final int PREF_DEFAULT_LINK_LENGTH = 30;
    private static final String PREF_DISPLAY_EDRMS_LINK = "DISPLAY_EDRMS_LINK";
    private static boolean PREF_DISPLAY_EDRMS_LINK_DEF = true;
    private static final String PREF_DOC_LIST_TBL_DEF_HEIGHT = "400";
    private static final String PREF_DOC_LIST_TBL_HEIGHT = "DOC_LIST_HEIGHT";
    private static final String PREF_DOC_PREVIEW_HEIGHT = "DOC_PREVIEW_HEIGHT";
    private static final String PREF_DOC_PREVIEW_HEIGHT_DEF = "500";
    private static final String PREF_DOC_PREVIEW_WIDTH = "DOC_PREVIEW_WIDTH";
    private static final String PREF_DOC_PREVIEW_WIDTH_DEF = "400";
    private static final String PREF_LINK_LENGTH = "LINK_LENGTH";
    private static final String PREF_LINK_LENGTH_DEF = "10";
    public static final String PREF_PARENT_NODE_ID = "PARENT_ID";
    public static final String PREF_PARENT_NODE_ID_DEF = "";
    private static final String PREF_PREVIEW_DOCUMENTS = "PREVIEW_DOCUMENTS";
    private static boolean PREF_PREVIEW_DOCUMENTS_DEF = true;
    private static final String PROMPT_DOC_LIST_TBL_HEIGHT = "eg. 400";
    private static final String PROMPT_DOC_PREVIEW_HEIGHT = "e.g. 500";
    private static final String PROMPT_DOC_PREVIEW_WIDTH = "e.g. 300";
    private static final String PROMPT_LINK_LENGTH = "e.g. 20";
    private static final long serialVersionUID = 473164693327347963L;
    private PrefCB displayEDRMSLinkCB;
    private PosNumberTF docPreviewHeightTF;

    private PosNumberTF docPreviewWidthTF;
    private VerticalLayout fileResultsLayout;
    private TextField keywordsTextField;
    private PosNumberTF linkLengthTF;
    private PrefCB previewDocsCB;
    private PosNumberTF tblHeightTF;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEditLayout() {
	linkLengthTF = new PosNumberTF(CAPTION_LINK_LENGTH, HINT_LINK_CHAR_LIMIT, PREF_LINK_LENGTH,
		PREF_LINK_LENGTH_DEF, PROMPT_LINK_LENGTH, false, 3);
	displayEDRMSLinkCB = new PrefCB(CAPTION_DISPLAY_EDRMS_LINK, PREF_DISPLAY_EDRMS_LINK,
		PREF_DISPLAY_EDRMS_LINK_DEF);
	previewDocsCB = new PrefCB(CAPTION_PREVIEW_DOCS, PREF_PREVIEW_DOCUMENTS,
		PREF_PREVIEW_DOCUMENTS_DEF);
	tblHeightTF = new PosNumberTF(CAPTION_DOC_TABLE_HEIGHT, HINT_TABLE_HEIGHT,
		PREF_DOC_LIST_TBL_HEIGHT, PREF_DOC_LIST_TBL_DEF_HEIGHT, PROMPT_DOC_LIST_TBL_HEIGHT,
		false, 4);
	docPreviewHeightTF = new PosNumberTF(CAPTION_PREVIEW_HEIGHT, HINT_DOC_PREVIEW_HEIGHT,
		PREF_DOC_PREVIEW_HEIGHT, PREF_DOC_PREVIEW_HEIGHT_DEF, PROMPT_DOC_PREVIEW_HEIGHT,
		false, 4);
	docPreviewWidthTF = new PosNumberTF(CAPTION_PREVIEW_WINDOW_WIDTH, HINT_DOC_PREVIEW_WIDTH,
		PREF_DOC_PREVIEW_WIDTH, PREF_DOC_PREVIEW_WIDTH_DEF, PROMPT_DOC_PREVIEW_WIDTH,
		false, 4);
	createEditLayoutForm(linkLengthTF, displayEDRMSLinkCB, previewDocsCB, tblHeightTF,
		docPreviewHeightTF, docPreviewWidthTF);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createHelpLayout() {
	helpLayout = new VerticalLayout();
    }

    /**
     * {@inhertiDoc}
     */
    @Override
    protected void createViewLayout() {
	viewLayout = new VerticalLayout();
	fileResultsLayout = new VerticalLayout();
	keywordsTextField = new TextField();
	keywordsTextField.setInputPrompt("keywords...");
	final HorizontalLayout toolbar = new HorizontalLayout();
	final Button searchButton = new Button("Search");
	searchButton.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = 3228362176098374900L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		doSearch(keywordsTextField.getValue());
	    }
	});
	searchButton.setDescription("Search for keywords");
	final Button clearButton = new Button("Clear");
	clearButton.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = 1204649263642256714L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		keywordsTextField.setValue("");
		doSearch(keywordsTextField.getValue());
	    }
	});
	clearButton.setDescription("Clear search results");
	toolbar.addComponent(keywordsTextField);
	toolbar.addComponent(searchButton);
	toolbar.addComponent(clearButton);
	viewLayout.addComponent(toolbar);
	viewLayout.addComponent(fileResultsLayout);
    }

    /**
     * Does actual search and populates the Portlet view with the results.
     * 
     * @param keywords
     *        Search keywords
     */
    private void doSearch(String keywords) {
	fileResultsLayout.removeAllComponents();

	if (getPreferences() != null) {
	    try {
		if (!StringUtils.isBlank(keywords)) {
		    final List<String> parentIds = ObjectiveUtil.getUsedFolderIds();
		    final List<OjiDocument> documents = ObjectiveUtil.searchDocuments(keywords,
			    parentIds, ObjectiveUtil.getOjiPRDSession());
//		    if (getPrefsBoolValue(PREF_PREVIEW_DOCUMENTS, true)) {
//			fileResultsLayout.addComponent(ObjectiveUtil.createDocPreviewLinkTable(
//				documents,
//				getPrefsIntValue(PREF_LINK_LENGTH, PREF_DEFAULT_LINK_LENGTH),
//				getPrefsBoolValue(PREF_DISPLAY_EDRMS_LINK,
//					PREF_DISPLAY_EDRMS_LINK_DEF),
//				getPrefsStringValue(PREF_DOC_LIST_TBL_HEIGHT,
//					PREF_DOC_LIST_TBL_DEF_HEIGHT),
//				getPrefsStringValue(PREF_DOC_PREVIEW_HEIGHT,
//					PREF_DOC_PREVIEW_HEIGHT_DEF),
//				getPrefsStringValue(PREF_DOC_PREVIEW_WIDTH,
//					PREF_DOC_PREVIEW_WIDTH_DEF)));
//		    } else {
//			fileResultsLayout.addComponent(ObjectiveUtil.createDocNativeLinkTable(
//				documents,
//				getPrefsIntValue(PREF_LINK_LENGTH, PREF_DEFAULT_LINK_LENGTH),
//				getPrefsBoolValue(PREF_DISPLAY_EDRMS_LINK,
//					PREF_DISPLAY_EDRMS_LINK_DEF),
//				getPrefsStringValue(PREF_DOC_LIST_TBL_HEIGHT,
//					PREF_DOC_LIST_TBL_DEF_HEIGHT)));
//		    }
		}
	    } catch (final Exception e) {
		addError(fileResultsLayout, e);
	    }
	} else {
	    addPortletPrefsNotSetError();
	}

	// if (!StringUtils.isBlank(keywords)) {
	// try {
	// final List<String> parentIds = objectiveUtil.getUsedFolderIds();
	// final List<OjiDocument> documents =
	// objectiveUtil.searchDocuments(keywords,
	// parentIds);
	// if (documents != null && documents.size() > 0) {
	// fileResultsLayout.addComponent(new Label(documents.size()
	// + " matching document(s) found."));
	// for (final OjiDocument document : documents) {
	// fileResultsLayout.addComponent(objectiveUtil.createDocLink(document));
	// }
	// } else {
	// fileResultsLayout.addComponent(new Label("No results found"));
	// }
	// } catch (final Exception e) {
	// viewLayout.addComponent(new Label(convertExceptionToMessage(e)));
	// }
	// }
    }

    @Override
    protected void init(VaadinRequest request) {
	super.init(request);
	refreshViewLayout();
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
	// doSearch(getPreferences().getValue(DOCUMENT_ID, ""));
    }

}