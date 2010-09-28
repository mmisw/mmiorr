package org.mmisw.ont;

import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.mmiuri.MmiUri;
import org.mmisw.ont.vocabulary.Omv;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * Gets the "unversioned" version of an ontology.
 * 
 * @author Carlos Rueda
 */
public class UnversionedConverter {
	
	// 2009-06-03 Disabling the alteration of attribute values (so the contents will be exactly
	// as those of the versioned ontology):
	private static boolean ALTERATE_ANNOTATIONS = false;

	private static final Log log = LogFactory.getLog(UnversionedConverter.class);
	
	
	/**
	 * Gets the "unversioned" version of a model.
	 * See issue #24.
	 * 
	 * @param model original model.
	 * @param mmiUri The URI of the corresponding latest version.
	 * @return the unversioned version. null if an error occurs, which will be logged.
	 */
	public static OntModel getUnversionedModel(OntModel model, MmiUri mmiUri) {
		
		//
		// NOTE: I'm copying/adapting code from OrrServiceImpl.review(..):
		//
		
		
		///////////////////////////////////////////////////////////////////
		// creation date:
		final Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		final String creationDate = sdf.format(date);
		

		///////////////////////////////////////////////////////////////////
		// version:
		// bug #243 String version = "Latest terms per " +mmiUri.getVersion();
		String version = JenaUtil2.getOntologyPropertyValue(model, Omv.version);
		
		// issue 252: "omv:version gone?"
		// if Omv.version value is missing, "recover" it from omv:creationDate
		if ( version == null ) {
			version = JenaUtil2.getVersionFromCreationDate(model);
			log.info("Using omv.creationDate to assign omv.version: " +version);
		}

		///////////////////////////////////////////////////////////////////
		// final URI is just the mmiUri but without version:
		String finalUri;
		try {
			finalUri = mmiUri.copyWithVersion(null).getOntologyUri();
		}
		catch (URISyntaxException e) {
			log.error("shouldn't occur", e);
			return null;
		}

		final String ns_ = JenaUtil2.appendFragment(finalUri);
		final String base_ = JenaUtil2.removeTrailingFragment(finalUri);


		String uriForEmpty = model.getNsPrefixURI("");
		if ( uriForEmpty == null ) {
			// FIXME Get the original ns when model.getNsPrefixURI("") returns null
			// For now, returning error:
			String error = "Unexpected error: No namespace for prefix \"\"";
			log.error(error);
			return null;
			
			// This case was manifested with the platform.owl ontology.
		}
		
		if ( log.isDebugEnabled() ) {
			log.debug("review: model.getNsPrefixURI(\"\") = " +uriForEmpty);
		}
		
		// Why using JenaUtil.getURIForNS(uriForEmpty) to get the namespace?
		// model.getNsPrefixURI("") should provide the base namespace, in fact,
		// I verified that this call gives the right URI associated with "" in two
		// cases, one with xxxx/ (slash) and xxxxx# (pound) at the end.
		// So, instead of:
		//    final String original_ns_ = JenaUtil.getURIForNS(uriForEmpty);
		// I just take the reported URI as given by model.getNsPrefixURI(""):
		final String original_ns_ = uriForEmpty;

		
//		log.info("original namespace: " +original_ns_);
//		log.info("Setting prefix \"\" for URI " + ns_);
		model.setNsPrefix("", ns_);
		if ( log.isDebugEnabled() ) {
			log.debug("     new namespace: " +ns_);
		}

		
		// Update statements  according to the new namespace:
		_replaceNameSpace(model, original_ns_, ns_);

		
		/////////////////////////////////////////////////////////////////
		// Is there an existing OWL.Ontology individual?
		// TODO Note that ONLY the first OWL.Ontology individual is considered.
		Resource ontRes = JenaUtil2.getFirstIndividual(model, OWL.Ontology);
		List<Statement> prexistStatements = null; 
		if ( ontRes != null ) {
			prexistStatements = new ArrayList<Statement>();
//			log.info("Getting pre-existing properties for OWL.Ontology individual: " +ontRes.getURI());
			StmtIterator iter = ontRes.listProperties();
			while ( iter.hasNext() ) {
				Statement st = iter.nextStatement();
				prexistStatements.add(st);
			}	
		}

		
		// The new OntModel that will contain the pre-existing attributes (if any),
		// plus the new and updated attributes:
		final OntModel newOntModel = ModelFactory.createOntologyModel(model.getSpecification(), model);
		// Note: previously, newOntModel = new OwlModel(model); but the OwlModel extension was not used
		// in any particular way, so the new call should be equivalent.
		final Ontology ont_ = newOntModel.createOntology(base_);
		if ( log.isDebugEnabled() ) {
			log.debug("New ontology created with namespace " + ns_ + " base " + base_);
		}
		newOntModel.setNsPrefix("", ns_);
		
		
		//////////////////////////////////////////////////////////////////////////
		// set new values for the unversioned version:
		Map<String, String> newValues = new HashMap<String, String>();
		
		// Set internal attributes, which are updated in the newValues map itself
		// so we facilite the processing below:
		newValues.put(Omv.uri.getURI(), base_);
		if ( version != null ) {
			newValues.put(Omv.version.getURI(), version);
		}
		
		newValues.put(Omv.creationDate.getURI(), creationDate);


		//////////////////////////////////////////////////
		// transfer any preexisting attributes, and then remove all properties from
		// pre-existing ontRes so just the new OntModel gets added.
		if ( ontRes != null ) {
			for ( Statement st : prexistStatements ) {
				Property prd = st.getPredicate();

				//
				// Do not tranfer pre-existing/pre-assigned-above attributes
				//
				String newValue = newValues.get(prd.getURI());
				if ( newValue == null || newValue.trim().length() == 0 ) {
					// not assigned above.
					
					if ( ALTERATE_ANNOTATIONS ) {
						// See if it's one of the ones to be modified:
						if ( Omv.description.getURI().equals(prd.getURI()) ) {
							// transfer modified description:
							String description = 
								"An unversioned ontology containing the latest terms as of the request time, " +
								"for the ontology containing: " +st.getObject();
//							log.info("  Transferring modified description: " +st.getSubject()+ " :: " +prd+ " :: " +description);
							newOntModel.add(ont_, st.getPredicate(), description);
						}
						else if ( Omv.name.getURI().equals(prd.getURI()) ) {
							// transfer modified title:
							String title = 
								"Unversioned form of: " +st.getObject();
//							log.info("  Transferring modified title: " +st.getSubject()+ " :: " +prd+ " :: " +title);
							newOntModel.add(ont_, st.getPredicate(), title);
						}
						else {
							// transfer as it comes:
//							log.info("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
							newOntModel.add(ont_, st.getPredicate(), st.getObject());
						}
					}
					else {
						// just transfer as it comes:
//						log.info("  Transferring: " +st.getSubject()+ " :: " +prd+ " :: " +st.getObject());
						newOntModel.add(ont_, st.getPredicate(), st.getObject());
					}
				}
				else {
//					log.info(" Not Transferring: " +prd+ " from previous version because new value " +newValue);
				}
			}	
			
//			log.info("Removing original OWL.Ontology individual");
			ontRes.removeProperties();
			// TODO the following may be unnecesary but doesn't hurt:
			model.remove(ontRes, RDF.type, OWL.Ontology); 
		}

		
		
		///////////////////////////////////////////////////////
		// Update attributes in model:
		
		ont_.addProperty(Omv.uri, base_);
		if ( version != null ) {
			ont_.addProperty(Omv.version, version);
		}
		ont_.addProperty(Omv.creationDate, creationDate);

		////////////////////////////////////////////////////////////////////////
		// Done with the model. 
		////////////////////////////////////////////////////////////////////////
		
		return model;
	}
	
	/**
	 * Replaces a namespace in a model.
	 * 
	 * @param model Model to be updated.
	 * @param oldNameSpace 
	 * @param newNameSpace
	 */
	private static void _replaceNameSpace(OntModel model, String oldNameSpace, String newNameSpace) {
		
		if ( log.isDebugEnabled() ) {
			log.debug(" REPLACING NAMESPACE " +oldNameSpace+ " WITH " +newNameSpace);
		}
		
		// old statements to be removed:
		List<Statement> o_stmts = new ArrayList<Statement>(); 
		
		// new statements to be added:
		List<Statement> n_stmts = new ArrayList<Statement>(); 
		
		// check all statements in the model:
		StmtIterator existingStmts = model.listStatements();
		while ( existingStmts.hasNext() ) {
			Statement o_stmt = existingStmts.nextStatement();
			Resource sbj = o_stmt.getSubject();
			Property prd = o_stmt.getPredicate();
			RDFNode obj = o_stmt.getObject();
			
			// will indicate that o_stmt is affected by the namespace change:
			boolean any_change = false;
			
			// the new triplet, initialized with the existing statement:
			Resource n_sbj = sbj;
			Property n_prd = prd;
			RDFNode  n_obj = obj;

			if ( oldNameSpace.equals(sbj.getNameSpace()) ) {
				// the subject is affected; create new subject
				n_sbj = model.createResource(newNameSpace + sbj.getLocalName());
				any_change = true;
			}
			if ( oldNameSpace.equals(prd.getNameSpace()) ) {
				// the predicate  is affected; create new predicate
				n_prd = model.createProperty(newNameSpace + prd.getLocalName());
				any_change = true;
			}
			if ( (obj instanceof Resource) && oldNameSpace.equals(((Resource) obj).getNameSpace()) ) {
				// the object is affected; create new object
				n_obj = model.createResource(newNameSpace + ((Resource) obj).getLocalName());
				any_change = true;
			}

			if ( any_change ) {
				// create the new statment:
				Statement n_stmt = model.createStatement(n_sbj, n_prd, n_obj);
				// and update the lists for final adjustments below:
				o_stmts.add(o_stmt);
				n_stmts.add(n_stmt);
			}
		}
		
		// add the new statements
		for ( Statement n_stmt : n_stmts ) {
			model.add(n_stmt);
		}
		
		// remove the old statements
		for ( Statement o_stmt : o_stmts ) {
			model.remove(o_stmt);
		}
	}


}
