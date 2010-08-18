package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions for Vine.
 * 
 * @author Carlos Rueda
 */
public class Vine {
	private Vine() {}
	
	
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://mmisw.org/ont/mmi/vine/";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    
    public static Resource Statement = m_model.createResource(NS + "Statement");

    public static Property subject = m_model.createProperty(NS + "subject");

    public static Property predicate = m_model.createProperty(NS + "predicate");

    public static Property object = m_model.createProperty(NS + "object");

    public static Property confidence = m_model.createProperty(NS + "confidence");
}
