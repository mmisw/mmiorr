package org.mmisw.ont2dot.impl.jena;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * Helper to load models.
 * 
 * @author Carlos Rueda
 */
class ModelLoader {
	static OntModel loadModel(String uriModel, boolean includeImports) {
		OntModel model = createDefaultOntModel();
		uriModel = _removeTrailingFragment(uriModel);
		
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(includeImports);
		
		model.read(uriModel);
		return model;
	}
	
	private static String _removeTrailingFragment(String uri) {
		return uri.replaceAll("(/|#)+$", "");
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
