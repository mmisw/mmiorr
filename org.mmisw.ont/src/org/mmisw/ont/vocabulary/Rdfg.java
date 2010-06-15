package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;


/**
 * Vocabulary definitions for RDFG.
 * See <a href="http://www.w3.org/2004/03/trix/rdfg-1/">http://www.w3.org/2004/03/trix/rdfg-1/</a>.
 * 
 * @author Carlos Rueda
 */
public class Rdfg {
	private Rdfg() {}
	
    /** The namespace of the vocabalary as a string. */
	public static final String NS = "http://www.w3.org/2004/03/trix/rdfg-1/";
	
    /** The namespace of the vocabalary as a resource. */
    public static final Resource NAMESPACE = ResourceFactory.createResource( NS );
	
	public static final Resource Graph = ResourceFactory.createResource(NS + "Graph");
	public static final Property subGraphOf = ResourceFactory.createProperty(NS + "subGraphOf");
	
	public static final Property equivalentGraph = ResourceFactory.createProperty(NS + "equivalentGraph");
	
}
