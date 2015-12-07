package org.mmisw.orrclient.core.util.ontinfo;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.orrclient.core.util.Utf8Util;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyInfo;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Ontology info utilities.
 * 
 * @author Carlos Rueda
 */
public class OntInfoUtil {
	
	private static final OntInfo ontInfo = new OntInfo();
	
	
	/**
	 * Populates the list of entities associated with the given ontology. 
	 * @param baseOntologyInfo
	 * @return the given argument
	 * @throws Exception 
	 */
	public static BaseOntologyInfo getEntities(BaseOntologyInfo baseOntologyInfo, OntModel ontModel) throws Exception {
		String ontologyUri = baseOntologyInfo.getUri();

		if ( ontModel == null ) {
			ontModel = loadModel(ontologyUri);
		}

		return ontInfo.getEntities(baseOntologyInfo, ontModel);
	}
	
	/**
	 * Loads an "external" model (meaning the URI is presumably an HTTP URL).
	 * A number of content negotiation attempts are done trying to read a 
	 * feasible format using different headers
	 * 
	 * @param uriModel URI of the model
	 * @return the read model.
	 * @throws Exception If unable to access the URI or read the model.
	 */
	public static OntModel loadExternalModel(String uriModel) throws Exception {
		String[] acceptLangPairs = {
			//  mime type,              Lang
				"application/rdf+xml", "RDF/XML",
				"text/turtle",         "N3",
				"text/plain",          "N-TRIPLES",
		};
		
		HttpClient client = new HttpClient();
	    
		// for the thrown exception in case we are unable to read the model:
		StringBuilder tryMsgs = new StringBuilder("Unable to read ontology from URI='" +uriModel+ "':\n");
		
	    for (int i = 0; i < acceptLangPairs.length; i += 2) {
	    	String acceptEntry = acceptLangPairs[i];
	    	String lang = acceptLangPairs[i + 1];
	    	
	    	tryMsgs.append("Trying accept header: '" +acceptEntry+ "': ");
	    	GetMethod meth = new GetMethod(uriModel);
	    	meth.addRequestHeader("accept", acceptEntry);
		    try {
		        client.executeMethod(meth);
	
		        if (meth.getStatusCode() == HttpStatus.SC_OK) {
		            InputStream is = meth.getResponseBodyAsStream();
		    		OntModel model = createDefaultOntModel();
		    		try {
		    			model.read(is, uriModel, lang);
		    			return model;
		    		}
		    		catch (Throwable ex) {
		    			tryMsgs.append("error reading model: " +ex.getMessage()+ "\n");
		    		}
		        }
		        else {
		        	tryMsgs.append("error loading URI: " +meth.getStatusLine().toString()+ "\n");
		        }
		    }
		    finally {
		        meth.releaseConnection();
		    }
	    }
	    throw new Exception(tryMsgs.toString());
	}
		

	/**
	 * Loads a model first verifying the source text is in UTF-8.
	 * @param uriModel
	 * @return
	 * @throws Exception
	 */
	private static OntModel loadModel(String uriModel) throws Exception {
		log.debug("Loading model '" + uriModel + "' with processImports=" +false);
		URL url = new URL(uriModel);
		InputStream is = url.openStream();
		
		byte[] bytes = IOUtils.toByteArray(is);
		Utf8Util.verifyUtf8(bytes);
		
		OntModel model = createDefaultOntModel();

		// #311
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(false);

		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		
		StringReader sr = new StringReader(new String(bytes, "UTF-8"));
		
//		model.read(uriModel);
		model.read(sr, uriModel);
		
		return model;
	}

	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}

	private static final Log log = LogFactory.getLog(OntInfoUtil.class);
}

