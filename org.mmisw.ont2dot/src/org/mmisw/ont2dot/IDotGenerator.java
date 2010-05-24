package org.mmisw.ont2dot;

import java.io.Writer;

/**
 * The operations for the generation.
 * 
 * @author Carlos Rueda
 */
public interface IDotGenerator {
	
	/**
	 * Loads the given model dor subsequent generation.
	 */
	public void loadModel(String ontUri) ;

	/**
	 * Include URL=entityURI to the generated elements?
	 */
	public void setIncludeUrls(boolean include) ;

	/**
	 * Should the imports be loaded as well?
	 */
	public void setIncludeImports(boolean include) ;
	
	/**
	 * 
	 * @param includeLegend
	 */
	public void setIncludeLegend(boolean includeLegend);
	
	/**
	 * Should the rdfs:labels be used for display instead of the local name of the
	 * properties?
	 * @param useLabel
	 */
	public void setUseLabel(boolean useLabel) ;
	
	/**
	 * Ignore rdfs:comments?
	 * @param ignoreRdfsComment
	 */
	public void includeRdfsComment(boolean ignoreRdfsComment);
	
	/**
	 * Indicates that the class hierarchy rooted at the given class should be
	 * "separated" for purposes of showing object properties. That is, any object property with
	 * range in any class in the hierarchy will be indicated in the body of the domain
	 * class and not with an arrow to the range class.
	 * This helps reduce cluttering of the diagram, especially for common attributes
	 * used in many classes.
	 * 
	 * @param classUri URI of the root class.
	 */
	public void separateClassHierarchy(String classUri);
	
	/**
	 * Generates the output format.
	 * @param writer
	 * @param header
	 */
	public void generateDot(Writer writer, String... header) ;
}
