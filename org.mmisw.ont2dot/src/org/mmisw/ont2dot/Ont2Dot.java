package org.mmisw.ont2dot;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;

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
		
		String outFile = null;
		PrintWriter pw = null;
		
		if (args.length == 0 || args[0].equals("--help") ) {
			System.out.println(
					"USAGE: Ont2Dot <ontology-uri> [outputFile]\n" +
					"  outputFile: name of the putput file; can be ``-''  for stdout. " +
					"  By default, the name is taken from the URI with ``.dot'' appended."
			);
			return;
		}
		
		String ontUri = args[0];
		
		if (args.length > 1 ) {
			outFile = args[1];
		}
		
		if ( outFile != null ) {
			if ( outFile.equals("-") ) {
				pw = new PrintWriter(System.out, true);
			}
			else {
				pw = new PrintWriter(new FileWriter(outFile), true);
			}
		} 
		else {
			outFile = new File(new URL(ontUri).getFile()).getName();
			outFile += ".dot";
			pw = new PrintWriter(new FileWriter(outFile), true);
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
