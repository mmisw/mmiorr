package org.mmisw.voc2rdf.transf;

import edu.drexel.util.rdf.JenaUtil;

/**
 * (Copied/Adapted from org.mmi.ont.voc2owl.trans
 * for "easy" adjustments -- Carlos Rueda)
 * <p><p><p>
 * 
 * <p>
 * Transformation model, with convenient setters and getters.
 * </p>
 * <hr>
 * 
 * @author : $Author: luisbermudez $
 * @version : $Revision: 1.1 $
 * @since : Aug 8, 2006
 */

public class Transformation {
	public String title;

	public final static int TAB = 1;

	public final static int CSV = 2;

	public final static int UNKOWN = -1;

	private int format = 2;

	private String fileIn;

	private String fileOut;

	private String NS;

	private String description;

	private String URLMoreInformation;

	private String source = fileIn;

	private String contributor;

	private String subject;

	private int[] convertToClass = null;

	private int columnForPrimaryClass = 1;

	private String nameForPrimaryClass = null;

	private boolean treatAsHierarchy = false;

	private boolean createAllRelationsHierarchy = true;

	private boolean generateAutoIds = false;

	private String creator;
	
	 private String convertionType = OwlCreatorComplex.CLASS_HIERARCHY;

	public boolean isCreateAllRelationsHierarchy() {
		return createAllRelationsHierarchy;
	}

	public void setCreateAllRelationsHierarchy(
			boolean createAllRelationsHierarchy) {
		this.createAllRelationsHierarchy = createAllRelationsHierarchy;
	}

	public int getColumnForPrimaryClass() {
		return columnForPrimaryClass;
	}

	public void setColumnForPrimaryClass(int columnId) {
		this.columnForPrimaryClass = columnId;
	}

	public String getContributor() {
		return contributor;
	}

	public void setContributor(String contributor) {
		this.contributor = contributor;
	}

	public int[] getConvertToClass() {
		return convertToClass;
	}

	public void setConvertToClass(int[] convertToClass) {
		this.convertToClass = convertToClass;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileIn() {
		return fileIn;
	}

	public void setFileIn(String fileIn) {
		this.fileIn = fileIn;
	}

	public String getFileOut() {
		return fileOut;
	}

	public void setFileOut(String fileOut) {
		this.fileOut = fileOut;
	}

	public int getFormat() {
		return format;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public boolean isGenerateAutoIds() {
		return generateAutoIds;
	}

	public void setGenerateAutoIds(boolean generateAutoIds) {
		this.generateAutoIds = generateAutoIds;
	}

	public String getNS() {
		return JenaUtil.getURIForNS(NS);

	}

	public void setNS(String ns) {
		NS = ns;
	}

	public boolean isTreatAsHierarchy() {
		return treatAsHierarchy;
	}

	public void setTreatAsHierarchy(boolean treatAsHierarchy) {
		this.treatAsHierarchy = treatAsHierarchy;
	}

	public String getNameForPrimaryClass() {
		return nameForPrimaryClass;
	}

	public void setNameForPrimaryClass(String nameForPrimaryClass) {
		this.nameForPrimaryClass = nameForPrimaryClass;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getURLMoreInformation() {
		return URLMoreInformation;
	}

	public void setURLMoreInformation(String moreInformation) {
		URLMoreInformation = moreInformation;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
		
	}

	/**
	 * @return the convertionType
	 */
	public String getConvertionType() {
		return convertionType;
	}

	/**
	 * @param convertionType the convertionType to set
	 */
	public void setConvertionType(String convertionType) {
		this.convertionType = convertionType;
	}

}