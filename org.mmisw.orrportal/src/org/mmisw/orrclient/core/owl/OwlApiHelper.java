package org.mmisw.orrclient.core.owl;

import com.hp.hpl.jena.ontology.OntModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.orrclient.core.util.Utf8Util;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * OWL API related operations
 */
public class OwlApiHelper {
	/**
	 * Reads a model from a text file that can be parsed by the OWL API library.
	 * Internally it loads the file and then saves it in RDF/XML to then use
	 * Jena to load this converted version.
	 *
	 * @param file the file
	 * @return Corresponding jena OntModel
	 * @throws IOException
	 */
	public static OntModel loadOntModel(File file) throws Exception {
		Utf8Util.verifyUtf8(file);

		if (log.isDebugEnabled()) log.debug("Owl.loadOntModel: loading file=" + file);
		OWLOntologyManager m = OWLManager.createOWLOntologyManager();
		OWLOntology o = m.loadOntologyFromOntologyDocument(file);

		File rdfFile = new File(file.getParent(), file.getName() + ".owl");
		if (log.isDebugEnabled()) log.debug("Owl.loadOntModel: saving RDF/XML in =" + rdfFile);
		try (FileOutputStream fos = new FileOutputStream(rdfFile)) {
			m.saveOntology(o, new RDFXMLOntologyFormat(), fos);
		}

		String uriFile = rdfFile.toURI().toString();
		if (log.isDebugEnabled()) log.debug("Owl.loadOntModel: now loading using Jena uriFile=" + uriFile);
		try {
			return JenaUtil2.loadModel(uriFile, false);
		}
		catch (Throwable ex) {
			String error = ex.getClass().getName()+ " : " +ex.getMessage();
			log.error(error, ex);
			throw new IOException(error);
		}
	}

	private static final Log log = LogFactory.getLog(OwlApiHelper.class);

	private OwlApiHelper() {}
}
