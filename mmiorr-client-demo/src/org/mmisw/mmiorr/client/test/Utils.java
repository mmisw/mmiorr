package org.mmisw.mmiorr.client.test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * some support utilities for the tests 
 * @author Carlos Rueda
 */
public class Utils {
	
	/** gets a system property */
	public static String getRequiredSystemProperty(String key) {
		String val = System.getProperty(key);
		if ( val == null || val.trim().length() == 0 ) {
			throw new IllegalArgumentException("Required system property '" +key+ "' not specified");
		}
		return val;
	}

	/** reads a model from a file */
	public static OntModel readModel(File file) throws IOException {
		String uriModel = file.toURI().toString();
		OntModel model = ModelFactory.createOntologyModel();
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(false);
		model.read(uriModel);
		return model;
	}
	
	/** reads a model from a string */
	public static OntModel readModel(String contents) throws IOException {
		OntModel model = ModelFactory.createOntologyModel();
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(false);
		model.read(new StringReader(contents), null);
		return model;
	}
}
