package org.mmisw.orrclient.core.util;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * SKOS vocabulary elements in the "http://www.w3.org/2008/05/skos#" namespace.
 * 
 * @author Carlos Rueda
*/
public class Skos2 {
	private static Model model = ModelFactory.createDefaultModel();

	public static final String NS = "http://www.w3.org/2008/05/skos#";

	private static final Property property(String local) {
		return model.createProperty(NS, local);
	}
    
	public static final Resource Concept = model.createResource(NS + "Concept", RDFS.Class);
	
	public static final Property prefLabel = property("prefLabel");
	public static final Property altLabel = property("altLabel");
	public static final Property hiddenLabel = property("hiddenLabel");
	
	public static final Property definition = property("definition");
	
	public static final Property note = property("note");
	public static final Property changeNote = property("changeNote");
	public static final Property editorialNote = property("editorialNote");
	public static final Property example = property("example");
	public static final Property historyNote = property("historyNote");
	public static final Property scopeNote = property("scopeNote");
	
	
	public static final Property hasTopConcept = property("hasTopConcept");


	public static final Property broader = property("broader");
	public static final Property narrower = property("narrower");
	public static final Property related = property("related");
	
	
	public static final Property exactMatch = property("exactMatch");
	public static final Property closeMatch = property("closeMatch");
	public static final Property broadMatch = property("broadMatch");
	public static final Property narrowMatch = property("narrowMatch");
	public static final Property relatedMatch = property("relatedMatch");
	
}
