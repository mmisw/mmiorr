package org.mmisw.ont.vocabulary;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Vocabulary definitions for OmvMmi following
 * <a href="http://marinemetadata.org/files/mmi/OntologyExampleOMV.owl">this example</a>
 * in a similiar way as with
 * <a href="http://jena.sourceforge.net/javadoc/com/hp/hpl/jena/vocabulary/DC_11.html">DC_11 in Jena</a>.
 * 
 * <p>
 * 
 * @author Carlos Rueda
 */
public class OmvMmi {
	private OmvMmi() {}
	
	
    /** <p>The RDF model that holds the vocabulary terms</p> */
    private static Model m_model = ModelFactory.createDefaultModel();
    
    /** <p>The namespace of the vocabalary as a string ({@value})</p> */
    public static final String NS = "http://mmisw.org/ont/mmi/20081020/ontologyMetadata/";
    
    /** <p>The namespace of the vocabalary as a string</p>
     *  @see #NS */
    public static String getURI() {return NS;}
    
    /** <p>The namespace of the vocabalary as a resource</p> */
    public static final Resource NAMESPACE = m_model.createResource( NS );
    
    
    /** OM.5 ; source of omv:acronym */
    public static final Property shortNameUri = m_model.createProperty( NS , "shortNameUri" );
    
    /** UL.1 */
    public static final Property contact = m_model.createProperty( NS , "contact" );
    
    /** UL.2 */
    public static final Property contactRole = m_model.createProperty( NS , "contactRole" );
    
    /** UL.5 */
    public static final Property accessStatus = m_model.createProperty( NS , "accessStatus" );
    
    /** UL.8 */
    public static final Property accessStatusDate = m_model.createProperty( NS , "accessStatusDate" );
    
    /** UL.9 ; until omv:hasLicense */
    public static final Property licenseCode = m_model.createProperty( NS , "licenseCode" );
    
    /** UL.10 */
    public static final Property licenseReference = m_model.createProperty( NS , "licenseReference" );
    
    /** UL.11 */
    public static final Property licenseAsOfDate = m_model.createProperty( NS , "licenseAsOfDate" );
    
    /** UL.12 */
    public static final Property temporaryMmiRole = m_model.createProperty( NS , "temporaryMmiRole" );
    
    /** UL.13 */
    public static final Property agreedMmiRole = m_model.createProperty( NS , "agreedMmiRole" );
    
    /** UL.17 */
    public static final Property creditRequired = m_model.createProperty( NS , "creditRequired" );
    
    /** UL.18 */
    public static final Property creditConditions = m_model.createProperty( NS , "creditConditions" );
    
    /** UL.19 */
    public static final Property creditCitation = m_model.createProperty( NS , "creditCitation" );
    
    /** OSI.1 */
    public static final Property origVocUri = m_model.createProperty( NS , "origVocUri" );
    
    /** OSI.3 */
    public static final Property origVocManager = m_model.createProperty( NS , "origVocManager" );
    
    /** OSI.7 */
    public static final Property origVocDocumentationUri = m_model.createProperty( NS , "origVocDocumentationUri" );
    
    /** OSI.9.1 */
    public static final Property origVocShortName = m_model.createProperty( NS , "origVocShortName" );
    
    /** OSI.9.2 */
    public static final Property origVocDescriptiveName = m_model.createProperty( NS , "origVocDescriptiveName" );
    
    /** OSI.9.3 */
    public static final Property origVocVersionId = m_model.createProperty( NS , "origVocVersionId" );
    
    /** OSI.9.4 */
    public static final Property origVocKeywords = m_model.createProperty( NS , "origVocKeywords" );
    
    /** OSI.9.5 */
    public static final Property origVocSyntaxFormat = m_model.createProperty( NS , "origVocSyntaxFormat" );

    /** OSM.1 */
    public static final Property origMaintainerCode = m_model.createProperty( NS , "origMaintainerCode" );
    

    /** Instance of {@link Omv#usedOntologyEngineeringTool} */  
    public static final Property voc2rdf = m_model.createProperty( NS , "voc2rdf" );

    /** Instance of {@link Omv#usedOntologyEngineeringTool} */  
    public static final Property vine = m_model.createProperty( NS , "vine" );
}
