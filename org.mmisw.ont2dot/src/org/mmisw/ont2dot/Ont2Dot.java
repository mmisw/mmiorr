package org.mmisw.ont2dot;


import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;



/**
 * This program generates graphviz dot format of an ontology.

 * @author Carlos Rueda
 * @version $Id$
 */
public class Ont2Dot {

	public static final String USAGE = 
		"\n" +
		"MMI Ont2Dot\n" +
		"USAGE: Ont2Dot [options] <ontology-uri> [outputFile]\n" +
		"  outputFile: output file name; can be ``-''  for stdout.\n" +
		"  By default, the name is taken from the URI with ``.dot'' appended.\n" +
		"The name of the generated file is printed to stdout, unless ``-'' was indicated.\n" +
		"\n" +
		"options:\n" +
		"  --includeImports\n" +
		"  --noRdfsLabel\n" +
		"  --includeRdfsComment\n" +
		"  --includeLegend\n" +
		"  --separate <class-uri>\n" 
	;

	private static void help(String error) {
		if ( error != null ) {
			System.err.println("Error: " +error);
			System.exit(1);
		}
		
		System.out.println(USAGE);
		System.exit(0);
	}
	
	/**
	 * Main program.
	 * 
	 * TODO Capture parameters from a parameter file.
	 * 
	 * @param args  See {@link #USAGE}.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		String outFile = null;
		PrintWriter pw = null;
		boolean stdOut = false;
		
		String header = "";
		
		IDotGenerator dotGenerator = DotGeneratorFactory.createInstance();
		
		// ensure defaults:
		dotGenerator.setIncludeImports(false);
		dotGenerator.setUseLabel(true);
		dotGenerator.includeRdfsComment(false);
		dotGenerator.setIncludeLegend(false);
		

		int arg = 0;
		for ( ; arg < args.length && args[arg].startsWith("--"); arg++ ) {
			if ( args[arg].equals("--separate") ) {
				String separate = args[++arg];
				dotGenerator.separateClassHierarchy(separate);
				header += "--separate " +separate + "\n";
			}
			else if ( args[arg].equals("--includeImport") ) {
				dotGenerator.setIncludeImports(true);
				header += "--includeImport " + "\n";
			}
			else if ( args[arg].equals("--noRdfsLabel") ) {
				dotGenerator.setUseLabel(false);
				header += "--noRdfsLabel " + "\n";
			}
			else if ( args[arg].equals("--includeRdfsComment") ) {
				dotGenerator.includeRdfsComment(true);
				header += "--includeRdfsComment " + "\n";
			}
			else if ( args[arg].equals("--includeLegend") ) {
				dotGenerator.setIncludeLegend(true);
				header += "--includeLegend " + "\n";
			}
			else if ( args[arg].equals("--help") ) {
				help(null);
			}
			else {
				help("Invalid option: " +args[arg]);
			}
		}
		if ( arg >= args.length  ) {
			help("indicate an ontology");
			return;
		}
		
		String ontUri = args[arg++];
		header += "Input: " +ontUri + "\n";
		
		if ( arg < args.length ) {
			outFile = args[arg++];
		}
		
		if ( outFile != null ) {
			if ( outFile.equals("-") ) {
				pw = new PrintWriter(System.out, true);
				stdOut = true;
			}
		}
		else {
			// from URI
			outFile = new File(new URL(ontUri).getFile()).getName();
		}
		
		if ( ! stdOut ) {
			if ( ! outFile.toLowerCase().endsWith(".dot") ) {
				outFile += ".dot";
			}
			pw = new PrintWriter(new FileWriter(outFile), true);
		}

		dotGenerator.loadModel(ontUri);
		dotGenerator.generateDot(pw, header);
		
		if ( ! stdOut ) {
			System.out.println(outFile);
		}
	}
	
}
