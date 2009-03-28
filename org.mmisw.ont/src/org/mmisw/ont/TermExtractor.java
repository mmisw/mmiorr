package org.mmisw.ont;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.util.Unfinished;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Gets the model for a given term.
 * 
 * @author Carlos Rueda
 */
@Unfinished
class TermExtractor {

	private static final Log log = LogFactory.getLog(TermExtractor.class);
	
	
	/**
	 * Gets the model for a given term.
	 * 
	 * @param model original model.
	 * @param mmiUri The URI of the desired term.
	 * @return A model representing the term from the given model.
	 */
	static Model getTermModel(OntModel model, MmiUri mmiUri) {
		String term = mmiUri.getTerm();
		assert term.length() > 0 ;
		
		String termUri = mmiUri.getTermUri();
		Resource termRes = null;
		
		if ( model.contains(ResourceFactory.createResource(termUri), (Property) null, (RDFNode) null) ) {
			termRes = model.getResource(termUri);
		}
		if ( termRes == null ) {
			return null;
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("getTermModel: termUri: " +termUri);
		}

		Model termModel = ModelFactory.createDefaultModel();
		
		if ( true ) { // get all statements about the term
			StmtIterator iter = model.listStatements(termRes, (Property) null, (Property) null);
			if (iter.hasNext()) {
				while (iter.hasNext()) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					termModel.add(sta);
				}
			}
		}
		
		if ( true ) { // test for subclasses
			StmtIterator iter = model.listStatements(null, RDFS.subClassOf, termRes);
			if  ( iter.hasNext() ) {
				while ( iter.hasNext() ) {
					com.hp.hpl.jena.rdf.model.Statement sta = iter.nextStatement();
					
					termModel.add(sta);
				}
			}
		}
		

		if ( model instanceof OntModel ) {
			OntModel ontModel = (OntModel) model;
			ExtendedIterator iter = ontModel.listIndividuals(termRes);
			if ( iter.hasNext() ) {
				while ( iter.hasNext() ) {
					Resource idv = (Resource) iter.next();

					termModel.add(idv, RDF.type, termRes);
				}
			}
		}

		// set a prefix for the ontology URI. Use the topic as the
		// prefix. For example, it the ontogy URI is:
		//     http://mmisw.org/ont/seacoos/qualityFlag_aq
		// then the prefix will be:
		//     qualityFlag_aq
		String ontologyUri = mmiUri.getOntologyUri();
		String prefix = mmiUri.getTopic();
		if ( null != termModel.getNsPrefixURI(prefix + "/") ) {
			// the topic is already used as a prefix. Try the topic with a number, starting from
			// 2 until getting one not used.
			int count = 2;
			while ( null != termModel.getNsPrefixURI(prefix + count + "/") ) {
				count++;
			}
			prefix += count;
		}
		termModel.setNsPrefix(prefix + "/", ontologyUri);


		return termModel;
	}


}
