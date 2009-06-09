package org.mmisw.ont.util;

import java.io.PrintWriter;

import org.mmisw.ont.JenaUtil2;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;


/**
 * Simple program that exercises DotGenerator
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class DotGeneratorProgram {

	/**
	 * Test program.
	 * @param args  An argument indicating the URI of the ontology, or "--help"
	 */
	public static void main(String[] args) {
		
		boolean includeLegend = false; // TODO make this a parameter
		
		if (args.length == 0 ) {
			args = new String[] {
//					"http://mmisw.org/ont/mmi/device"
					"file:///Users/carueda/mmiworkspace/mmisw/device.owl"
//					"file:///Users/carueda/Desktop/OntDev/mmisw/device.owl"
//					"http://mmisw.org/ont/mmi/20090512T011137/device"
//					"http://mmisw.org/ont/univmemphis/20090422T011238/sensor"
//					"file:///Users/carueda/Desktop/instv0.2.owl"
//					"http://sweet.jpl.nasa.gov/1.1/units.owl"
//					"http://sweet.jpl.nasa.gov/2.0/sciUnits.owl"
//					"http://mmisw.org/ont/mmitest/unit"
//					"http://www.w3.org/2005/Incubator/ssn/wiki/images/4/42/SensorOntology20090320.owl.xml"
//					"http://www.cesn.org/sensor/cesn.owl"
//					"file:///Users/carueda/Downloads/BioPortalMetadata_v0.2_v0.2.owl"
//					"http://ontoware.org/frs/download.php/663/OMV_v2.4.1.owl"
//					"http://mmisw.org/ont/mmi/20090519T125341/general"
			};
		}
		else if ( args[0].equals("--help") ) {
			System.out.println("USAGE: DotGeneratorProgram <ontology-uri>");
			return;
		}
		
		String uriModel = args[0];
		Model model = loadModel(uriModel);
		
		DotGenerator dotGenerator = new DotGenerator(model, includeLegend);
		
		PrintWriter pw = new PrintWriter(System.out, true);
		
		dotGenerator.generateDot(pw, "Input: " +uriModel);
		
	}
	
	private static OntModel loadModel(String uriModel) {
		OntModel model = createDefaultOntModel();
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		model.read(uriModel);
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
	
	private DotGeneratorProgram() {}
}
