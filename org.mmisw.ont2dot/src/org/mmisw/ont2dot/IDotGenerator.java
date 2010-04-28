package org.mmisw.ont2dot;

import java.io.Writer;

/**
 * The operations for the generation.
 * 
 * @author Carlos Rueda
 */
public interface IDotGenerator {
	
	public enum DiagramType { CLASS_INSTANCE_DIAGRAM, CLASS_DIAGRAM, INSTANCE_DIAGRAM,  };

	/**
	 * Loads the given model dor subsequent generation.
	 */
	public void loadModel(String ontUri) ;

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
	 * Sets the type of diagram to generate
	 */
	public void setDiagramType(DiagramType dt);
	
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
	public void setIgnoreRdfsComment(boolean ignoreRdfsComment);
	
	/**
	 * Generates the output format.
	 * @param writer
	 * @param header
	 */
	public void generateDot(Writer writer, String... header) ;
}
