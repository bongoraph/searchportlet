package au.gov.qld.redland.objective.domain;

import java.util.Date;

public class ObjectiveDocument {

    private String contents;
    private Date createDate;
    private String description;
    private Long id;
    private Date lastModified;
    private String title;

    public ObjectiveDocument(String title, String description, String contents, Long id,
	    Date createDate, Date lastModified) {
	this.title = title;
	this.contents = contents;
	this.description = description;
	this.id = id;
	this.createDate = createDate;
	this.lastModified = lastModified;
    }

    public String getContents() {
	return contents;
    }

    public Date getCreateDate() {
	return createDate;
    }

    public String getDescription() {
	return description;
    }

    public Long getId() {
	return id;
    }

    public Date getLastModified() {
	return lastModified;
    }

    public String getTitle() {
	return title;
    }

    public void setContents(String contents) {
	this.contents = contents;
    }

    public void setCreateDate(Date createDate) {
	this.createDate = createDate;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public void setId(Long id) {
	this.id = id;
    }

    public void setLastModified(Date lastModified) {
	this.lastModified = lastModified;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    // public Document getSearchDocument() {
    // Document searchDoc = new DocumentImpl();
    // searchDoc.addText(Field.DESCRIPTION, getDescription());
    // searchDoc.addText(Field.TITLE, getTitle());
    // searchDoc.addUID(ObjectiveIndexer.PORTLET_ID, getId());
    // searchDoc.addKeyword(Field.ENTRY_CLASS_PK, Long.toString(getId()));
    // searchDoc.addKeyword(Field.ENTRY_CLASS_NAME,
    // ObjectiveDocument.class.getName());
    // searchDoc.addKeyword(Field.COMPANY_ID, new Long(10154));
    // // searchDoc.addKeyword(Field.GROUP_ID, new Long(10180));
    // searchDoc.addKeyword(Field.PORTLET_ID, ObjectiveIndexer.PORTLET_ID);
    //
    // return searchDoc;
    //
    // }

}
