package org.mmisw.ont2dot;


import java.io.FileWriter;
import java.io.PrintWriter;

import org.mmisw.ont2dot.IDotGenerator.DiagramType;


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
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		DiagramType whatDiagram = DiagramType.CLASS_INSTANCE_DIAGRAM;
		
		boolean includeImports = false; 
		boolean includeLegend = false;
		boolean useLabel = true;
		boolean ignoreRdfsComment = true;
		
		PrintWriter pw = null;
		
		if (args.length == 0 || args[0].equals("--help") ) {
			System.out.println("USAGE: Ont2Dot <ontology-uri> [outputFile]");
			return;
		}
		
		String ontUri = args[0];
		
		if (args.length > 1 ) {
			String outFile = args[1];
			pw = new PrintWriter(new FileWriter(outFile), true);
			
		} else {
			pw = new PrintWriter(System.out, true);
		}

		IDotGenerator dotGenerator = DotGeneratorFactory.createInstance();
		
		dotGenerator.loadModel(ontUri);
		
		dotGenerator.setIncludeImports(includeImports);
		dotGenerator.setIncludeLegend(includeLegend);
		dotGenerator.setDiagramType(whatDiagram);
		dotGenerator.setUseLabel(useLabel);
		dotGenerator.setIgnoreRdfsComment(ignoreRdfsComment);
		
		
		dotGenerator.generateDot(pw, "Input: " +ontUri);
		
	}
	
}
