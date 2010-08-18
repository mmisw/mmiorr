package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

/**
 * Vine vocabulary definitions.
 * 
 * <p>Namespace: <a href="http://mmisw.org/ont/mmi/vine/"
 *   ><code> http://mmisw.org/ont/mmi/vine/ </code></a>
 *   
 * @author Carlos Rueda
 */
public class Vine {
	
    /** The URI of the vocabulary: "http://mmisw.org/ont/mmi/vine" */
    public static final String URI = "http://mmisw.org/ont/mmi/vine";
    
    /** The namespace of the vocabalary: "http://mmisw.org/ont/mmi/vine/"  */
    public static final String NS = URI + "/";
    
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

	private Vine() {}	
}
