package au.gov.qld.redland.objective.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.ss.examples.html.ToHtml;
import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import au.gov.qld.redland.objective.FolderUI;
import au.gov.qld.redland.objective.ObjectivePropType;
import au.gov.qld.redland.objective.service.AppSettingsService;
import au.gov.qld.redland.objective.ui.ObjectiveHierarchicalContainer;

import com.liferay.portal.kernel.cache.MultiVMPoolUtil;
import com.liferay.portal.kernel.cache.PortalCache;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.service.PortletPreferencesLocalServiceUtil;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.objective.client.ObjDocument;
import com.objective.client.ObjObject;
import com.objective.client.ObjPrivAssignment;
import com.objective.client.ObjPrivilege;
import com.objective.client.ObjUser;
import com.objective.oji.OjiApplication;
import com.objective.oji.OjiCatalogueableObject;
import com.objective.oji.OjiDocVersion;
import com.objective.oji.OjiDocument;
import com.objective.oji.OjiException;
import com.objective.oji.OjiFolder;
import com.objective.oji.OjiObject;
import com.objective.oji.OjiPhysicalFile;
import com.objective.oji.OjiSearch;
import com.objective.oji.OjiSession;
import com.vaadin.data.util.HierarchicalContainer;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.TreeTable;

/**
 * Utility class for common Objective functions.
 * 
 * Objective objects returned from the API can be either of ObjObject or OjiObject.
 * 
 * It uses Liferay Caching for caching the methods' results.
 * 
 * @author danielma
 * 
 * @todo need to implement connections for different server types.
 * 
 * 
 *       if (object instanceof OjiObject) { OjiObject ojiObject =
 *       (OjiObject)object; id = ojiObject.getObjId(); } else if (object
 *       instanceof ObjDocument){ ObjDocument doc = (ObjDocument) object; id =
 *       doc.getObjId(); } else if (object instanceof OjiDocument) { OjiDocument
 *       doc = (OjiDocument)object; id = doc.getObjId(); } else if (object
 *       instanceof OjiFolder) { OjiFolder folder = (OjiFolder)object; id =
 *       folder.getObjId(); } else if (object instanceof ObjFolder) { ObjFolder
 *       folder = (ObjFolder)object; id = folder.getObjId(); } else if (object
 *       instanceof ObjPhysicalFile) { ObjPhysicalFile file =
 *       (ObjPhysicalFile)object; id = file.getObjId(); } else {
 *       logger.error("Unknown object type" + object.getClass().getName()); }
 * 
 */
public class ObjectiveUtil {

    /**
     * Session to Objective API
     */
    private static OjiSession apiSession;

    private static final String CACHE_KEY_DOCUMENTS = "documents";

    private static final String CACHE_KEY_OBJECT_TREE = "objectTree";

    /**
     * To keep track of the cached documents
     */
    private static Hashtable<String, String> cachedDocumentMap;

    /**
     * Expiry time for cached documents
     */
    public static final long DOCUMENT_CACHE_MS = 240000;
    /**
     * Name of temp folder for cached documents
     */
    public static final String DOCUMENT_TEMP_FOLDER = "objective";
    /**
     * Format for Dowload URL
     */
    private static String FILE_DOWNLOAD_URL = "https://SERVER_URL/id:DOCUMENT_ID/document/versions/latest";
    private static Log logger = LogFactoryUtil.getLog(ObjectiveUtil.class.getName());
    private static DateFormat sdf = new SimpleDateFormat("dd/MM/YYYY hh:mm");

    /**
     * Creation time for current Objective API.
     */
    private static long sessionCreateTime = 0;

    private static String objPrdServerURL;
    private static int objPrdServerPort;
    private static String objPrdServerUser;
    private static String objPrdServerPassword;
    private static int objPrdServerSessionTimeout;
    private static String objUatServerURL;
    private static int objUatServerPort;
    private static String objUatServerUser;
    private static String objUatServerPassword;
    private static int objUatServerSessionTimeout;

    static {
	cachedDocumentMap = new Hashtable<String, String>();
	try {
	    Properties appProperties = AppSettingsService.getProperties();
	    objPrdServerURL = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_PRD_URL).trim();
	    objPrdServerPort = Integer.parseInt(appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_PRD_PORT));
	    objPrdServerUser = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_PRD_USER).trim();
	    objPrdServerPassword = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_PRD_PASSWORD).trim();
	    objPrdServerSessionTimeout = Integer.parseInt(appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_PRD_SESSION_TIMEOUT));
	    objUatServerURL = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_UAT_URL).trim();
	    objUatServerPort = Integer.parseInt(appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_UAT_PORT));
	    objUatServerUser = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_UAT_USER).trim();
	    objUatServerPassword = appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_UAT_PASSWORD).trim();
	    objUatServerSessionTimeout = Integer.parseInt(appProperties.getProperty(AppSettingsService.PROP_NAME_OBJ_UAT_SESSION_TIMEOUT));

	} catch (Exception e) {
	    logger.error("Could not load properties, cause: " + e.getMessage());
	}
    }

    /**
     * Returns of the cache contains the document.
     * 
     * @param documentId
     *        ID of document to lookup
     * @return If cache contains document
     */
    private static synchronized boolean cacheHasValidDoc(String documentId) {
	final File dir = new File(getTempDir());
	final FileFilter fileFilter = new WildcardFileFilter(documentId + ".*");
	final File[] files = dir.listFiles(fileFilter);
	if (cachedDocumentMap.get(documentId) == null) {
	    deleteFiles(files);
	    return false;
	} else if (files != null) {
	    boolean cacheExpired = false;
	    for (final File file : files) {
		if (System.currentTimeMillis() - file.lastModified() > ObjectiveUtil.DOCUMENT_CACHE_MS) {
		    cacheExpired = true;
		    break;
		}
	    }
	    if (!cacheExpired) {
		return files.length > 0;
	    } else { // delete files as they have expired
		deleteFiles(files);
		cachedDocumentMap.remove(documentId);
	    }
	}
	return false;
    }

    /**
     * Converts a doc Word file to an HTML formated file.
     * 
     * @param wordFName
     *        Name of Word file to read from
     * @param htmlFName
     *        Name of HTML file to write to
     * @throws Exception
     */
    public static void convertDocToPDF(String wordFName, String pdfFName) throws Exception {
	final com.lowagie.text.Document document = new com.lowagie.text.Document();
	final long start = System.currentTimeMillis();
	try (InputStream is = new FileInputStream(new File(wordFName));
		OutputStream out = new FileOutputStream(new File(pdfFName));) {
	    final HWPFDocument doc = new HWPFDocument(is);

	    final WordExtractor we = new WordExtractor(doc);

	    final PdfWriter writer = PdfWriter.getInstance(document, out);

	    final Range range = doc.getRange();
	    document.open();
	    writer.setPageEmpty(true);
	    document.newPage();
	    writer.setPageEmpty(true);

	    final String[] paragraphs = we.getParagraphText();
	    for (int i = 0; i < paragraphs.length; i++) {

		range.getParagraph(i);
		// CharacterRun run = pr.getCharacterRun(i);
		// run.setBold(true);
		// run.setCapitalized(true);
		// run.setItalic(true);
		paragraphs[i] = paragraphs[i].replaceAll("\\cM?\r?\n", "");
		System.out.println("Length:" + paragraphs[i].length());
		System.out.println("Paragraph" + i + ": " + paragraphs[i].toString());

		// add the paragraph to the document
		document.add(new Paragraph(paragraphs[i]));
	    }
	    document.close();
	    System.out.println("Document testing completed");
	} catch (final Exception e) {
	    System.out.println("Exception during test");
	    e.printStackTrace();
	}
	logger.debug("Converted: " + wordFName + " to: " + pdfFName + " in "
		+ +(System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Converts a docx Word file to an HTML formated file.
     * 
     * @param wordFName
     *        Name of Word file to read from
     * @param htmlFName
     *        Name of HTML file to write to
     * @throws Exception
     */
    public static void convertDocxToPDF(String wordFName, String pdfFName) throws Exception {
	try (InputStream is = new FileInputStream(new File(wordFName));
		OutputStream out = new FileOutputStream(new File(pdfFName));) {
	    final long start = System.currentTimeMillis();
	    final XWPFDocument document = new XWPFDocument(is);
	    final PdfOptions options = PdfOptions.create();
	    PdfConverter.getInstance().convert(document, out, options);
	    logger.debug("Converted: " + wordFName + " to: " + pdfFName + " in "
		    + +(System.currentTimeMillis() - start) + "ms");
	}
    }

    /**
     * Converts an Excel file to an HTML formated file.
     * 
     * @param excelFName
     *        Name of Excel file to read from
     * @param htmlFName
     *        Name of HTML file to write to
     * @throws Exception
     */
    public static void convertExcelToHTML(String excelFName, String htmlFName) throws Exception {
	try (InputStream is = new FileInputStream(new File(excelFName));
		FileWriter writer = new FileWriter(new File(htmlFName));) {
	    final long start = System.currentTimeMillis();
	    final StringBuilder test = new StringBuilder();
	    final ToHtml converter = ToHtml.create(is, test);
	    converter.print();
	    writer.write(test.toString());
	    logger.debug("Converted: " + excelFName + " to: " + htmlFName + " in "
		    + +(System.currentTimeMillis() - start) + "ms");
	}
    }

    /**
     * Converts a RTF file to an HTML formated file.
     * 
     * @param wordFName
     *        Name of RTF file to read from
     * @param htmlFName
     *        Name of HTML file to write to
     * @throws Exception
     */
    public static void convertRTFToHTML(String wordFName, String pdfFName) throws Exception {
	try (InputStream is = new FileInputStream(new File(wordFName));
		OutputStream out = new FileOutputStream(new File(pdfFName));) {
	    final long start = System.currentTimeMillis();
	    final RTFEditorKit rtfEditorKit = new RTFEditorKit();
	    final HTMLEditorKit  htmlEditorKit = new HTMLEditorKit();
	    final Document doc = rtfEditorKit.createDefaultDocument();
	    rtfEditorKit.read(is, doc, 0);
	    htmlEditorKit.write(out, doc, 0, doc.getLength());
	    logger.debug("Converted: " + wordFName + " to: " + pdfFName + " in "
		    + +(System.currentTimeMillis() - start) + "ms");
	}
    }

    public static TreeTable createObjectTreeTable(ObjectiveServer server, String parentId, ObjectiveLinkType linkType,
	    List<ObjectivePropType> propTypes, int linkMaxLength, String windowHeight,
	    String previewHeight, String previewWidth, Label status) throws Exception {
	final TreeTable ttable = getObjectTree(server, parentId, linkType, propTypes, linkMaxLength,
		previewHeight, previewWidth, status);
	if (windowHeight == null) {
	    ttable.setSizeFull();
	} else {
	    ttable.setHeight(windowHeight);
	    ttable.setWidth("100%");
	}
	return ttable;
    }

    /*
     * Deletes a list of files from the file system
     * 
     * @param files List of files to delete
     */
    private static void deleteFiles(File... files) {
	if (files != null) {
	    for (final File file : files) {
		file.delete();
	    }
	}
    }

    /**
     * Utility method to return the creator (author) for an Objective object. Uses
     * reflection to get "getCreator" to access the date created
     * 
     * @param object
     *        to create date
     * @return String representing the date created for the object
     */
    public static String getCreator(Object object) {
	try {
	    final Method method = getMethod(object, "getUserCreated"); 
	    ObjUser user = (ObjUser)method.invoke(object, (Object[]) null);
	    return user.getName();
	} catch (final Exception e) {
	    return "";
	}
    }

    /**
     * Utility method to return the updated by user for an Objective object. Uses
     * reflection to get "getCreator" to access the date created
     * 
     * @param object
     *        to create date
     * @return String representing the date created for the object
     */
    public static String getUpdatedBy(Object object) {
	try {
	    final Method method = getMethod(object, "getUserUpdate");
	    Object userObj = method.invoke(object, (Object[]) null);
	    if (userObj instanceof ObjUser) {   
		ObjUser user = (ObjUser)method.invoke(object, (Object[]) null);
		return user.getName();
	    } else { 
		return "";
	    }
	} catch (final Exception e) {
	    return "";
	}
    }


    /**
     * Generic method retrieve a method for a given object.
     * 
     * @param object
     * @param methodName
     * @return
     * @throws Exception
     */
    private static Method getMethod(Object object, String methodName) throws Exception {
	try {
	    final Method method = object.getClass().getMethod(methodName, (Class[]) null);
	    method.setAccessible(true);
	    return method;
	} catch (final Exception e) {
	    if (logger.isDebugEnabled()) {
		logger.debug("Could not find method: " + methodName + " for object of type" + object.getClass().getName());
		Method[] methods = object.getClass().getMethods();
		for (Method method : methods) {
		    logger.debug(method.getName());
		}
	    }
	    throw e;
	}
    }


    /**
     * Generic method retrieve a field for a given object.
     * 
     * @param object
     * @param fieldName
     * @return
     * @throws Exception
     */
    public static Field getField(Object object, String fieldName) throws Exception {
	try {
	    final Field field = object.getClass().getSuperclass().getDeclaredField(fieldName);
	    field.setAccessible(true);
	    return field; }
	catch (final Exception e) {
	    if (logger.isDebugEnabled()) {
		logger.debug("Could not find field: " + fieldName + " for object of type" + object.getClass().getName());
		Field[] fields = object.getClass().getFields();
		for (Field field : fields) {
		    logger.debug(field.getName());
		}
	    }
	    throw e;
	}
    } 



    /**
     * Utility method to return the date created for an Objective object. Uses
     * reflection to get "getDateCreate" to access the date created
     * 
     * @param object
     *        to create date
     * @return String representing the date created for the object
     */
    public static String getDateCreate(Object object) {
	try {
	    final Method method = getMethod(object, "getDateCreate");
	    return sdf.format((Date) method.invoke(object, (Object[]) null));
	} catch (final Exception e) {
	    return "";
	}
    }

    /**
     * Utility method to return the date updated for an Objective object. Uses
     * reflection to get "getDateUpdate" to access the date updated
     * 
     * @param object
     *        to create date
     * @return String representing the date created for the object
     */
    private static String getDateUpdate(Object object) {
	try {
	    final Method method = getMethod(object, "getDateCreate");
	    return sdf.format((Date) method.invoke(object, (Object[]) null));
	} catch (final Exception e) {
	    return "";
	}
    }

    /**
     * Returns the icon name for a particular document
     * 
     * @param fileType
     *        document extension
     * @return String containing the image name
     */
    public static String getDocIconName(String fileType) {
	String iconName = Constants.IMAGES_UNKNOWN_PNG;
	try {
	    iconName = getVaadinFileTypeIconName(fileType);
	} catch (final Exception e) {
	    logger.error(e);
	}
	return iconName;
    }

    /**
     * Returns a string containing the document name, truncating it if necessary
     * if its length is greater than the maxNameLength.
     * 
     * @param docName
     *        Name of object to truncate if necessary
     * @param maxNameLength
     *        Max length of name to return
     * @return String containing the document name, truncated if necessary
     */

    public static String getDocName(String docName, long size, int maxNameLength) {
	if (maxNameLength > 0 && docName.length() > maxNameLength) {
	    docName = docName.substring(0, Math.min(docName.length(), maxNameLength)) + "...";
	}
	try {
	    docName += " (" + FileUtils.byteCountToDisplaySize(size) + ")";
	} catch (final Exception e) {
	    logger.error(e);
	}
	return docName;
    }

    /**
     * Returns the path to the document. If the document is not in the cache, it
     * caches it.
     * 
     * @param documentId
     *        ID of document
     * @param apiSession  Objective API session to access Objective
     * @return
     * @throws OjiException
     * @throws OjiServerException
     * @throws Exception
     */
    public static synchronized String getDocPath(String documentId, OjiSession apiSession) throws OjiException,
    OjiServerException, Exception {
	String fName = null;
	if (!cacheHasValidDoc(documentId)) {
	    final OjiDocument document = getDocument(documentId, apiSession);
	    if (document != null) {
		final OjiDocVersion docVersion = document.getLastPublishedVersion();
		fName = getTempDir() + documentId + "." + docVersion.getFileType();
		logger.debug("Attempting to get file: " + fName);
		docVersion.getRemoteFile(fName);
		logger.debug("Downloading Objective document with Id: " + documentId
			+ " and caching to: " + fName);
		final String fileType = docVersion.getFileType();
		if (fileType.equalsIgnoreCase("docx")) {
		    final String oldName = fName;
		    fName = getTempDir() + documentId + ".pdf";
		    convertDocxToPDF(oldName, fName);
		    cachedDocumentMap.put(documentId, "pdf");
		} else if (fileType.equalsIgnoreCase("doc")) {
		    final String oldName = fName;
		    fName = getTempDir() + documentId + ".pdf";
		    convertDocToPDF(oldName, fName);
		    cachedDocumentMap.put(documentId, "pdf");
		} else if (fileType.equalsIgnoreCase("xls") || fileType.equalsIgnoreCase("xlsx")) {
		    final String oldName = fName;
		    fName = getTempDir() + documentId + ".html";
		    convertExcelToHTML(oldName, fName);
		    cachedDocumentMap.put(documentId, "html");
		} else if (fileType.equalsIgnoreCase("rtf")) {
		    final String oldName = fName;
		    fName = getTempDir() + documentId + ".html";
		    convertRTFToHTML(oldName, fName);
		    cachedDocumentMap.put(documentId, "html");
		} else {
		    cachedDocumentMap.put(documentId, docVersion.getFileType());
		}
	    }
	} else {
	    fName = getTempDir() + documentId + "." + cachedDocumentMap.get(documentId);
	}
	return fName;
    }

    /**
     * Returns the Objective Document (OjiDocument) which matches the document
     * ID.
     * 
     * @param documentId
     *        To retrieve document
     * @param apiSession  Objective API session to access Objective      	
     * @return
     * @throws OjiException
     * @throws OjiServerException
     */
    private static OjiDocument getDocument(String documentId, OjiSession apiSession) throws OjiException,
    OjiServerException {
	final OjiDocument document = (OjiDocument) apiSession.getObject(documentId);
	return document;
    }

    /**
     * Returns a list of documents belonging to an Objective Folder. The return
     * results are cached by Liferay.
     * 
     * @param parentId
     *        ID of folder
     * @param apiSession 	API Session to access Objective       
     * @return List of documents
     * @throws OjiException
     */
    public static List<OjiDocument> getFolderDocuments(String parentId, OjiSession apiSession) throws OjiException {
	logger.debug("Start getDocuments - getting list from Objective");
	List<OjiDocument> cachedDocuments = null;
	final PortalCache<Serializable, Serializable> cache = getLiferayCache();
	try {
	    cachedDocuments = (List<OjiDocument>) cache.get(CACHE_KEY_DOCUMENTS);
	} catch (final Exception e) {
	    cachedDocuments = null;
	}
	if (false) { // cachedDocuments != null && cachedDocuments.size() > 0) {
	    return cachedDocuments;
	} else {
	    final ArrayList<OjiDocument> links = new ArrayList<OjiDocument>();
	    try {
		final OjiSearch newSearch = apiSession.initAdvancedSearch();
		newSearch.setObjectSearchType(apiSession.getTypeDefnByName("document"));
		newSearch.addCriteria("parent", "is", parentId, 0, 0, OjiSearch.AND_RELATION);

		newSearch.execute();
		final Collection results = newSearch.getResults();
		final Iterator i = results.iterator();
		while (i.hasNext()) {
		    final OjiObject searchResult = (OjiObject) i.next();
		    logger.debug(getObjectEffectivePriviledges(searchResult.getObjId()));
		    links.add((OjiDocument) apiSession.getObject(searchResult.getObjId()));
		}
	    } catch (final Exception e) {
		logger.error(e);
		throw new OjiException(e.getMessage());
	    }
	    logger.debug("End getDocuments - finished getting list from Objective");
	    cache.put(CACHE_KEY_DOCUMENTS, links);
	    return links;
	}
    }

    /**
     * Returns the name of the image resource for a given folder type.
     * 
     * @param folderType
     *        name of folder
     * @return String containing image name
     */
    public static String getFolderIconName(String folderType) {
	final String iconName = Constants.IMAGES_UNKNOWN_PNG;
	try {
	    final String base = "images/folder_types/";
	    switch (folderType) {
	    case "File Divider 1":
		return base + "file_divider_1_16.png";
	    case "File Divider 2":
		return base + "file_divider_2_16.png";
	    case "Level 1 Folder":
		return base + "level_1_folder_16.png";
	    case "Level 2 Folder":
		return base + "level_2_folder_16.png";
	    case "Level 3 Folder":
		return base + "level_3_folder_16.png";
	    case "Level 4 Folder":
		return base + "level_4_folder_16.png";
	    default:
		return base + "unknown_16.png";
	    }
	} catch (final Exception e) {
	    logger.error(e);
	}
	return iconName;
    }

    /**
     * Gets the inbuild Liferay cache for these portlets
     * 
     * @return
     */
    private static PortalCache<Serializable, Serializable> getLiferayCache() {
	final PortalCache<Serializable, Serializable> cache = MultiVMPoolUtil
		.getCache("Objective-Cache");
	return cache;
    }

    /**
     * Returns the download link to a given Objective object
     * 
     * @param objectiveId
     *        ID of Objective Object to download
     * @return String containing URL to document
     */
    public static String getObjectDownloadLink(String objectiveId) {
	return FILE_DOWNLOAD_URL.replace("SERVER_URL", objPrdServerURL).replace("DOCUMENT_ID",
		objectiveId);
    }

    /**
     * Returns a parent for an object.
     * 
     * @param object OjiObject to return parent for
     * @return OjiObject The parent for the object, null if none found or object does not have a parent
     */
    public static OjiObject getParent(OjiObject object) {
	if (object instanceof OjiFolder) {
	    return ((OjiFolder)object).getParent();
	} else if (object instanceof OjiPhysicalFile) {
	    return ((OjiPhysicalFile)object).getParent();
	} else {
	    logger.debug("Could not get parent for: " + object);
	    return null;
	}

    }

    /**
     * Returns a list of ancestors for a given object
     * 
     * @param object Either an ObjObject or OjiObject
     * @param apiSession	Session to Objective API
     * @return
     * @throws OjiServerException
     */
    public static List<OjiObject> getParents(Object object, OjiSession apiSession) throws OjiServerException {
	String objectId = getObjectId(object);
	List<OjiObject> parents = new ArrayList<>();
	OjiObject ojiObject = apiSession.getObject(objectId);
	OjiObject parent = getParent(ojiObject);
	while (parent != null && !parent.getTypeDefinition().getSingularName().equalsIgnoreCase(Constants.OBJECTIVE_NAME_FOLDER)) {
	    parents.add(parent);
	    parent = getParent(parent);
	}
	return parents;
    }


    /**
     * Utility method to return the Object ID for an Objective object. Uses
     * reflection to get "getObjID" to access the ID
     * 
     * @param object
     *        to retrieve ID fo
     * @return ID of object, or empty string if operation fails
     */
    public static String getObjectId(Object object) {
	try {
	    final Method method = getMethod(object, "getObjId");
	    return method.invoke(object, (Object[]) null).toString();
	} catch (final Exception e) {
	    logger.debug("Cannot get ObjectID for " + object.getClass().getName());
	    return "";
	}
    }

    /**
     * Returns a string containing the object name, truncating it if necessary
     * if its length is greater than the maxNameLength.
     * 
     * @param objectName
     *        Name of object to truncate if necessary
     * @param maxNameLength
     *        Max length of name to return
     * @return String containing the object name, truncated if necessary
     */
    public static String getTruncatedObjectName(String objectName, int maxNameLength) {
	if (maxNameLength > 0 && objectName.length() > maxNameLength) {
	    return objectName.substring(0, Math.min(objectName.length(), maxNameLength)) + "...";
	} else {
	    return objectName;
	}
    }


    /**
     * Returns the number of descendants for a given Objective object
     * 
     * @param parentObject
     * @return
     */
    public static int getObjectTreeCount(OjiObject parentObject) {
	int count = 0;
	try {
	    final Method getContentsMethod = parentObject.getClass().getMethod("getContents",
		    (Class[]) null);
	    getContentsMethod.setAccessible(true);
	    final Collection children = (Collection) getContentsMethod.invoke(parentObject,
		    (Object[]) null);
	    if (children != null && children.size() > 0) {
		final Iterator it = children.iterator();
		while (it.hasNext()) {
		    final OjiObject child = (OjiObject) it.next();
		    try {
			final String className = child.getClass().getName();
			count++;
			if (className.equalsIgnoreCase("com.objective.objapi.ObjApiPhysicalFile")
				|| className.equalsIgnoreCase("com.objective.objapi.ObjApiFolder")) {
			    count += getObjectTreeCount(child);
			}
		    } catch (final Exception e) {
		    }
		}
	    }
	} catch (final Exception e) {
	    logger.error(e);
	}
	return count;
    }

    /**
     * Returns the number descendants for a given object
     * @param rootObjectId
     * @return
     */
    public static int getObjectTreeCount(String rootObjectId, OjiSession apiSession) {
	int count = 0;
	try {
	    final OjiObject rootObject = apiSession.getObject(rootObjectId);
	    final Field mObjectField = getField(rootObject, "mObject"); 
	    final ObjObject objObject = (ObjObject) mObjectField.get(rootObject);
	    Map<String, ParentObjectDetails> parents = new HashMap<>();
	    List<OjiObject> ancestors = getParents(objObject, ObjectiveUtil.getOjiPRDSession());
	    count = getObjectTreeCount(rootObject);
	} catch (final Exception e) {
	    logger.error(e);
	}
	return count;
    }

    /**
     * Returns an initialised Hierarchical Container which contains the correct columns for a set of Objective Property Types.
     * 
     * @param propTypes
     * @return
     */
    private static HierarchicalContainer getInitialisedHierarchicalContainer(List<ObjectivePropType> propTypes) {
	HierarchicalContainer dataContainer = new HierarchicalContainer();
	dataContainer.addContainerProperty("Name", Component.class, null);
	for (final ObjectivePropType type : propTypes) {
	    dataContainer.addContainerProperty(type.getLabel(), String.class, null);
	}
	return dataContainer;
    }


    private static synchronized TreeTable getObjectTree(ObjectiveServer server, String rootObjectId, ObjectiveLinkType linkType,
	    List<ObjectivePropType> propTypes, int linkMaxLength, String previewHeight,
	    String previewWidth, Label status) throws Exception {
	logger.debug("Start getObjectTree - getting object tree from Objective");
	final PortalCache<Serializable, Serializable> cache = getLiferayCache();
	TreeTable treeTable = null;
	ObjectiveHierarchicalContainer dataContainer = null;
	String key = server.name() + " container" + rootObjectId;
	for (ObjectivePropType propType : propTypes) {
	    key += "pt:" + propType.ordinal();
	}
	try {
	    if (cache.get(key) != null && cache.get(key) instanceof ObjectiveHierarchicalContainer) {
		dataContainer = (ObjectiveHierarchicalContainer) cache.get(key);
	    }
	} catch (final Exception e) {
	    logger.error(e);
	}
	if (dataContainer == null || dataContainer.getObjectiveItems().isEmpty()) { /// no cached tree data found
	    dataContainer = new ObjectiveHierarchicalContainer(server, rootObjectId, linkType, propTypes, linkMaxLength, previewHeight, previewWidth, status);
	    dataContainer.init();
	    cache.put(key, dataContainer);
	}
	treeTable = new TreeTable("", dataContainer.getCopy());
	treeTable.setSortEnabled(true);
	logger.debug("End getObjectTree - finished getting object tree from Objective");
	return treeTable;
    }

    /**
     * Returns a ParentObjectDetails object containing the main details for a given Objective object.
     * 
     * @param object
     * 
     * @return ObjectDetails
     */
    public static ParentObjectDetails getObjectDetails(Object object) {
	if (object instanceof ObjObject) {
	    ObjObject objObject = (ObjObject)object;
	    return new ParentObjectDetails(getObjectId(objObject), objObject.getName(), 
		    getObjectTypeDefn(objObject), getObjectEffectivePriviledges(objObject).toString(),
		    getObjectAssignedPriviledges(objObject).toString());
	} else if (object instanceof OjiObject) {
	    OjiObject ojiObject = (OjiObject)object;
	    return new ParentObjectDetails(getObjectId(ojiObject), ojiObject.getName(), 
		    getObjectTypeDefn(ojiObject), getObjectEffectivePriviledges(ojiObject).toString(),
		    getObjectAssignedPriviledges(ojiObject).toString());
	} else {
	    return new ParentObjectDetails(null, null, null, null, null);
	}
    }


    /**
     * Returns a stream containing an ".obr" file with the document ID.
     * 
     * @param docId
     *        To return in file
     * @return Stream containing ".obr" file.
     */
    public static StreamResource getOBRFileStream(final String docId) {
	final StreamResource.StreamSource source = new StreamResource.StreamSource() {

	    private static final long serialVersionUID = 407018423121561734L;

	    @Override
	    public InputStream getStream() {
		return new ByteArrayInputStream(docId.getBytes());
	    }
	};
	final StreamResource resource = new StreamResource(source, docId + ".obr");
	return resource;
    }

    /**
     * Returns an Objective API session to the specified server.
     * 
     * @return Objective API session
     * @throws Exception
     */
    public static synchronized OjiSession getOjiSession(ObjectiveServer server) throws Exception {
	if (server == null) {
	    throw new IllegalArgumentException("A null server was passed");
	}
	if (server.equals(ObjectiveServer.PRD)) {
	    return getOjiPRDSession();
	} else if (server.equals(ObjectiveServer.UAT)) {
	    return getOjiUATSession();
	} else {
	    throw new IllegalArgumentException("Cannot connect to server type: " + server);
	}
    }


    /**
     * Returns an Objective PRD API session. The same session is reused up to the
     * expiry time, then a new session is returned.
     * 
     * @return Objective API session
     * @throws Exception
     */
    public static synchronized OjiSession getOjiPRDSession() throws Exception {
	final OjiApplication objective = new OjiApplication();
	return objective.loginUser(objPrdServerUser, objPrdServerPassword, objPrdServerURL, objPrdServerPort);
    }

    /**
     * Returns an Objective UAT API session. The same session is reused up to the
     * expiry time, then a new session is returned.
     * 
     * @return Objective API session
     * @throws Exception
     */
    public static synchronized OjiSession getOjiUATSession() throws Exception {
	final OjiApplication objective = new OjiApplication();
	return objective.loginUser(objUatServerUser, objUatServerPassword, objUatServerURL, objUatServerPort);
    }

    /**
     * Returns an Objective API session to the specified server. The same session is reused up to the
     * expiry time, then a new session is returned.
     * 
     * @param server
     * @return Objective API session
     * @throws OjiServerException
     */
    public static synchronized OjiSession getOjiPRDSession(ObjectiveServer server) throws Exception {
	if (server.equals(ObjectiveServer.PRD)) {
	    return getOjiPRDSession();
	} else if (server.equals(ObjectiveServer.UAT)) {
	    return getOjiUATSession();
	} else {
	    return null;
	}
    }

    /**
     * Returns an Objective API session. The same session is reused up to the
     * expiry time, then a new session is returned.
     * 
     * @param username Username for Objective api account
     * @param passwor password for Objective api account
     * @param serverURL Objective server URL
     * @param port Objective server port
     * 
     * @return Objective API session
     * @throws OjiServerException
     */
    //    private static synchronized OjiSession getOjiSession(String username, String password, String serverURL, int port) throws OjiServerException {
    //	logger.debug("getOjiSession Start");
    //	try {
    //	    if (System.currentTimeMillis() - sessionCreateTime > objPrdServerSessionTimeout) {
    //		logger.debug("Creating new OjiSession as it has expired");
    //		final OjiApplication objective = new OjiApplication();
    //		apiSession = objective.loginUser(username, password, serverURL, port);
    //		sessionCreateTime = System.currentTimeMillis();
    //	    } else {
    //		logger.debug("Returning existing OjiSession");
    //	    }
    //	} catch (final OjiException e) {
    //	    sessionCreateTime = 0;
    //	    apiSession = null;
    //	    logger.error(e);
    //	    throw new OjiServerException(e);
    //	}
    //	logger.debug("getOjiSession End");
    //	return apiSession;
    //    }

    /**
     * Returns a list of String objects containing the effective privileges for a given
     * Objective Object.
     * 
     * Each string is in the format of "ABCDES->Id", e.g. "aBcdeS->A12345"
     * 
     * If the letter is in capital it means it is set. A - Admin B - Browse C -
     * Create D - Delete E - Edit S - See
     * 
     * @param object
     *        Objective object to return privileges for
     * 
     * @return List of Strings containing privileges
     */
    public static List<String> getObjectEffectivePriviledges(Object object) {
	final List<String> privList = new ArrayList<String>();
	Object privileges = null;
	try {
	    final Method method = object.getClass().getMethod("getEffectivePrivilegeAssignments");
	    privileges = method.invoke(object, (Object[]) null);
	    if (privileges != null) {
		Iterator it = null;
		if (privileges instanceof Collection) {
		    it = ((Collection)privileges).iterator();
		} else {
		    it = ((HashMap)privileges).values().iterator();
		}
		while (it.hasNext()) {
		    final ObjPrivAssignment pri = (ObjPrivAssignment) it.next();
		    final StringBuilder privStr = new StringBuilder();
		    privStr.append(pri.getSourceObject().getName() + "(" + pri.getSourceObject().getObjId() + ")"  + "->" + pri.getName() + "(" + pri.getObjId() + ") ->" + pri.getPrivStr());
		    privList.add(privStr.toString());
		}
	    }
	} catch (Exception e) {
	    logger.debug(e);
	}    
	return privList;
    }

    /**
     * Returns the security classification for the object
     * 
     * @param object
     *        Objective object to return caveats for
     * 
     * @return String
     */
    public static String getObjectClassification(Object object) {
	try {
	    Method method = object.getClass().getMethod("getSecurityClassification");
	    method.setAccessible(true);
	    Object value = method.invoke(object, (Object[])null);
	    return value.toString();
	} catch (Exception e) {
	    logger.debug(e);
	    return "";
	}
    }

    /**
     * Returns String representation of the Caveats for the object
     * 
     * @param object
     *        Objective object to return caveats for
     * 
     * @return String
     */
    public static String getObjectCaveats(Object object) {
	try {
	    Method method = object.getClass().getMethod("getCaveatsAsString");
	    method.setAccessible(true);
	    Object value = method.invoke(object, (Object[])null);
	    return value.toString();
	} catch (Exception e) {
	    logger.debug(e);
	    return "";
	}
    }

    /**
     * Returns a list of String objects containing the assigned privileges for a given
     * Objective Object.
     * 
     * Each string is in the format of "ABCDES->Id", e.g. "aBcdeS->A12345"
     * 
     * If the letter is in capital it means it is set. A - Admin B - Browse C -
     * Create D - Delete E - Edit S - See
     * 
     * @param object
     *        Objective object to return privileges for
     * 
     * @return List of Strings containing privileges
     */
    public static List<String> getObjectAssignedPriviledges(Object object) {
	final List<String> privList = new ArrayList<String>();
	Object privileges = null;
	try {
	    final Method method = object.getClass().getMethod("getPrivileges");
	    privileges = method.invoke(object, (Object[]) null);
	    if (privileges != null) {
		Iterator it = null;
		if (privileges instanceof Collection) {
		    it = ((Collection)privileges).iterator();
		} else {
		    it = ((HashMap)privileges).values().iterator();
		}
		while (it.hasNext()) {
		    Object priObj = it.next();
		    final StringBuilder privStr = new StringBuilder();
		    if (priObj instanceof ObjPrivilege) {
			final ObjPrivilege pri = (ObjPrivilege)priObj; 
			privStr.append(pri.asString() + "(" + pri.toWhomAsObjId()+ ")");
		    } else {
			Field field = getField(priObj, "mPrivilege"); 
			ObjPrivilege pri = (ObjPrivilege)field.get(priObj);
			privStr.append(pri.asString() + "(" + pri.toWhomAsObjId()+ ")");
		    }
		    privList.add(privStr.toString());
		}
	    }
	} catch (Exception e) {
	    logger.debug(e);
	}    
	return privList;
    }

    /**
     * Returns a String with the path to the document cache folder.
     * 
     * @return
     */
    public static String getTempDir() {
	return System.getProperty("java.io.tmpdir") + File.separatorChar
		+ ObjectiveUtil.DOCUMENT_TEMP_FOLDER + File.separatorChar;
    }

    /**
     * Returns the matching icon name for the file type.
     * 
     * @param fileType
     *        File extension for document
     * @return The name of the icon
     */
    public static String getThemeFileTypeIconName(String fileType) {
	final String base = "/file_types/";
	switch (fileType.toLowerCase()) {
	case "pdf":
	    return base + "pdf_16.png";
	case "rtf":
	case "doc":
	case "docx":
	    return base + "word_16.png";
	case "xls":
	case "xlsx":
	    return base + "excel_16.png";
	case "htm":
	case "html":
	    return base + "html_16.png";
	case "ppt":
	case "pptx":
	    return base + "ppt_16.png";
	case "msg":
	    return base + "outlook_16.png";
	default:
	    return base + "unknown_16.png";
	}
    }

    /**
     * Utility method to return the type definition for an Objective object.
     * Uses reflection to get "getTypeDefinition" to access the type definition
     * 
     * @param object
     *        to create date
     * @return String representing the date created for the object
     */
    public static String getObjectTypeDefn(Object object) {
	try {
	    Method method = null;
	    try {
		method = getMethod(object, "getTypeDefn");
	    } catch (final Exception e1) {
		try {
		    method = getMethod(object, "getTypeDefinition");
		} catch (final Exception e2) {
		    logger.debug("Cannot get Type Definition for " + object.getClass().getName());
		}
	    }
	    if (method != null) {
		Object typeDefnObject = method.invoke(object, (Object[]) null);
		String typeDefnName = "";
		if (typeDefnObject instanceof com.objective.objapi.ObjApiTypeDefn) {
		    typeDefnName = ((com.objective.objapi.ObjApiTypeDefn)typeDefnObject).getSingularName();
		} else {
		    typeDefnName = typeDefnObject.toString();
		}
		if (typeDefnName.indexOf("(") > 0) {
		    return typeDefnName.substring(0, typeDefnName.indexOf("(")).trim();
		} else {
		    return typeDefnName.trim();
		}
	    } else {
		return "";
	    }
	} catch (final Exception e) {
	    logger.debug("Cannot get Type Definition for " + object.getClass().getName());
	    return "";
	}
    }

    /**
     * Returns a list of Objective Folder IDs which have been linked to in
     * Liferay. The ids are obtained by iterating through the Portlet
     * Preferences for all of the deployed Portlets.
     * 
     * @return List of Folder IDs
     * @throws Exception
     */
    // @Cacheable(value = "folderIds")
    public static List<String> getUsedFolderIds() throws Exception {
	logger.debug("Start getUsedFolderIds - getting porlet preferences");
	final List<String> parentIds = new ArrayList<String>();
	final int totalCount = PortletPreferencesLocalServiceUtil.getPortletPreferencesesCount();
	final List<com.liferay.portal.model.PortletPreferences> list = PortletPreferencesLocalServiceUtil
		.getPortletPreferenceses(0, totalCount);
	final java.util.Iterator<com.liferay.portal.model.PortletPreferences> it = list.iterator();
	while (it.hasNext()) {
	    final com.liferay.portal.model.PortletPreferences pref = it.next();
	    if (pref.getPortletId().startsWith(FolderUI.PORTLET_NAME)) {
		final String value = Util.getLiferayPortletPreferenceValue(pref,
			FolderUI.PREF_PARENT_NODE_ID, "");
		if (StringUtils.isNotBlank(value) && !parentIds.contains(value)) {
		    parentIds.add(value);
		}
	    }
	}
	logger.debug("End getUsedFolderIds - finished porlet preferences");
	return parentIds;
    }

    public static String getVaadinFileTypeIconName(String fileType) {
	final String base = "images/file_types/";
	switch (fileType.toLowerCase()) {
	case "pdf":
	    return base + "pdf_16.png";
	case "rtf":
	case "doc":
	case "docx":
	    return base + "word_16.png";
	case "xls":
	case "xlsm" :    
	case "xlsx":
	    return base + "excel_16.png";
	case "htm":
	case "html":
	    return base + "html_16.png";
	case "ppt":
	case "pptx":
	    return base + "ppt_16.png";
	case "msg":
	    return base + "outlook_16.png";
	case "jpg":
	case "gif":
	case "bmp":
	case "png":
	case "tif":    
	    return base + "image_16.png";

	default:
	    return base + "unknown_16.png";
	}
    }

    /**
     * Performs an Objective search for a set of given Folder IDs.
     * 
     * @param keywords
     *        Words to look for
     * @param parentIds
     *        Folder IDs to search for keywords
     * @param apiSession Session to use to access Objective        
     * @return List of documents matching search criteria
     * @throws OjiException
     * @throws OjiServerException
     */
    public static List<OjiDocument> searchDocuments(String keywords, List<String> parentIds, OjiSession apiSession)
	    throws OjiException, OjiServerException {
	logger.debug("Start searchDocuments - getting list from Objective");
	final List<OjiDocument> links = new ArrayList<OjiDocument>();
	try {
	    final OjiSearch newSearch = apiSession.initAdvancedSearch();
	    newSearch.setObjectSearchType(apiSession.getTypeDefnByName("document"));
	    newSearch.setTextQuery("content:(" + keywords + ")");
	    int index = 0;
	    if (parentIds != null) {
		for (final String parentId : parentIds) {
		    final boolean first = index == 0;
		    final boolean last = index == parentIds.size() - 1;
		    newSearch.addCriteria("parent", "is", parentId, first ? 1 : 0, last ? 1 : 0,
			    first ? OjiSearch.AND_RELATION : OjiSearch.OR_RELATION);
		    index++;
		}
	    }
	    newSearch.execute();
	    final Collection results = newSearch.getResults();
	    final Iterator i = results.iterator();
	    while (i.hasNext()) {
		final OjiObject searchResult = (OjiObject) i.next();
		if (apiSession.getObject(searchResult.getObjId()) instanceof OjiDocument) {
		    links.add((OjiDocument) apiSession.getObject(searchResult.getObjId()));
		}
	    }
	} catch (final Exception e) {
	    logger.error(e);
	    throw new OjiException(e.getMessage());
	}
	logger.debug("End searchDocuments - finished getting list from Objective");
	return links;
    }

    public ObjectiveUtil() {
    }

    /**
     * Utility method to return the value held by an Object
     * reflection to get "getValue" to access the date created
     * 
     * @param object
     *        
     * @return String representing the value for object
     */
    public static String getValue(Object object) {
	try {
	    final Method method = getMethod(object, "getValue");
	    Object value = method.invoke(object, (Object[]) null);
	    if (value instanceof String) {   
		return (String)value;
	    } else { 
		return "";
	    }
	} catch (final Exception e) {
	    return "";
	}
    }

    /**
     * Utility method to return the field value assigned to an Object.
     * 
     * @param object
     * @param fieldName name of field to retrieve value for
     * @param session Objective API session
     *         
     * @return String representing the value for object
     */
    public static String getFieldValue(Object object, String fieldName, OjiSession session) {
	String value = "";
	try {
	    OjiObject catObj = session.getObject(ObjectiveUtil.getObjectId(object));
	    if (catObj instanceof OjiCatalogueableObject) {	
		value = ((OjiCatalogueableObject)catObj).getFieldValueByName(fieldName);
		if (StringUtils.isBlank(value)) {
		    List<Object> fields = (List<Object>)((OjiCatalogueableObject)catObj).getFieldValuesByName(fieldName);
		    value = "";
		    for (Object field : fields) {
			if (value.length() > 0) {
			    value += ", ";
			}
			value += getValue(field);
		    }
		}
	    }
	} catch (final Exception e) {

	}
	return value;
    }


    /**
     * Returns if the given object has a method
     * 
     * @param object Object to check if it has method
     * @param methodName Name of method
     * @return boolen indicating if Object has method
     */
    public static boolean hasMethod(Object object, String methodName) {
	boolean hasMethod = false;
	try {
	    final Method method = getMethod(object, "getFieldValuesByName");
	    if (method != null) {
		hasMethod =  true;
	    }
	}    catch (final Exception e) {

	}
	return hasMethod;
    }

}