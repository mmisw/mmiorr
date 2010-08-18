package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vine vocabulary definitions, old namespace.
 * 
 * <p>Namespace: <code> http://marinemetadata.org/mmiws/20071128/vine# </code>
 * 
 * @author Luis Bermudez 
 * @author Carlos Rueda
 */
public class Vine20071128 {
    /** The URI of the vocabulary: "http://marinemetadata.org/mmiws/20071128/vine" */
    public static final String URI = "http://marinemetadata.org/mmiws/20071128/vine";
    
    /** The namespace of the vocabalary: "http://marinemetadata.org/mmiws/20071128/vine#"  */
    public static final String NS = URI + "#";
    
    public static final Resource Statement = resource("Statement");

    public static final Property subject = property("subject");

    public static final Property predicate = property("predicate");

    public static final Property object = property("object");

    public static final Property confidence = property("confidence");

    
    private static final Resource resource(String local) {
		return ResourceFactory.createResource(NS + local);
	}

    private static final Property property(String local) {
		return ResourceFactory.createProperty(NS, local);
	}

	private Vine20071128() {}	
}
