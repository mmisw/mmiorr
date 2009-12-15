package org.mmisw.ont.util;

import java.io.PrintWriter;

import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.util.DotGenerator.What;

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
		
		// TODO make this a parameter
		What whatDiagram = What.CLASS_INSTANCE_DIAGRAM;   
		
		boolean includeLegend = false; // TODO make this a parameter

		boolean useLabel = true; // TODO make this a parameter

		boolean ignoreRdfsComment = true; // TODO make this a parameter
		
		
		if (args.length == 0 ) {
			args = new String[] {
					
					"file:///Users/carueda/mmiworkspace/org.mmisw.ontmd2/onts/mmiorr.owl"
//					"http://xmlns.com/foaf/spec/index.rdf"

//					"file:////Users/carueda/Desktop/OntDev/obj_vs_datatype/approach_1.owl"
//					"file:////Users/carueda/Desktop/OntDev/obj_vs_datatype/approach_2.owl"
//					"file:////Users/carueda/Desktop/OntDev/obj_vs_datatype/approach_3.owl"
					
//					"file:////Users/carueda/Desktop/OntDev/mydevice1.owl"
//					"file:////Users/carueda/Desktop/OntDev/BobMorris/cesn.rdf"
//					"file:////Users/carueda/Desktop/OntDev/mmi-devices/myOrgDevices.owl"
					
//					"http://compass.edina.ac.uk/ontologies/instruments_v3.owl"
					
//					"file:///Users/carueda/mmiworkspace/ooi-semantic_prototype/src/main/resources/fui.owl"
//					"file:///Users/carueda/mmiworkspace/ooi-semantic_prototype/src/main/resources/cdm.owl"
					
//					"file:///Users/carueda/mmiworkspace/mmisw/device.owl"
					
//					"http://mmisw.org/ont/mmi/device"
//					"file:////Users/carueda/Desktop/OntDev/BobMorris/cesn-stripped.owl"
//					"file:////Users/carueda/Desktop/OntDev/BobMorris/instrumentsByMfr.owl"
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
//					"http://ontoware.org/frs/download.php/663/OMV_v2.4.1.owl"
			};
		}
		else if ( args[0].equals("--help") ) {
			System.out.println("USAGE: DotGeneratorProgram <ontology-uri>");
			return;
		}
		
		String uriModel = args[0];
		Model model = loadModel(uriModel);
		
		DotGenerator dotGenerator = new DotGenerator(model, includeLegend);
		
		dotGenerator.setDiagramType(whatDiagram);
		dotGenerator.setUseLabel(useLabel);
		dotGenerator.setIgnoreRdfsComment(ignoreRdfsComment);
		
		PrintWriter pw = new PrintWriter(System.out, true);
		
		dotGenerator.generateDot(pw, "Input: " +uriModel);
		
	}
	
	private static OntModel loadModel(String uriModel) {
		OntModel model = createDefaultOntModel();
		uriModel = JenaUtil2.removeTrailingFragment(uriModel);
		
		model.setDynamicImports(false);
		model.getDocumentManager().setProcessImports(false);
		
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
