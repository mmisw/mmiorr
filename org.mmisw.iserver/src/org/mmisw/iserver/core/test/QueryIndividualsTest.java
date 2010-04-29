package org.mmisw.iserver.core.test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.mmisw.iserver.core.util.QueryUtil;
import org.mmisw.iserver.gwt.client.rpc.EntityInfo;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * @author Carlos Rueda
 */
public class QueryIndividualsTest extends TestCase {

	public void test0() throws Exception  {
//    	final String ontologyUri = "http://mmisw.org/ont/ODMT/deploymentterms";
    	final String ontologyUri = "file:///Users/carueda/deploymentterms.rdf";
    	
    	OntModel ontModel = QueryUtil.loadModel(ontologyUri);
    	
    	List<EntityInfo> indivs = new ArrayList<EntityInfo>();
    	
    	indivs.addAll(QueryUtil._getIndividuals(null, ontModel, ontologyUri));
    	
    	System.out.println("indivs = " +indivs.size());
		
    }
}
