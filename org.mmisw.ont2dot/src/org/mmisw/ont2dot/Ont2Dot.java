package org.mmisw.ont2dot;


import java.io.PrintWriter;

import org.mmisw.ont2dot.IDotGenerator.What;


/**
 * This program generates graphviz dot format of an ontology.

 * @author Carlos Rueda
 * @version $Id$
 */
public class Ont2Dot {

	/**
	 * Main program.
	 * 
	 * TODO Capture parameters from command line or from a parameter file.
	 * 
	 * @param args  An argument indicating the URI of the ontology, or "--help"
	 */
	public static void main(String[] args) {
		
		What whatDiagram = What.CLASS_INSTANCE_DIAGRAM;
		
		boolean includeImports = false; 
		boolean includeLegend = false;
		boolean useLabel = true;
		boolean ignoreRdfsComment = true;
		
		
		if (args.length == 0 || args[0].equals("--help") ) {
			System.out.println("USAGE: Ont2Dot <ontology-uri>");
			return;
		}
		
		String ontUri = args[0];

		IDotGenerator dotGenerator = DotGeneratorFactory.createInstance();
		
		dotGenerator.setIncludeImports(includeImports);
		dotGenerator.setIncludeLegend(includeLegend);
		dotGenerator.setDiagramType(whatDiagram);
		dotGenerator.setUseLabel(useLabel);
		dotGenerator.setIgnoreRdfsComment(ignoreRdfsComment);
		
		PrintWriter pw = new PrintWriter(System.out, true);
		
		dotGenerator.generateDot(pw, "Input: " +ontUri);
		
	}
	
}
