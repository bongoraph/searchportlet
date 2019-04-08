package au.gov.qld.redland.objective;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.vaadin.haijian.ExcelExporter;

import au.gov.qld.redland.objective.util.ObjectiveLinkType;
import au.gov.qld.redland.objective.util.ObjectiveServer;
import au.gov.qld.redland.objective.util.ObjectiveUtil;
import au.gov.qld.redland.ui.BaseUI;

import com.objective.oji.OjiObject;
import com.objective.oji.OjiSession;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.AbstractSelect.ItemCaptionMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.TreeTable;
import com.vaadin.ui.TwinColSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Portlet which displays the documents for a specific Objective Folder/File/Divider.
 * 
 * It differs from the FolderUI portlet in that there is a text field to enter the folder ID form the main view of the portlet.
 * 
 * It does not have any preferences.
 * 
 * @author danielma
 * 
 */
@Theme("objective")
@Widgetset("au.gov.qld.redland.objective.AppWidgetSet")
public class FolderReportUI extends BaseUI {

    //    public static final String PORTLET_NAME = "ObjectiveFolderPortlet";
    //    public static final String PREF_PARENT_NODE_ID = "PARENT_ID";
    //    public static final String PREF_PARENT_NODE_ID_DEF = "";
    private static final long serialVersionUID = -7080706157336540568L;

    private Button generateReportBtn;
    private VerticalLayout reportLayout;
    private TreeTable objectTree;
    private TextField objectiveIDTF;
    private TwinColSelect propSelect;
    private ComboBox serverSelect;
    private FormLayout reportSettingsForm;
    private Panel reportSettingsPanel;
    private Panel reportPanel;
    private HorizontalSplitPanel mainPanel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createEditLayout() {
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
    @SuppressWarnings("serial")
    @Override
    protected void createViewLayout() {
	viewLayout = new VerticalLayout();
	reportLayout = new VerticalLayout();
	reportSettingsPanel = new Panel("Report Options");
	reportSettingsPanel.setIcon(FontAwesome.COGS);
	reportPanel = new Panel("Report Results");
	reportPanel.setIcon(FontAwesome.TABLE);
	reportPanel.setContent(reportLayout);
	createReportSettingsForm();
	reportSettingsPanel.setContent(reportSettingsForm);
	mainPanel = new HorizontalSplitPanel();
	mainPanel.setHeight("700px");
	mainPanel.setFirstComponent(reportSettingsPanel);
	mainPanel.setSecondComponent(reportPanel);
	mainPanel.setSplitPosition(400, Unit.PIXELS);
	viewLayout.addComponent(mainPanel);
    }

    /**
     * Creates the controls used to capture the report details
     */
    private void createReportSettingsForm() {
	propSelect = new TwinColSelect("Properties");
	propSelect.addItems(ObjectivePropType.getAllPropLabels());
	reportSettingsForm = new FormLayout();	
	reportSettingsForm.addComponent(propSelect);
	createServerSelectLB();
	createObjectiveIDTF();
	createGenerateReportBtn();
	reportSettingsForm.addComponent(serverSelect);
	reportSettingsForm.addComponent(objectiveIDTF);
	reportSettingsForm.addComponent(generateReportBtn);
    }

    /**
     * Creates the "generate report" button.
     */
    private void createGenerateReportBtn() {
	generateReportBtn = new Button("Generate Report");
	generateReportBtn.addClickListener(new Button.ClickListener() {
	    @Override
	    public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
		reportSettingsForm.setComponentError(null);
		objectiveIDTF.setComponentError(null);
		generateReportBtn.setComponentError(null);
		objectiveIDTF.validate();
		if (objectiveIDTF.isValid()) {
		    try {
			ObjectiveServer server  = (ObjectiveServer)serverSelect.getValue();
			OjiSession session = ObjectiveUtil.getOjiSession(server);
			OjiObject object = session.getObject(objectiveIDTF.getValue());
			if (object == null) {
			    addComponentError(objectiveIDTF, "Could not find Object with ID: " + objectiveIDTF.getValue());
			} else {
			    objectiveIDTF.setEnabled(false);
			    generateReportBtn.setEnabled(false);
			    initObjectTree(StringUtils.strip(objectiveIDTF.getValue()), (Collection<String>)propSelect.getValue());
			}
		    } catch (Exception e) {
			addComponentError(reportSettingsForm, e);
		    }		    
		}
	    }
	});
    }

    /**
     * Creates the Objective Server combobox used to select the server from which to extract report.
     */
    private void createServerSelectLB() {
	serverSelect = new ComboBox("Server");
	serverSelect.addItem(ObjectiveServer.PRD);
	serverSelect.addItem(ObjectiveServer.UAT);
	serverSelect.setNullSelectionAllowed(false);
	serverSelect.select(ObjectiveServer.PRD);
    }

    /**
     * Creates the text field used to capture the objective ID.
     */
    private void createObjectiveIDTF() {
	objectiveIDTF = new TextField("Objective ID");
	objectiveIDTF.setRequired(true);
	objectiveIDTF.setInputPrompt("eg. qA3326, fA12345");
	objectiveIDTF.addValidator(new RegexpValidator("[a-zA-Z]{1,3}[0-9]{2,7}",
		"The Objective ID must only contain alphanumerical characters and must be between 5 and 8 characters long."));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void init(VaadinRequest request) {
	super.init(request);
    }

    /**
     * Populates the Links layout with a tree table containing links to the
     * documents in objective which belong to the selected Folder ID.
     * 
     */
    private void initObjectTree(final String parentNodeId, Collection<String> propertyList) {
	if (StringUtils.isNotBlank(parentNodeId)) {
	    final List<ObjectivePropType> properties = ObjectivePropType.getPropTypes(propertyList);
	    viewLayout.removeAllComponents();
	    final ProgressBar progress = new ProgressBar();
	    progress.setIndeterminate(true);
	    progress.setEnabled(true);
	    final VerticalLayout vl = new VerticalLayout();
	    final HorizontalLayout statusLayout = new HorizontalLayout();
	    statusLayout.addComponent(progress);
	    final Label status = new Label();
	    statusLayout.addComponent(status);
	    vl.addComponent(new Label("Generating Report Table for Objective item: " + parentNodeId));
	    vl.addComponent(statusLayout);
	    viewLayout.addComponent(vl);

	    // A thread to do some work
	    class WorkThread extends Thread {
		// Volatile because read in another thread in access()

		@Override
		public void run() {
		    try {
			objectTree = ObjectiveUtil.createObjectTreeTable((ObjectiveServer)serverSelect.getValue(), parentNodeId,
				ObjectiveLinkType.OBR, properties, 100, "", "",
				"200px", status);
		    } catch (final Exception e) {
			addError(viewLayout, e);
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
			    displayReport();			    
			    progress.setEnabled(true);
			    viewLayout.addComponent(mainPanel);
			}
		    });
		}
	    }
	    final WorkThread thread = new WorkThread();
	    thread.start();

	    UI.getCurrent().setPollInterval(500);
	} else {
	    objectiveIDTF.setEnabled(true);
	    generateReportBtn.setEnabled(true);
	}

    }

    /**
     * Displays the Objective Object Tree and Export to Excel button on the right panel.
     */
    private void displayReport() {
	viewLayout.removeAllComponents();
	reportLayout.removeAllComponents();
	final ExcelExporter exportExcelBtn = new ExcelExporter(objectTree);
	exportExcelBtn.setCaption("");
	exportExcelBtn.setIcon(new ThemeResource("images/export_excel_16.png"));
	exportExcelBtn.setDescription("Export results to Excel");
	viewLayout.addComponent(mainPanel);
	reportLayout.setSizeFull();
	reportLayout.addComponent(exportExcelBtn);
	objectTree.setSizeFull();
	objectTree.setPageLength(30);
	reportLayout.addComponent(objectTree);
	objectiveIDTF.setEnabled(true);
	generateReportBtn.setEnabled(true);
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
    }

}