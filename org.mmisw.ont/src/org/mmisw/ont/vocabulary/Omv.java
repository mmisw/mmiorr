package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions for Omv following
 * <a href="http://marinemetadata.org/files/mmi/OntologyExampleOMV.owl">this example</a>
 * in a similiar way as with
 * <a href="http://jena.sourceforge.net/javadoc/com/hp/hpl/jena/vocabulary/DC_11.html">DC_11 in Jena</a>.
 * 
 * <p>
 * 
 * @author Carlos Rueda
 */
public class Omv {
	private Omv() {}
	
	
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://omv.ontoware.org/2005/05/ontology#";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    
    /** OM.1 */
    public static final Property uri = m_model.createProperty( NS , "uri" );
    
    /** OM.22 */
    public static final Property name = m_model.createProperty( NS , "name" );
    
    /** OM.9 */
    public static final Property description = m_model.createProperty( NS , "description" );
    
    /** OM.4 ; is really short name, not acronym */
    public static final Property acronym = m_model.createProperty( NS , "acronym" );
    
    /** OM.2 */
    public static final Property version = m_model.createProperty( NS , "version" );
    
    /** OM.12 */
    public static final Property keywords = m_model.createProperty( NS , "keywords" );
    
    /** OSI.5 */
    public static final Property hasCreator = m_model.createProperty( NS , "hasCreator" );
    
    /** OM.3 ; creation date of this version */
    public static final Property creationDate = m_model.createProperty( NS , "creationDate" );
    
    /** OM.23 */
    public static final Property hasDomain = m_model.createProperty( NS , "hasDomain" );
    
    /** OM.25 ; same as uri + '.owl' */
    public static final Property resourceLocator = m_model.createProperty( NS , "resourceLocator" );
    
    /** OM.15 */
    public static final Property documentation = m_model.createProperty( NS , "documentation" );
    
    /** OM.17 */
    public static final Property naturalLanguage = m_model.createProperty( NS , "naturalLanguage" );
    
    /** OM.21 */
    public static final Property hasContributor = m_model.createProperty( NS , "hasContributor" );
    
    /** OM.19 */
    public static final Property hasPriorVersion = m_model.createProperty( NS , "hasPriorVersion" );
    

}
