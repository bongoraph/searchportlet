package au.gov.qld.redland.objective.util;

import au.gov.qld.redland.objective.FolderUI;

import com.liferay.portal.model.PortletPreferences;

/**
 * Util class with generic helper methods
 * 
 * @author danielma
 * 
 */
public class Util {

    public static String getCurrentMethod() {
	final Thread current = Thread.currentThread();
	final StackTraceElement[] stack = current.getStackTrace();
	for (final StackTraceElement element : stack) {
	    if (!element.isNativeMethod()) {
		return element.getMethodName();
	    }
	}
	return "unknown";
    }

    public static String getLiferayPortletPreferenceValue(PortletPreferences pref, String key,
	    String defaultValue) {
	final String preferences = pref.getPreferences();
	final String valueStartStr = "<value>";
	final String valueEndStr = "</value>";
	int start = preferences.indexOf(FolderUI.PREF_PARENT_NODE_ID);
	start = preferences.indexOf(valueStartStr, start) + valueStartStr.length();
	final int end = preferences.indexOf(valueEndStr, start);
	if (start > 0 && end > start) {
	    return preferences.substring(start, end);
	} else {
	    return defaultValue;
	}
    }

}
