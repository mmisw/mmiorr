package org.mmisw.ont2dot;

import java.io.Writer;

/**
 * The operations for the generation.
 * 
 * @author Carlos Rueda
 */
public interface IDotGenerator {
	
	public enum What { CLASS_INSTANCE_DIAGRAM, CLASS_DIAGRAM, INSTANCE_DIAGRAM,  };

	public void loadModel(String ontUri) ;

	public void setIncludeImports(boolean include) ;
	
	public void setIncludeLegend(boolean includeLegend);
	
	public void setDiagramType(What what);
	
	public void setUseLabel(boolean useLabel) ;
	
	public void setIgnoreRdfsComment(boolean ignoreRdfsComment);
	
	public void generateDot(Writer writer, String... header) ;
}
