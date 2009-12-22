package org.mmisw.iserver.core.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.iserver.core.MdHelper;
import org.mmisw.iserver.gwt.client.rpc.MetadataBaseInfo;
import org.mmisw.iserver.gwt.client.rpc.TempOntologyInfo;
import org.mmisw.iserver.gwt.client.vocabulary.AttrDef;
import org.mmisw.iserver.gwt.client.vocabulary.AttrGroup;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Omv;
import org.mmisw.ont.vocabulary.OmvMmi;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;

/**
 * Helper to read a temporary (uploaded into working space) ontology.
 * 
 * @author Carlos Rueda
 */
public class TempOntologyHelper {
	
	private final Log log = LogFactory.getLog(TempOntologyHelper.class);

	
	private MetadataBaseInfo metadataBaseInfo;
	
	public TempOntologyHelper(MetadataBaseInfo metadataBaseInfo) {
		this.metadataBaseInfo = metadataBaseInfo;
	}


	public TempOntologyInfo getTempOntologyInfo(String uploadResults, TempOntologyInfo tempOntologyInfo,
			boolean includeRdf
	) {
		
		uploadResults = uploadResults.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		log.info("getOntologyInfo: " +uploadResults);
		
		
		if ( uploadResults.matches(".*<error>.*") ) {
			log.info("<error>");
			tempOntologyInfo.setError(uploadResults);
			return tempOntologyInfo;
		}
		
		if ( false && !uploadResults.matches(".*success.*") ) {
			log.info("Not <success> !");
			// unexpected response.
			tempOntologyInfo.setError("Error while loading ontology. Please try again later.");
			return tempOntologyInfo;
		}
		

		String full_path;
		
		Pattern pat = Pattern.compile(".*<filename>([^<]+)</filename>");
		Matcher matcher = pat.matcher(uploadResults);
		if ( matcher.find() ) {
			full_path = matcher.group(1);
		}
		else {
			log.info("Could not parse uploadResults.");
			tempOntologyInfo.setError("Could not parse uploadResults.");
			return tempOntologyInfo;
		}

		
		File file = new File(full_path);
		
		OntModel model;
		try {
			model = Util2.loadModelWithCheckingUtf8(file);
		}
		catch ( Throwable ex ) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			tempOntologyInfo.setError(error);
			return tempOntologyInfo;
		}
		
		String uriForEmpty = Util2.getDefaultNamespace(model, file, tempOntologyInfo);
		// 2009-12-21: previously returning error if uriForEmpty==null. Not anymore; see below.
		
		tempOntologyInfo.setNamespace(uriForEmpty);
		if ( uriForEmpty != null ) {
			// get shortName as the last piece in the path but discarding any query piece
			String path = uriForEmpty;
			int idx = Math.max(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')), path.lastIndexOf(':'));
			String shortName = idx < 0 ? path : path.substring(idx + 1);
			int idxQ = shortName.lastIndexOf('?');
			if ( idxQ >= 0 ) {
				shortName = shortName.substring(0, idxQ);
			}
			tempOntologyInfo.setShortName(shortName);
		}
		

		if ( includeRdf ) {
			try {
				String rdf = Util2.readRdfWithCheckingUtf8(file);
				tempOntologyInfo.setRdf(rdf);
			}
			catch (Throwable e) {
				String error = "Cannot read RDF model: " +full_path+ " : " +e.getMessage();
				log.info(error, e);
				tempOntologyInfo.setError(error);
				return tempOntologyInfo;
			}
		}
		
		// prepare the rest of the ontology info:
		String error = prepareOntologyInfo(file, tempOntologyInfo);
		if ( error != null ) {
			tempOntologyInfo.setError(error);
		}
	
		return tempOntologyInfo;
	}

	
	/**
	 * Completes the ontology info object by assigning some of the members
	 * except the Rdf string and the new values map.
	 * 
	 * @param file  The ontology file.
	 * @param tempOntologyInfo  The object to be completed
	 */
	private String prepareOntologyInfo(File file, TempOntologyInfo tempOntologyInfo) {
		String full_path = file.getAbsolutePath();
		tempOntologyInfo.setFullPath(full_path);
		
		String uriFile = file.toURI().toString();
		log.info("Loading model: " +uriFile);

		return prepareOntologyInfoFromUri(uriFile, tempOntologyInfo);
	}
	
	/**
	 * Does the preparation by reading the model from the given URI.
	 * @param uriModel URI of the model to be loaded
	 * @param tempOntologyInfo  The object to be completed
	 * @return
	 */
	private String prepareOntologyInfoFromUri(String uriModel, TempOntologyInfo tempOntologyInfo) {
		
		if ( log.isDebugEnabled() ) {
			log.debug("prepareOntologyInfoFromUri: uriModel=" +uriModel);
		}
		
		OntModel model;
		
		try {
			model = JenaUtil.loadModel(uriModel, false);
		}
		catch (Throwable ex) {
			String error = "Unexpected error: " +ex.getClass().getName()+ " : " +ex.getMessage();
			log.info(error);
			return error;
		}
		
		if ( false && log.isDebugEnabled() ) {
			debugOntModel(model);
		}
		
		// set URI equal to the full path:
		tempOntologyInfo.setUri(uriModel);

		Resource ontRes = JenaUtil.getFirstIndividual(model, OWL.Ontology);
		
		//-_getBaseInfoIfNull();
		
		StringBuilder moreDetails = new StringBuilder();
		
		Map<String, Property> uriPropMap = MdHelper.getUriPropMap();
		Map<String,String> originalValues = new HashMap<String, String>();
		
		if ( ontRes != null ) {
			//
			// Get values from the existing ontology resource
			//
			for ( AttrGroup attrGroup : metadataBaseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					
					// get value of MMI property:
					Property mmiProp = uriPropMap.get(attrDef.getUri());
					String prefixedMmi = MdHelper.prefixedName(mmiProp);
					String value = JenaUtil.getValue(ontRes, mmiProp);
					
					// DC equivalent, which is obtained if necessary
					Property dcProp = null;
					
					if (value == null) {
						// try a DC equivalent to use:
						dcProp = MdHelper.getEquivalentDcProperty(mmiProp);
						if ( dcProp != null ) {
							value = JenaUtil.getValue(ontRes, dcProp);
						}
					}
					
					if ( value != null ) {
						
						// get value:
						if ( log.isDebugEnabled() ) {
							log.debug("Assigning: " +attrDef.getUri()+ " = " + value);
						}
						originalValues.put(attrDef.getUri(), value);
						
						// Special case: Omv.acronym/OmvMmi.shortNameUri  
						if ( Omv.acronym.getURI().equals(attrDef.getUri()) ) {
							// add also the value of OmvMmi.shortNameUri:
							String shortNameValue = JenaUtil.getValue(ontRes, OmvMmi.shortNameUri);
							if ( log.isDebugEnabled() ) {
								log.debug("Also assigning " +OmvMmi.shortNameUri.getURI()+ " = " +shortNameValue);
							}
							originalValues.put(OmvMmi.shortNameUri.getURI(), shortNameValue);
						}
						
						

						// add detail:
						if ( dcProp != null ) {
							String prefixedDc = MdHelper.prefixedName(dcProp);
							_addDetail(moreDetails, prefixedMmi, "not present", "Will use " +prefixedDc);
						}
						else {
							_addDetail(moreDetails, prefixedMmi, "present", " ");
						}
					}
					else {
						if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
							if ( dcProp != null ) {
								String prefixedDc = MdHelper.prefixedName(dcProp);
								_addDetail(moreDetails, prefixedMmi, "not present", "and " +prefixedDc+ " not present either");
							}	
							else {
								_addDetail(moreDetails, prefixedMmi, "not present", " not equivalent DC");
							}
						}
					}
				}
			}
		}
		else {
			//
			// No ontology resource. Check required attributes to report in the details:
			//
			for ( AttrGroup attrGroup : metadataBaseInfo.getAttrGroups() ) {
				for ( AttrDef attrDef : attrGroup.getAttrDefs() ) {
					if ( attrDef.isRequired() && ! attrDef.isInternal() ) {
						Property mmiProp = uriPropMap.get(attrDef.getUri());
						String prefixedMmi = MdHelper.prefixedName(mmiProp);
						_addDetail(moreDetails, prefixedMmi, "not present", "required");
					}
				}
			}
		}
		
		// add the new details if any:
		if ( moreDetails.length() > 0 ) {
			String details = tempOntologyInfo.getDetails();
			if ( details == null ) {
				tempOntologyInfo.setDetails(moreDetails.toString());
			}
			else {
				tempOntologyInfo.setDetails(details + "\n" +moreDetails.toString());
			}
		}
		
		tempOntologyInfo.getOntologyMetadata().setOriginalValues(originalValues);
		
		// associate the original base URI:
		String uri = model.getNsPrefixURI("");
		if ( uri != null ) {
			String base_ = JenaUtil2.removeTrailingFragment(uri);
			tempOntologyInfo.setUri(base_);
		}

		// OK:
		return null;
	}

	private void _addDetail(StringBuilder details, String a1, String a2, String a3) {
		String str = a1 + "|" + a2 + "|" + a3; 
		log.info(str);
		details.append(str + "\n");
	}

	
	private void debugOntModel(OntModel model) {
		StmtIterator stmts = model.listStatements();
		while ( stmts.hasNext() ) {
			Statement stmt = stmts.nextStatement();
			log.debug(" #### " +stmt);
		}
	}

}
