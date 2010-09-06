package org.mmisw.orrclient.core.util.ontinfo;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;

import org.apache.commons.io.IOUtils;
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
	
	// the actual implementation (TODO remove old impl OntInfoOld )
	private static final IOntInfo impl = new OntInfo();
	
	
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

		return impl.getEntities(baseOntologyInfo, ontModel);
	}
	

	/**
	 * Loads a model first verifying the source text is in UTF-8.
	 * @param uriModel
	 * @return
	 * @throws Exception
	 */
	private static OntModel loadModel(String uriModel) throws Exception {
		
		URL url = new URL(uriModel);
		InputStream is = url.openStream();
		
		byte[] bytes = IOUtils.toByteArray(is);
		Utf8Util.verifyUtf8(bytes);
		
		OntModel model = createDefaultOntModel();
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
}

