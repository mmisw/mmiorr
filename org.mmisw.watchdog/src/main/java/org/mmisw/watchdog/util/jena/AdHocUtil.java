package org.mmisw.watchdog.util.jena;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Misc utilities.
 * Includes copies of some available utilities in other modules (eg., JenaUtil2, OntModelUtil), 
 * which I have had no time to properly re-use as they are not yet maven-ized.
 * 
 * <p>
 * TODO Reuse utilities in other modules when maven-ized.
 * 
 * @author Carlos Rueda
 */
public class AdHocUtil {

	/** See OntModelUtil in iserver module */
	public static OntModel loadModel(String uriModel, boolean processImports) {
		OntModel model = null;
		uriModel = _removeTrailingFragment(uriModel);
		model = createDefaultOntModel();
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(processImports);
		model.read(uriModel);
		return model;
	}
	
	// from JenaUtil2
	private static String _removeTrailingFragment(String uri) {
		return uri.replaceAll("(/|#)+$", "");
	}
	
	// form OntModelUtil
	private static OntModel createDefaultOntModel() {
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager docMang = new OntDocumentManager();
		spec.setDocumentManager(docMang);
		OntModel model = ModelFactory.createOntologyModel(spec, null);
		// removeNotNeccesaryNamespaces(model);

		return model;
	}

}
