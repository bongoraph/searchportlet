package au.gov.qld.redland.objective;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;

import au.gov.qld.redland.objective.util.ObjectiveUtil;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.OpenSearch;
import com.liferay.portal.kernel.search.SearchException;
import com.objective.oji.OjiDocument;
import com.objective.oji.OjiException;

/**
 * OpenSearch implementation for the objective portlets.
 * 
 * 
 * @author danielma
 *
 */
public class ObjectiveOpenSearchImpl implements OpenSearch {

    private static Log logger = LogFactoryUtil.getLog(ObjectiveOpenSearchImpl.class.getName());
    private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Returns an OpenSearch entry tag populated with the details of the OjiDocument
     * 
     * @param document	To get details from
     * @return	String containing populate entry tag
     * 
     * @throws OjiException
     */
    private String getEntry(OjiDocument document) throws OjiException {
	final String docName = document.getName()
		+ " ("
		+ FileUtils
			.byteCountToDisplaySize(document.getLastPublishedVersion().getFileSize())
		+ ")";
	final String iconName = ObjectiveUtil.getThemeFileTypeIconName(document
		.getLastPublishedVersion().getFileType());
	final String entry = "   <entry>" + "     <title><![CDATA[" + docName + "]]></title>"
		+ "     <link href=\"" + "/objective_62-portlet/GetOBRFileServlet?objectiveID=" + document.getObjId()
		+ "\"/>" + "     <id>" + document.getObjId() + "</id>" + "     <updated>"
		+ df.format(document.getLastPublishedVersion().getDateFileUpdated()) + "</updated>"
		+ "     <content type=\"text\">TEST" + "     </content>" + "     <author>"
		+ "       <name>" + document.getLastPublishedVersion().getCreator().getName()
		+ "</name>" + "     </author>" + "     <icon>" + iconName + "</icon>"
		+ "     <tags></tags>" + "   </entry>";
	return entry;
    }

    /*
     * (non-Javadoc)
     * @see com.liferay.portal.kernel.search.OpenSearch#isEnabled()
     */
    @Override
    public boolean isEnabled() {
	// TODO Auto-generated method stub
	return true;
    }

    /**
     * Does the actual searching and returns a string containing the OpenSearch results.
     * 
     * {@inheritDoc}
     */
    @Override
    public String search(HttpServletRequest request, long groupId, long userId, String keywords,
	    int startPage, int itemsPerPage, String format) throws SearchException {
	try {
//	    final List<String> fileIds = ObjectiveUtil.getUsedFolderIds();
	    final List<OjiDocument> srchResults = ObjectiveUtil.searchDocuments(keywords, null, ObjectiveUtil.getOjiPRDSession());
	    final StringBuilder result = new StringBuilder();
	    result.append(" <?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		    + " <feed xmlns=\"http://www.w3.org/2005/Atom\" "
		    + "       xmlns:opensearch=\"http://a9.com/-/spec/opensearch/1.1/\">"
		    + "   <title>RCC Search: EDRMS</title> "
		    + "   <link href=\"http://rcc/keywords\"/>" + "   <updated>"
		    + df.format(new Date(System.currentTimeMillis())) + "</updated>"
		    + "   <author> " + "     <name>RCC</name>" + "   </author> "
		    + "   <opensearch:totalResults> " + srchResults.size()
		    + "</opensearch:totalResults>"
		    + "   <opensearch:startIndex>1</opensearch:startIndex>"
		    + "   <opensearch:itemsPerPage>" + srchResults.size()
		    + "</opensearch:itemsPerPage>"
	    );
	    int startDoc = (startPage - 1) * itemsPerPage;
	    int endDoc = Math.min((startPage - 1) * itemsPerPage + itemsPerPage, srchResults.size());
	    
	    for (int doc = startDoc; doc < endDoc; doc++) {
		result.append(getEntry(srchResults.get(doc)));
	    }
	    result.append("</feed>");
	    return result.toString();

	} catch (final Exception e) {
	    logger.error(e);
	    throw new SearchException(e);
	}
    }

    /**
     * Not used.
     */
    @Override
    public String search(HttpServletRequest arg0, long arg1, String arg2, int arg3, int arg4,
	    String arg5) throws SearchException {
	return null;
    }

    /**
     * Not used.
     */
    @Override
    public String search(HttpServletRequest arg0, String arg1) throws SearchException {
	return null;
    }

}
