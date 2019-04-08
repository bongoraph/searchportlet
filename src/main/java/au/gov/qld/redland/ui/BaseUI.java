package au.gov.qld.redland.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import javax.portlet.ValidatorException;

import org.apache.commons.lang3.StringUtils;

import au.gov.qld.redland.objective.FolderUI;
import au.gov.qld.redland.objective.util.OjiServerException;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.objective.oji.OjiException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.UserError;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinPortletSession;
import com.vaadin.server.VaadinPortletSession.PortletListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Base UI Class which provides core functionality for accessing Objective API,
 * Portlet preferences and portlet modes.
 * 
 * It supports the 3 portlet modes: edit, view and help.
 * 
 * @author danielma
 * 
 */
public abstract class BaseUI extends UI implements PortletListener {

    private static final String MSG_ERROR_GENERAL = "Please contact your web site administrator. The following error occurred: ";
    protected static Log logger = LogFactoryUtil.getLog(BaseUI.class.getName());
    private static final String MSG_CORRECT_ERROR = "Correct the error before saving again.";
    private static final String MSG_CORRECT_ERRORS = "Correct the errors before saving again.";
    private static final String MSG_INVALID_VALUE_ENTERED = "Invalid value entered";
    private static final String MSG_INVALID_VALUES_ENTERED = "Invalid Values entered";
    private static final String MSG_PORTLET_PREFERENCES_NOT_SET = "The Portlet's preferences need to be set, got to the Portlet Preferences and set the mandatory fields.";
    private static final String MSG_SAVED_PREFS = "Saved settings successfully";
    private static final long serialVersionUID = -6493432402138724097L;
    protected Layout editLayout;
    protected Layout helpLayout;
    protected Layout viewLayout;
    protected boolean savedPreferences;
    protected PortletMode mode = PortletMode.VIEW;

    /**
     * Adds an error message to the given Button
     * 
     * @param component
     *        to add error message
     * @param errorStr error message
     */
    protected void addComponentError(Button component, String errorStr) {
	component.setComponentError(new UserError(errorStr));
    }

    /**
     * Adds an error message to the given form
     * 
     * @param form
     *        to add error message
     * @param errorStr error message
     */
    protected void addFormError(FormLayout component, String errorStr) {
	component.setComponentError(new UserError(errorStr));
    }
    
    /**
     * Adds an error message to the given component
     * 
     * @param component
     *        to add error message
     * @param errorStr error message
     */
    protected void addComponentError(TextField component, String errorStr) {
	component.setComponentError(new UserError(errorStr));
    }
    
    /**
     * Adds an error message to the given component reflecting the nature of the
     * exception.
     * 
     * @param component
     *        to add error message
     * @param e
     *        Cause of error
     */
    protected void addComponentError(Component component, Exception e) {
	String errorStr = MSG_ERROR_GENERAL + e.getMessage();
	final Label msg = new Label(errorStr);
	msg.setComponentError(new UserError(errorStr));
    }
    
    /**
     * Adds an error message to the given layout.
     * 
     * @param layout
     *        to add error message
     * @param errorStr Error message to display
     *        Cause of error
     */
    protected void addError(Layout layout, String errorStr) {
	final Label msg = new Label(errorStr);
	msg.setComponentError(new UserError(errorStr));
	layout.removeAllComponents();
	layout.addComponent(msg);
    }
    
    /**
     * Adds an error message to the given layout reflecting the nature of the
     * exception.
     * 
     * @param layout
     *        to add error message
     * @param e
     *        Cause of error
     */
    protected void addError(Layout layout, Exception e) {
	String errorStr = MSG_ERROR_GENERAL + e.getMessage();
	final Label msg = new Label(errorStr);
	msg.setComponentError(new UserError(errorStr));
	layout.removeAllComponents();
	layout.addComponent(msg);
    }

    /**
     * Adds an error message to the ViewLayout to indicate that the Portlet
     * preferences have not been set.
     * 
     */
    protected void addPortletPrefsNotSetError() {
	final Label msg = new Label(MSG_PORTLET_PREFERENCES_NOT_SET);
	msg.setComponentError(new UserError(MSG_PORTLET_PREFERENCES_NOT_SET));
	viewLayout.removeAllComponents();
	viewLayout.addComponent(msg);
    }

    /**
     * Converts an Exception to a human readable message.
     * 
     * @param e
     *        Exception to convert
     * @return String containing human readable message
     */
    protected String convertExceptionToMessage(Exception e) {
	if (e instanceof OjiException) {
	    return "A general Objective error occurred, are the preferences set?";
	} else if (e instanceof OjiServerException) {
	    return "Could not communicate with the Objective server.";
	} else {
	    return "A general Application error occurred, please contact the web administrator.";
	}
    }

    /**
     * Creates the Edit Layout
     */
    protected abstract void createEditLayout();

    /**
     * Helper method to create a preferences form with a save button for a given
     * set of fields.
     * 
     * Should be called from subclasses in the createEditLayout.
     * 
     * @param fields
     *        List of fields to use as preferences
     */
    protected void createEditLayoutForm(final PrefField... fields) {
	final PortletPreferences preferences = getPreferences();
	for (final PrefField field : fields) {
	    field.initWithPrefValue(preferences);
	}
	editLayout = new VerticalLayout();
	final FormLayout formLayout = new FormLayout();
	formLayout.addComponents(fields);
	final Button saveButton = new Button("Save");
	saveButton.addClickListener(new Button.ClickListener() {
	    private static final long serialVersionUID = 93221219443946169L;

	    @Override
	    public void buttonClick(ClickEvent event) {
		savePrefFields(fields);
	    }
	});
	editLayout.addComponent(formLayout);
	final HorizontalLayout footerLayout = new HorizontalLayout();
	footerLayout.addComponent(saveButton);
	footerLayout.setSizeFull();
	footerLayout.setComponentAlignment(saveButton, Alignment.BOTTOM_CENTER);
	editLayout.addComponent(footerLayout);
    }

    /**
     * Creates the Help Layout
     */
    protected abstract void createHelpLayout();

    /**
     * Creates the View Layout
     */
    protected abstract void createViewLayout();

    /**
     * Returns the Portlet preferences associated with the Portlet
     * 
     * @return
     */
    protected PortletPreferences getPreferences() {
	return VaadinPortletService.getCurrentPortletRequest().getPreferences();
    }

    /**
     * Returns if the Portlet preferences have been set
     * 
     * @return
     */
    protected boolean getPreferencesSet() {
	return getPreferences() != null;
    }

    /**
     * Returns the boolean value associated for a Portlet preference property
     * (key).
     * 
     * @param key
     *        String containing the property name
     * @param defaultValue
     *        Default boolean value
     * @return boolean value associated with preference
     */
    protected boolean getPrefsBoolValue(String key, boolean defaultValue) {
	if (getPreferences() != null) {
	    boolean booleanValue = defaultValue;
	    final String storedValue = getPreferences().getValue(key, "");
	    if (StringUtils.isNotBlank(storedValue)) {
		try {
		    booleanValue = Boolean.parseBoolean(storedValue);
		} catch (final Exception e) {
		}
	    }
	    return booleanValue;
	} else {
	    return defaultValue;
	}
    }

    /**
     * Returns the int value associated for a Portlet preference property (key).
     * 
     * @param key
     *        String containing the property name
     * @param defaultValue
     *        Default boolean value
     * @return int value associated with preference
     */
    protected int getPrefsIntValue(String key, int defaultValue) {
	if (getPreferences() != null) {
	    int intValue = defaultValue;
	    final String storedValue = getPreferences().getValue(key, "");
	    if (StringUtils.isNotBlank(storedValue)) {
		try {
		    intValue = Integer.parseInt(storedValue);
		} catch (final Exception e) {
		}

	    }
	    return intValue;
	} else {
	    return defaultValue;
	}
    }

    /**
     * Returns the String value associated for a Portlet preference property
     * (key).
     * 
     * @param key
     *        The property name
     * @param defaultValue
     *        Default boolean value
     * @return String value associated with preference
     */
    protected String getPrefsStringValue(String key, String defaultValue) {
	if (getPreferences() != null) {
	    return getPreferences().getValue(key, defaultValue);
	} else {
	    return defaultValue;
	}
    }

    /**
     * Returns the list of String values associated for a Portlet preference property
     * (key).
     * 
     * @param key
     *        The property name
     * @param defaultValues
     *        Default values
     * @return List<String> values associated with preference
     */
    protected List<String> getPrefsStringValues(String key, List<String> defaultValues) {
	if (getPreferences() != null) {
	    List<String> values = new ArrayList<>(); 
	    String delimitedValues = getPreferences().getValue(key, "");
	    if (delimitedValues != null && delimitedValues.length() > 2) {
		delimitedValues = delimitedValues.substring(1, delimitedValues.length()-1);
	    }
	    for (String value : delimitedValues.split(",")) {
		values.add(value.trim());
	    }
	    return values;
	} else {
	    return defaultValues;
	}
    }

    @Override
    public void handleActionRequest(ActionRequest request, ActionResponse response, UI uI) {
    }

    @Override
    public void handleEventRequest(EventRequest request, EventResponse response, UI uI) {
    }

    @Override
    public void handleRenderRequest(RenderRequest request, RenderResponse response, UI uI) {
    }

    /**
     * Gets called when the portlet is first loaded, ie. user navigates to page, when page is refreshed and when the user changes portlet mode.
     */
    @Override
    public void handleResourceRequest(ResourceRequest request, ResourceResponse response, UI uI) {
	logger.debug("handleResourceRequest");
	//http://dev.vaadin.com/ticket/12369
	if (UI.getCurrent() == null) {
	    UI.setCurrent(this);
	  }
	setPortletMode(request.getPortletMode());
    }

//    @Override
//    public void doInit(VaadinRequest request, int uiId, String embedId) {
//	logger.debug("doInit");
//	super.doInit(request, uiId, embedId);
//    }
    
    /**
     * Initialises the layouts for the different views that are supported by the portlet. 
     * 
     */
    @Override
    protected void init(VaadinRequest request) {
	final VaadinPortletSession portletSession = (VaadinPortletSession) VaadinSession
		.getCurrent();
	portletSession.addPortletListener(this);
	createViewLayout();
	createEditLayout();
	createHelpLayout();
	setContent(viewLayout);
	setSizeUndefined();
    }

    /**
     * Initialises the preferences fields from Portlet preferences, if any
     * 
     * @param fields
     *        List of fields to initialise
     */
    protected void initPrefFields(PrefField... fields) {
	final PortletPreferences preferences = getPreferences();
	if (preferences != null) {
	    for (final PrefField field : fields) {
		field.initWithPrefValue(preferences);
	    }
	}
    }

    /**
     * Refresh the content of the Edit Layout
     */
    protected abstract void refreshEditLayout();

    /**
     * Refresh the content of the Help Layout
     */
    protected abstract void refreshHelpLayout();

    // @Override
    // public void handleResourceRequest(ResourceRequest request,
    // ResourceResponse response, Window window) {
    // setPortletMode(request.getPortletMode());
    // }

    /**
     * Refresh the content of the View Layout
     */
    protected abstract void refreshViewLayout();

    /**
     * Validates and saves the preferences fields to the Portlet preferences.
     * 
     * 
     * @param fields
     *        List of fields to save
     * 
     * @throws ReadOnlyException
     */
    protected void savePrefFields(PrefField... fields) {
	try {
	    int validationErrorCount = 0;

	    for (final PrefField field : fields) {
		try {
		    field.validate();
		} catch (final InvalidValueException e) {
		    validationErrorCount++;
		}
	    }
	    if (validationErrorCount == 1) {
		Notification.show(MSG_INVALID_VALUE_ENTERED, MSG_CORRECT_ERROR,
			Notification.Type.WARNING_MESSAGE);
	    } else if (validationErrorCount > 1) {
		Notification.show(MSG_INVALID_VALUES_ENTERED, MSG_CORRECT_ERRORS,
			Notification.Type.WARNING_MESSAGE);
	    } else {
		final PortletPreferences preferences = getPreferences();
		for (final PrefField field : fields) {
		    field.storePref(preferences);
		}
		updatePrefs();
		savedPreferences = true;
		Notification.show(MSG_SAVED_PREFS, Notification.Type.HUMANIZED_MESSAGE);
	    }
	} catch (final ReadOnlyException | ValidatorException | IOException e) {
	    logger.error(e);
	}
    }

    /**
     * Sets the display to reflect the current portlet mode.
     * 
     * Before the correct layout is displayed, it is refreshed by calling
     * the appropriate refreshXXXLayout method.
     * 
     * @param mode
     *        Mode of portlet, i.e. edit, view, help.
     */
    protected void setPortletMode(PortletMode mode) {
	logger.debug("setPortletMode: " + mode + " UI: " + UI.getCurrent());
	this.mode = mode;
	if (mode == null) {
	    this.mode = PortletMode.VIEW;
	}
	if (this.mode == PortletMode.EDIT) {
	    if (getContent() != editLayout) {
		refreshEditLayout();
		setContent(editLayout);
	    }
	} else if (this.mode == PortletMode.HELP) {
	    refreshHelpLayout();
	    setContent(helpLayout);
	} else {
	    // (mode == PortletMode.VIEW)
	    if (getContent() != viewLayout) {
		refreshViewLayout();
		setContent(viewLayout);
		setSizeFull();
	    }
	}
    }

    /**
     * Sets a Portlet's preference boolean value.
     * 
     * @param key
     *        The property name
     * @param value
     *        to set for property
     * @Exception ReadOnlyException
     */
    protected void setPrefsBoolValue(String key, Boolean value) throws ReadOnlyException {
	getPreferences().setValue(key, value.toString());
    }

    /**
     * Sets a Portlet's preference int value.
     * 
     * @param key
     *        The property name
     * @param value
     *        to set for property
     * @Exception ReadOnlyException
     */

    protected void setPrefsIntValue(String key, int value) throws ReadOnlyException {
	getPreferences().setValue(key, Integer.toString(value));
    }

    /**
     * Sets a Portlet's preference boolean value.
     * 
     * @param key
     *        The property name
     * @param value
     *        to set for property
     * @Exception ReadOnlyException
     */
    protected void setPrefsStringValue(String key, String value) throws ReadOnlyException {
	getPreferences().setValue(key, value);
    }

    /**
     * Saves the Portlet preferences.
     * 
     * @throws ValidatorException
     * @throws IOException
     */
    protected void updatePrefs() throws ValidatorException, IOException {
	getPreferences().store();
    }
    
    /**
     * Gets the inbuild Liferay cache for these portlets
     * 
     * @return
     */
    protected PortalCache<Serializable, Serializable> getLRCache() {
	final PortalCache<Serializable, Serializable> cache = MultiVMPoolUtil
		.getCache("Objective-Cache");
	return cache;
    }

}