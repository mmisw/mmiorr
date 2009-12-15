package org.mmisw.ont.graph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agbase.BlankNode;
import com.franz.agbase.EncodedLiteral;
import com.franz.agbase.LiteralNode;
import com.franz.agbase.Triple;
import com.franz.agbase.TriplesIterator;
import com.franz.agbase.UPI;
import com.franz.agbase.URINode;
import com.franz.agbase.ValueNode;
import com.franz.agbase.ValueObject;
import com.franz.agbase.ValueSetIterator;

/**
 * Some AllegroGraph utilities, some of them copied from the AllegroGraph demonstration
 * examples.
 * 
 * @author Carlos Rueda
 */
public class AgUtils {

	private static final Log log = LogFactory.getLog(AgUtils.class);
	
	/**
	 * A default limit on the number of triples to show, to avoid showing
	 * overwhelmingly many. 
	 */
	private static int showLimit = 100;
	
	public static void showTriples(String msg, TriplesIterator it) throws AllegroGraphException {
		System.out.println(msg);
		showTriples(it);
	}
	
	public static void showTriples(TriplesIterator it) throws AllegroGraphException {
		showTriples(it, showLimit);
	}
	
	public static void showTriples(TriplesIterator it, int limit) throws AllegroGraphException {
		int i=0;
		while (i<limit && it.step()) {
			showTriple(it.getTriple());
			i++;
		}
		if (it.step()) {
			System.out.println("[Showing " + limit + " triples, there are more]");
		}
	}
	
	public static void showTriple(Triple tr) throws AllegroGraphException {
		System.out.println(tr.toString());
	}

	public static void showResults(Log log, ValueSetIterator it) throws AllegroGraphException {
		String[] var = it.getNames();
	    System.out.println("Number of solutions: " + it.getCount());
	    for (int i=0; it.hasNext(); i++) {
			ValueObject[] objects = it.next();
			log.info("Solution " + (i+1) + ":");
			for (int j = 0; j < objects.length; j++) {
				ValueObject term = objects[j];
				log.info("  " + var[j] + " = " + printValueObject(term));
			}
		}
	}

	public static String printValueObject(ValueObject o) {
		String result;
		if (o == null) {
			result = "Null";
		} else if (o instanceof LiteralNode) {
			LiteralNode l = (LiteralNode)o;
			result = l.getLabel();
		} else if (o instanceof BlankNode) {
			BlankNode b = (BlankNode)o;
			result = b.getID();
		} else if (o instanceof ValueNode) {
			ValueNode n = (ValueNode)o;
			result = n.toString();
		} else {
			result = o.toString();
		}
		return result;
	}

	public static void showURI (URINode r) {
		System.out.println(r.getURI() + ":");
		System.out.println("  Namespace: " + r.getNamespace());
		System.out.println("  LocalName: " + r.getLocalName());
	}

	public static void showLiteral (LiteralNode lit) {
		System.out.println("Literal: " + lit.toString());
		System.out.println("  Label: " + lit.getLabel());
		System.out.println("  Datatype: " + lit.getDatatype());
		System.out.println("  Language: " + lit.getLanguage());
		// Note that only Literals added to the store have a UPI
		System.out.println("  UPI: " + ((ValueNode)lit).queryAGId());
	}
	
	public static void showEncodedLiteral (EncodedLiteral lit) {
		System.out.println("EncodedLiteral: " + lit.toString());
		System.out.println("  Label: " + lit.getLabel());
		System.out.println("  Datatype: " + lit.getDatatype());
		System.out.println("  Language: " + lit.getLanguage());
		// Note that only Literals added to the store have a UPI
		System.out.println("  UPI: " + ((ValueNode)lit).queryAGId());
	}
	
	public static void printObjectArray(String msg, Object[] objArr) {
		System.out.println(msg);
		if (objArr != null) {
			for (int i=0; i<objArr.length;i++) {
				System.out.println("  "+i+": "+objArr[i]);
			}
		}
	}
	
	public static void printUPIArray(String msg, AllegroGraph ts, UPI[] objArr) throws AllegroGraphException {
		System.out.println(msg);
		if (objArr != null) {
			for (int i=0; i<objArr.length;i++) {
				String[] parts = ts.getParts(objArr[i]);
				System.out.println("  "+i+": "+parts[1]);
			}
		}
	}
	
	public static String upiArrayToString(AllegroGraph ts, UPI[] objArr) throws AllegroGraphException {
		StringBuffer buf = new StringBuffer();
		if (objArr != null) {
			buf.append("{");
			for (int i=0; i<objArr.length;i++) {
				String[] parts = ts.getParts(objArr[i]);
				buf.append(parts[1]).append(" ");
			}
			buf.append("}");
		}
		return buf.toString();
	}
	
	public static void printStringArray(String msg, String[] objArr) {
		System.out.println(msg);
		if (objArr != null) {
			for (int i=0; i<objArr.length;i++) {
				System.out.println("  "+i+": "+objArr[i]);
			}
		}
	}

	public static String elapsedTime(long start) {
		long total = System.currentTimeMillis() - start;
		long min = total/60000;
		long msec = total%60000;
		double sec = msec/1000.0;
		String report;
		if (min > 0) {
			report = min + ":" + sec + " minutes:seconds";
		} else {
			report = sec + " seconds";
		}
		return report;
	}

	/**
	 * Load a single RDF/XML file into the default graph and time the load.
	 * 
	 * @param ts A triple store 
	 * @param rdfFile A server-accessible RDF/XML file
	 * @throws AllegroGraphException
	 */
	public static void loadRDFWithTiming(AllegroGraph ts, String rdfFile) throws AllegroGraphException {
		loadRDFWithTiming(ts, rdfFile, "");
	}
	
	/**
	 * Load a single RDF/XML file into the specified graph and time the load.
	 * 
	 * @param ts A triple store 
	 * @param rdfFile A server-accessible RDF/XML file
	 * @param graph The context to load
	 * @throws AllegroGraphException
	 */
	public static void loadRDFWithTiming(AllegroGraph ts, String rdfFile, Object graph) throws AllegroGraphException {
		System.out.println("Loading RDF from " + rdfFile);
		long start = System.currentTimeMillis();
		long n = ts.loadRDFXML(rdfFile, graph);
		System.out.println("Done loading " + n + " triples in " + elapsedTime(start));
	}


	
	/**
	 * Parses and loads contents into the default graph and time the load.
	 * 
	 * @param ts A triple store 
	 * @param contents string to parse
	 * @throws AllegroGraphException
	 */
	public static void parseWithTiming(AllegroGraph ts, boolean rdfXml, String contents) throws AllegroGraphException {
		parseWithTiming(ts, rdfXml, contents, "");
	}
	
	/**
	 * Parses and loads contents into the specified graph and time the load.
	 * 
	 * @param ts A triple store 
	 * @param rdfXml true if contents is in RDF/XML; false if N-triples
	 * @param contents string to parse
	 * @param graph The context to load
	 * @throws AllegroGraphException
	 */
	public static void parseWithTiming(AllegroGraph ts, boolean rdfXml, String contents, Object graph) throws AllegroGraphException {
		log.debug("Parsing and loading RDF contents...");
		long start = System.currentTimeMillis();
		String baseUri = "TODO";
		long n;
		if ( rdfXml ) {
			n = ts.parseRDFXML(contents, graph, baseUri);
		}
		else {
			n = ts.parseNTriples(contents, graph);
		}
		log.debug("Done loading " + n + " triples in " + elapsedTime(start));
	}


	
	
	/**
	 * Demonstrates basics of opening triple stores in various ways.
	 * 
	 * @param args unused
	 * @throws AllegroGraphException
	 */
	public static void main(String[] args) throws AllegroGraphException {
		
		// Connect to the server, which must already be running.
		AllegroGraphConnection ags = new AllegroGraphConnection();
		try {
			ags.enable();
		} catch (Exception e) {
			throw new AllegroGraphException("Server connection problem.", e);
		}

		// Access a store -- default is read-write access
		System.out.println("Access: open a store, creating if necessary.");
		AllegroGraph ts = ags.access("ooici", "/tmp");
		
		
//		addStatements(ts);
		getStatements(ts);
		
//		String graphUri = "http://mmisw/org/ont/graph/ooici";
//		String filename = "/Users/carueda/mmiworkspace/cisemanticprototype/rdf/cdm.owl";
//		loadRdf(ts, filename, graphUri);

		
//		Object context = "<http://example.org/wilburwine>";
//		Object context = "<http://mmisw/org/ont/graph/ooici>";
//		Object context = null;
//		removeAllStatements(ts, context);
		
		
		ts.closeTripleStore();

		// Disconnect from the server
		System.out.println("Disconnecting from the server.");
		ags.disable();
		System.out.println("Done.");
		
	}
	
	
	private static void addStatements(AllegroGraph ts) throws AllegroGraphException {
		
		// Add a single triple to the store
		ts.addStatement("<http://example.org/Dog>",  
				"<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",  
				"<http://www.w3.org/2002/07/owl#Class>"); 

		// For bulk loading over a socket, it is more efficient to buffer 
		// the operation by grouping the triple components into arrays. 
		// The following statement creates 4 triples from corresponding 
		// elements of the arrays.
		ts.addStatements(  
		new String[]{  
		    "<http://example.org/Cat2>",  
		    "<http://example.org/Dog2>",  
		    "<http://example.org/Giraffe2>",  
		    "<http://example.org/Lion2>" },  
		new String[]{  
		    "<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>",  },
		new String[]{  
		    "<http://www.w3.org/2002/07/owl#Class>",  }
		);                             

		// When an array consists of identical elements, it can be 
		// shortened to a single element. The following statement 
		// creates 4 triples where the predicate and object 
		// components are identical.
		ts.addStatements(  
		    new String[]{  
		        "<http://example.org/Cat>",  
			    "<http://example.org/Dog>",  
		        "<http://example.org/Giraffe>",  
		        "<http://example.org/Lion>" },  
		    new String[]{"<http://www.w3.org/2000/01/rdf-schema#subClassOf>"},  
		    new String[]{"<http://example.org/Mammal>"}  
		); 
		
		System.out.println("Added " + ts.numberOfTriples() + " triples to the store.");
	}
	
	private static void getStatements(AllegroGraph ts) throws AllegroGraphException {
		System.out.println("getStatements...");
		TriplesIterator cc = ts.getStatements(false,null,null,null);
		showTriples(cc);

	}
	
	
	
	private static void loadRdf(AllegroGraph ts, String filename, String graphUri) throws AllegroGraphException {

		// Load a server-side file in RDF/XML format into the store's default graph.
		loadRDFWithTiming(ts, filename);

		if ( graphUri != null ) {
			// Load RDF data into a named graph
			if ( graphUri.equals("source") ) {
				loadRDFWithTiming(ts, filename, graphUri);
			}
			else {
				URINode g = ts.createURI(graphUri);
				loadRDFWithTiming(ts, filename, g);
			}
		}
		else {
			loadRDFWithTiming(ts, filename);
		}

//		// Load RDF/XML from a URL into a graph named by the URL
//		ts.loadRDFXML("http://www.w3.org/TR/owl-guide/wine.rdf","source");
//			
//		
//		// If you have a number of files to load, it is more efficient to
//		// provide an array of filenames rather than loading them separately.
//		String[] files = { 
//				AGPaths.dataSources("Geonames_v2.0_Lite.rdf"),
//				AGPaths.dataSources("iswc-aswc-2007-complete.rdf"),
//				"http://www.w3.org/TR/2004/REC-owl-guide-20040210/food.rdf"
//		};
//		AGLoadRDF.loadRDFWithTiming(ts,files,"source");
		
	}

	
	private static void removeAllStatements(AllegroGraph ts, Object context) throws AllegroGraphException {
		ts.removeStatements(null, null, null, context);
		System.out.println("numberOfTriples = " + ts.numberOfTriples());
	}
	
	
	public static void indexAllTriplesWithTiming(Log log, AllegroGraph ts, boolean wait) throws AllegroGraphException {
		log.debug("Indexing all triples...");
		long start = System.currentTimeMillis();
		ts.indexAllTriples(wait);
		log.debug("done in " + elapsedTime(start));
	}

}
