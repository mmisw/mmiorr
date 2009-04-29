package org.mmisw.ont.util;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;


/**
 * Generates dot for a given ontology.
 * 
 * <p>
 * Preliminary implementation -- needs lots of clean up.
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Dot {

	private static void _outNodeStyles() {
		System.out.println("node [ " +
				"shape=box, " +
				"fillcolor=cornsilk, " +
				"style=filled, " +
				"fontname=\"helvetica\", " +
				"]; "
		);
	}

	
	private static void _outEdgeStyles() {
		System.out.println("edge [ " +
				"fontname=\"helvetica\", " +
				"fontsize=11, " +
				"]; "
		);
	}



	private static void _outSubclass(String sub, String sup) {
		System.out.println("\"" +sup+ "\" -> \"" +sub+ "\"   [ " +
						"dir=back, " +
						"arrowtail=onormal, " +
						"arrowsize=2.0," +
						" ]"
		);
	}

	private static void _outObjectProp(String domain, String prop, String range) {
		System.out.println("\"" +domain+ "\" -> \"" +range+ "\""
				+ "   [ " +
						"label=\"" +prop+ "\", " +
//						"labelfloat=true, " +
//						"decorate=true, " +
						"color=darkgreen, " +
						"fontcolor=darkgreen, " +
						"arrowhead=vee, " +
					"]"
		);	

	}

	public static void main(String[] args) {
		
		if (args.length == 0 ) {
			args = new String[] {
//					"http://mmisw.org/ont/mmi/device"
//					"file:///Users/carueda/mmiworkspace/mmisw/device.owl"
//					"file:///Users/carueda/Desktop/OntDev/mmisw/device.owl"
					"http://mmisw.org/ont/univmemphis/20090422T011238/sensor"
			};
		}
		String ontologyUri = args[0];
		OntModel ontModel = loadModel(ontologyUri);
		
		String baseUri = ontModel.getNsPrefixURI("");
		if ( baseUri == null ) {
			baseUri = ontologyUri;
		}
		
		if ( ! baseUri.endsWith("/") ) {
			baseUri += "/";
		}
		
		System.out.println(" digraph {");
		
//		System.out.println("  rankdir=LR\n");

		_outNodeStyles();
		
		_outEdgeStyles();

		System.out.println();
		
		List<EntityInfo> entities = _classes(baseUri, ontModel);
		
		for ( EntityInfo entityInfo : entities ) {
			String name = entityInfo.getLocalName();
			String label = "{ " +name;
			

			for ( PropValue pv : entityInfo.getProps() ) {
				label += " |{" +pv.getPropName()+ " | " +pv.getValueName()+ " }";
			}
			
			
			label += " }";
			
			System.out.println("\"" +name+ "\"   [ shape=record, label=\"" +label+ "\" ]");	
		}
		
		_hierarchy(baseUri, ontModel);
		_objectProperties(baseUri, ontModel);
		
		System.out.println("}");

	}
	


	private static 	String PREFIXES =
		"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
		"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
		"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
	;


	
	private static List<EntityInfo> _classes(String baseUri, OntModel ontModel) {
		
		List<EntityInfo> entities = new ArrayList<EntityInfo>();
		
		final String CLASSES_QUERY = PREFIXES + 
			"SELECT ?class " +
			"WHERE { ?class rdf:type owl:Class . }"
		;
		Query query = QueryFactory.create(CLASSES_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is baseUri a prefix of entityUri?
				if ( entityUri !=null && entityUri.indexOf(baseUri) == 0 ) {
					ClassInfo entityInfo = new ClassInfo();
					String localName = entityUri.substring(baseUri.length());
					entityInfo.setLocalName(localName);
					
					_addDataProps(entityUri, entityInfo, ontModel);
					
					entities.add(entityInfo);
				}
			}
		}
		
		return entities;
	}
	
	private static void _hierarchy(String baseUri, OntModel ontModel) {
		String SUBCLASS_QUERY = PREFIXES + 
			"SELECT ?sub ?sup " +
			"WHERE { ?sub rdfs:subClassOf ?sup . }"
		;
		
		Query query = QueryFactory.create(SUBCLASS_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			
			String sub = null;
			String sup = null;
			
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is baseUri a prefix of entityUri?
				if ( entityUri != null && entityUri.indexOf(baseUri) == 0 ) {
					String localName = entityUri.substring(baseUri.length());
					
					if ( varName.equals("sub") ) {
						sub = localName;
					}
					else if ( varName.equals("sup") ) {
						sup = localName;
					}
				}
			}
			
			if ( sub != null && sup != null ) {
				_outSubclass(sub, sup);
			}
		}
	}

	
	private static void _objectProperties(String baseUri, OntModel ontModel) {
		final String PROPERTIES_QUERY = PREFIXES + 
			"SELECT ?domain ?prop ?range " +
			"WHERE { ?prop rdf:type owl:ObjectProperty . " +
			"        ?prop rdfs:domain ?domain ." +
			"        ?prop rdfs:range ?range . " +
			"}"
		;
		
		
		Query query = QueryFactory.create(PROPERTIES_QUERY);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			
			String prop = null;
			String domain = null;
			String range = null;
			
			Iterator<?> varNames = sol.varNames();
			while ( varNames.hasNext() ) {
				String varName = String.valueOf(varNames.next());
				String entityUri = String.valueOf(sol.get(varName));
				
				// is baseUri a prefix of entityUri?
				if ( entityUri != null && entityUri.indexOf(baseUri) == 0 ) {
					String localName = entityUri.substring(baseUri.length());
					
					if ( varName.equals("prop") ) {
						prop = localName;
					}
					else if ( varName.equals("domain") ) {
						domain = localName;
					}
					else if ( varName.equals("range") ) {
						range = localName;
					}
				}
			}
			
			if ( prop != null && domain != null && range != null ) {
				_outObjectProp(domain, prop, range);
			}
		}
		

	}


	private static void _addDataProps(String entityUri, EntityInfo entityInfo, OntModel ontModel) {
		final String DATA_PROPS_QUERY_TEMPLATE = PREFIXES +
			"SELECT ?prop ?range " +
			"WHERE { ?prop rdf:type owl:DatatypeProperty . " +
			"        ?prop rdfs:domain <{E}> ." +
			"        ?prop rdfs:range ?range . " +
			"}"
		;		
		String queryStr = DATA_PROPS_QUERY_TEMPLATE.replaceAll("\\{E\\}", entityUri);
		Query query = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.create(query, ontModel);
		
		ResultSet results = qe.execSelect();
		
		while ( results.hasNext() ) {
			QuerySolution sol = results.nextSolution();
			Iterator<?> varNames = sol.varNames();
			
			String propName = null, propUri = null;
			String valueName = null, valueUri = null;
			
			while ( varNames.hasNext() ) {
				String varName = varNames.next().toString();
				RDFNode rdfNode = sol.get(varName);
				
//				if ( rdfNode.isAnon() ) {
//					continue;
//				}
				
				if ( varName.equals("prop") ) {
					if ( rdfNode.isResource() ) {
						Resource r = (Resource) rdfNode;
						propName = r.getLocalName();
						propUri = r.getURI();
					}
					else {
						propName = rdfNode.toString();
						
						// if propName looks like a URL, associate the link also:
						try {
							new URL(propName);
							propUri = propName;
						}
						catch (MalformedURLException ignore) {
						}
					}
				}
				else if ( varName.equals("range") ) {
					if ( rdfNode.isResource() ) {
						Resource r = (Resource) rdfNode;
						valueName = r.getLocalName();
						valueUri = r.getURI();
					}
					else {
						valueName = rdfNode.toString();
						// if valueName looks like a URL, associate the link also:
						try {
							new URL(valueName);
							valueUri = valueName;
						}
						catch (MalformedURLException ignore) {
						}
					}
				}
			}
			
			if ( valueName == null ) {
				// TODO  temporarily assign ".." to null value
				// TODO display enumerations?
				valueName = "...";  
			}
			
			PropValue pv = new PropValue(propName, propUri, valueName, valueUri);
			entityInfo.getProps().add(pv);
		}
	}

	
	/** see JenaUtil2 */
	private static final String FRAG_SEPARATOR = "/" ;

	private static String getURIForBase(String uri) {
		return uri.replaceAll(FRAG_SEPARATOR + "+$", "");
	}
	
	private static OntModel loadModel(String uriModel) {
		OntModel model = createDefaultOntModel();
		uriModel = getURIForBase(uriModel);
		model.read(uriModel);
		return model;
	}
	
	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}
	
	private Dot() {}
	
}


class EntityInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// used only on the client side
	private transient char code;
	
	
	private String localName;
	private String displayLabel;
	private String comment;
	
	private List<PropValue> props;
	
	
	public char getCode() {
		return code;
	}
	public void setCode(char code) {
		this.code = code;
	}
	

	public String getLocalName() {
		return localName;
	}
	public void setLocalName(String localName) {
		this.localName = localName;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public List<PropValue> getProps() {
		if ( props == null ) {
			props = new ArrayList<PropValue>();
		}
		return props;
		
	}
}



 class PropValue implements Serializable {
	private static final long serialVersionUID = 1L;

	private String propName;
	private String propUri;
	
	private String valueName;
	private String valueUri;
	
	
	// no-arg constructor
	public PropValue() {
	}
	
	
	public PropValue(String propName, String propUri, String valueName, String valueUri) {
		super();
		this.propName = propName;
		this.propUri = propUri;
		this.valueName = valueName;
		this.valueUri = valueUri;
	}


	public String getPropName() {
		return propName;
	}


	public void setPropName(String propName) {
		this.propName = propName;
	}


	public String getPropUri() {
		return propUri;
	}


	public void setPropUri(String propUri) {
		this.propUri = propUri;
	}


	public String getValueName() {
		return valueName;
	}


	public void setValueName(String valueName) {
		this.valueName = valueName;
	}


	public String getValueUri() {
		return valueUri;
	}


	public void setValueUri(String valueUri) {
		this.valueUri = valueUri;
	}


	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	

}



 class OntologyInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	// used only on the client side
	private transient char code;
	
	private String uri;
	private String displayLabel;
	
	private List<EntityInfo> entities;
	

	
	public char getCode() {
		return code;
	}
	public void setCode(char code) {
		this.code = code;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getDisplayLabel() {
		return displayLabel;
	}
	public void setDisplayLabel(String displayLabel) {
		this.displayLabel = displayLabel;
	}
	public List<EntityInfo> getEntities() {
		return entities;
	}
	public void setEntities(List<EntityInfo> entities) {
		this.entities = entities;
	}
	
	
	public boolean equals(Object other) {
		return other instanceof OntologyInfo && uri.equals(((OntologyInfo) other).uri);
	}
	public int hashCode() {
		return uri.hashCode();
	}
	
}


 class ClassInfo extends EntityInfo implements Serializable {
	 private static final long serialVersionUID = 1L;

 }


