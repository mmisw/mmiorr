package org.mmisw.ont.graph.allegro;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.util.Util;

import com.franz.agbase.AllegroGraph;
import com.franz.agbase.AllegroGraphConnection;
import com.franz.agbase.AllegroGraphException;
import com.franz.agbase.BlankNode;
import com.franz.agbase.EncodedLiteral;
import com.franz.agbase.LiteralNode;
import com.franz.agbase.ResourceNode;
import com.franz.agbase.Triple;
import com.franz.agbase.TriplesIterator;
import com.franz.agbase.UPI;
import com.franz.agbase.URINode;
import com.franz.agbase.ValueNode;
import com.franz.agbase.ValueObject;
import com.franz.agbase.ValueSetIterator;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;

import edu.drexel.util.rdf.JenaUtil;

/**
 * Some AllegroGraph utilities, some of them copied from the AllegroGraph demonstration
 * examples.
 * 
 * @author Carlos Rueda
 */
class AgUtils {

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
	    log.debug("Number of solutions: " + it.getCount());
	    for (int i=0; it.hasNext(); i++) {
			ValueObject[] objects = it.next();
			log.info("Solution " + (i+1) + ":");
			for (int j = 0; j < objects.length; j++) {
				ValueObject term = objects[j];
				log.info("  " + var[j] + " = " + printValueObject(term));
			}
		}
	}
	
	/** Formats the results in N3
	 * FIXME:actually returning CSV
	 */
	static String getResultInN3(Log log, ValueSetIterator it) {
		return getResultInCsv(log, it);
	}
	
	/** Formats the results in N-TRIPLE
	 * FIXME:actually returning CSV
	 */
	static String getResultInNTriples(Log log, ValueSetIterator it) {
		return getResultInCsv(log, it);
	}
	
	
	/** Formats the results in CSV */
	static String getResultInCsv(Log log, ValueSetIterator it) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
//	    log.debug("Number of solutions: " + it.getCount());
		
		String comma = "";

		// header
		String[] var = it.getNames();
	    for ( int i = 0; i < var.length; i++ ) {
			String value = var[i];
			if ( value.indexOf(',') >= 0 ) {
				value = "\"" +value+ "\"";
			}
			out.printf("%s%s", comma, value);
			comma = ",";
		}
		out.printf("%n");
		
	    for (int i=0; it.hasNext(); i++) {
	    	ValueObject[] objects = it.next();
//	    	log.info("Solution " + (i+1) + ":");
			comma = "";
	    	for (int j = 0; j < objects.length; j++) {
	    		ValueObject term = objects[j];
	    		String value = printValueObject(term);
				if ( value.indexOf(',') >= 0 ) {
					value = "\"" +value+ "\"";
				}
				out.printf("%s%s", comma, value);
				comma = ",";
	    	}
			out.printf("%n");
	    }

		return sw.toString();
	}

	/** Formats the results in JSON */
	// TODO!!
	static String getResultInJson(Log log, ValueSetIterator it) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
//	    log.debug("Number of solutions: " + it.getCount());
		
		String comma = "";

		// header
		String[] var = it.getNames();
	    for ( int i = 0; i < var.length; i++ ) {
			String value = var[i];
			if ( value.indexOf(',') >= 0 ) {
				value = "\"" +value+ "\"";
			}
			out.printf("%s%s", comma, value);
			comma = ",";
		}
		out.printf("%n");
		
	    for (int i=0; it.hasNext(); i++) {
	    	ValueObject[] objects = it.next();
//	    	log.info("Solution " + (i+1) + ":");
			comma = "";
	    	for (int j = 0; j < objects.length; j++) {
	    		ValueObject term = objects[j];
	    		String value = printValueObject(term);
				if ( value.indexOf(',') >= 0 ) {
					value = "\"" +value+ "\"";
				}
				out.printf("%s%s", comma, value);
				comma = ",";
	    	}
			out.printf("%n");
	    }

		return sw.toString();
	}
	

	/** Formats the results in HTML */
	static String getResultInHtml(Log log, ValueSetIterator it) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
//	    log.debug("Number of solutions: " + it.getCount());
		
		
		out.printf("<table class=\"inline\">%n");

		// header
		out.printf("<tr>%n");
		String[] var = it.getNames();
	    for ( int i = 0; i < var.length; i++ ) {
			String value = var[i];
			out.printf("\t<th>%s</th>%n", Util.toHtml(value.toString()));
		}
		out.printf("</tr>%n");
		
	    for (int i=0; it.hasNext(); i++) {
	    	out.printf("<tr>%n");
	    	ValueObject[] objects = it.next();
//	    	log.info("Solution " + (i+1) + ":");
	    	for (int j = 0; j < objects.length; j++) {
	    		ValueObject term = objects[j];
	    		String value = printValueObject(term);
	    		
				String link = Util.getLink(value);
				if ( link != null ) {
					out.printf("\t<td><a href=\"%s\">%s</a></td>%n", link, Util.toHtml(value));
				}
				else {
					out.printf("\t<td>%s</td>%n", Util.toHtml(value));
				}

	    		
	    	}
	    	out.printf("</tr>%n");
	    }
	    
	    out.printf("</table>%n");

		return sw.toString();
	}

	
	public static void showResults(Log log, TriplesIterator it) throws AllegroGraphException {
	    for (int i=0; it.hasNext(); i++) {
			Triple triple = it.next();
			log.info("Solution " + (i+1) + ": " +triple);
		}
	}

	
	/** Formats the results in HTML 
	 * @throws AllegroGraphException */
	static String getResultInHtml(Log log, TriplesIterator it) throws AllegroGraphException {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
		out.printf("<table class=\"inline\">%n");

	    for (int i=0; it.hasNext(); i++) {
	    	out.printf("<tr>%n");
	    	Triple triple = it.next();
	    	
	    	String[] objects = { triple.getSubjectLabel(), triple.getPredicateLabel(), triple.getObjectLabel() };
	    	for (int j = 0; j < objects.length; j++) {
	    		String value = objects[j];
	    		
				String link = Util.getLink(value);
				if ( link != null ) {
					out.printf("\t<td><a href=\"%s\">%s</a></td>%n", link, Util.toHtml(value));
				}
				else {
					out.printf("\t<td>%s</td>%n", Util.toHtml(value));
				}
	    	}
	    	out.printf("</tr>%n");
	    }

		out.printf("</table>%n");

		return sw.toString();
	}
	
	
	/**
	 * Gets a jena model for the given iterator. 
	 * @throws AllegroGraphException 
	 */
	static Model getModel(Log log, TriplesIterator it) throws AllegroGraphException {
		
		Model model = JenaUtil.createDefaultRDFModel();
		
	    while ( it.hasNext() ) {
	    	Triple triple = it.next();
	    	String objLab = triple.getObjectLabel();
	    	
	    	Resource sbj = ResourceFactory.createResource(triple.getSubjectLabel());
	    	Property prd = ResourceFactory.createProperty(triple.getPredicateLabel());
	    	RDFNode obj;
			
			if ( triple.getObject() instanceof ResourceNode ) {
				obj = ResourceFactory.createResource(objLab);
			}
			else {
				obj = ResourceFactory.createPlainLiteral(objLab);
			}
			Statement stmt = ResourceFactory.createStatement(sbj, prd, obj);	
	    	
			model.add(stmt);
	    }
	    
		return model;
	}


	/**
	 * Formats the results in RDF/XML 
	 * @throws AllegroGraphException 
	 */
	static String getResultInRdf(Log log, TriplesIterator it) throws AllegroGraphException {
		Model model = getModel(log, it);
	    String str = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		return str;
	}
	
	/**
	 * Formats the results in N3 
	 * @throws AllegroGraphException 
	 */
	static String getResultInN3(Log log, TriplesIterator it) throws AllegroGraphException {
		Model model = getModel(log, it);
		String str = JenaUtil2.getOntModelAsString(model, "N3");
		return str;
	}
	
	/**
	 * Formats the results in Ntriples 
	 * @throws AllegroGraphException 
	 */
	static String getResultInNTriples(Log log, TriplesIterator it) throws AllegroGraphException {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
	    while ( it.hasNext() ) {
	    	Triple triple = it.next();
	    	String objLab = triple.getObjectLabel();
	    	ValueNode obj = triple.getObject();
	    	boolean objIsUri = obj instanceof ResourceNode;
			
			objLab = objIsUri ? '<' +objLab+ '>' : '"' +objLab+ '"';
	    	
	    	out.printf("<%s> <%s> %s%n", triple.getSubjectLabel(), triple.getPredicateLabel(), objLab);
	    }

		return sw.toString();
	}
	
	/** Formats the results in CSV 
	 * @throws AllegroGraphException */
	static String getResultInCsv(Log log, TriplesIterator it) throws AllegroGraphException {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
	    for (int i=0; it.hasNext(); i++) {
	    	Triple triple = it.next();
	    	out.printf("\"%s\",\"%s\",\"%s\"%n", triple.getSubjectLabel(), triple.getPredicateLabel(), triple.getObjectLabel());
	    }

		return sw.toString();
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
	 * Parses and loads contents into the specified graph and time the load.
	 * 
	 * @param ts A triple store 
	 * @param rdfXml true if contents is in RDF/XML; false if N-triples
	 * @param contents string to parse
	 * @param graph The context to load
	 * @throws AllegroGraphException
	 */
	public static void parseWithTiming(AllegroGraph ts, boolean rdfXml, String contents, Object graph) throws AllegroGraphException {
		log.debug("Parsing and loading RDF contents into graph " +String.valueOf(graph)+ " ...");
		long start = System.currentTimeMillis();
		long n;
		if ( rdfXml ) {
			// TODO baseUri for ts.parseRDFXML
			String baseUri = "TODO";
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
	
	
	@SuppressWarnings("unused")
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
	
	
	
	@SuppressWarnings("unused")
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

	
	@SuppressWarnings("unused")
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
