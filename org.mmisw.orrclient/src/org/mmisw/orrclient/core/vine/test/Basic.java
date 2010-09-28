package org.mmisw.orrclient.core.vine.test;

import java.io.BufferedReader;
import java.io.StringReader;

import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.vocabulary.Skos;
import org.mmisw.ont.vocabulary.Vine;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import com.hp.hpl.jena.reasoner.rulesys.Rule.Parser;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Basic tests to review the effect of some reasoning rules on SKOS relations.
 * <p>
 * Note, this is not a unit test properly, so it's not named *Test to avoid running it from the
 * build.xml.
 * 
 * <p>
 * TODO This kind of test should probably be moved to the Ont service.
 * 
 * @author Carlos Rueda
 */
public class Basic extends VineTestCase {
	
	private static final String SYM_RULE = "[(?p rdf:type owl:SymmetricProperty) (?a ?p ?b) -> (?b ?p ?a)]";
	private static final String VINE_PREFIX_FOR_RULES = "@prefix vine: <" +Vine.NS+ ">.";
	private static final String VINE_UNREIFICATION_RULE = "[(?m vine:subject ?s) (?m vine:predicate ?p) (?m vine:object ?o) -> (?s ?p ?o)]";
	private static final String RDF_UNREIFICATION_RULE = "[(?m rdf:subject ?s) (?m rdf:predicate ?p) (?m rdf:object ?o) -> (?s ?p ?o)]";
	
	
	private final String namespace1 = "http://example.org/ontologyOne/";
	private final String namespace2 = "http://example.org/ontologyTwo/";
	private final Object[][]  mapps = {
		{ "termAAAAAA", Skos.exactMatch, "termPPPPPP" },
		{ "termBBBBBB", Skos.closeMatch, "termQQQQQQ" },
		{ "termCCCCCC", Skos.relatedMatch, "termRRRRRR" },
	};
	
	public void testSymmetric() throws Exception {
		
		Model model = ModelFactory.createDefaultModel();
		
		for ( int i = 0; i < mapps.length; i ++ ) {
			Resource s = model.createResource(namespace1 + mapps[i][0]);
			Resource o = model.createResource(namespace2 + mapps[i][2]);
			model.add(s, (Property) mapps[i][1], o);
		}
		
		_characterizeSkosRelations(model);
		
		for ( Statement stmt : model.listStatements().toList() ) {
			log.debug("@@ " +stmt);
		}
			

		String rules = SYM_RULE;
		Reasoner ruleReasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		InfModel inf = ModelFactory.createInfModel(ruleReasoner, model);
		for ( Statement stmt : inf.listStatements().toList() ) {
			log.debug("^^ " +stmt);
		}
	}

	private static void _characterizeSkosRelations(Model model) {
		model.add(Skos.exactMatch, RDF.type, OWL.SymmetricProperty);
		model.add(Skos.closeMatch, RDF.type, OWL.SymmetricProperty);
		model.add(Skos.relatedMatch, RDF.type, OWL.SymmetricProperty);
	}

	public void testReification() throws Exception {
		
		final boolean useVine = false;
		
		Model model = ModelFactory.createDefaultModel();
		
		_characterizeSkosRelations(model);
		
		Resource STATEMENT;
		Property SUBJECT;
		Property PREDICATE;
		Property OBJECT;
		
		if ( useVine ) {
			STATEMENT = Vine.Statement;
			SUBJECT = Vine.subject;
			PREDICATE = Vine.predicate;
			OBJECT = Vine.object;
			model.add(Vine.Statement, RDFS.subClassOf, RDF.Statement);
			model.setNsPrefix("vine", Vine.NS);
		}
		else {
			STATEMENT = RDF.Statement;
			SUBJECT = RDF.subject;
			PREDICATE = RDF.predicate;
			OBJECT = RDF.object;
			model.setNsPrefix("rdf", RDF.getURI());
		}
		
		// add the reified mapping statements:
		for ( int i = 0; i < mapps.length; i ++ ) {
			Resource mapping = model.createResource(STATEMENT);
			Resource s = model.createResource(namespace1 + mapps[i][0]);
			Resource o = model.createResource(namespace2 + mapps[i][2]);
			model.add(mapping, SUBJECT, s);
			model.add(mapping, PREDICATE, (Property) mapps[i][1]);
			model.add(mapping, OBJECT, o);
		}
		
		log.debug(JenaUtil2.getOntModelAsString(model, "N3"));
		int noAsserted = 0;
		for ( Statement stmt : model.listStatements().toList() ) {
			log.debug("@@ " +stmt);
			noAsserted++;
		}
		log.debug("Asserted: " +noAsserted);

		
		String rules;
		Reasoner ruleReasoner;
		
		if ( useVine ) {
			rules = VINE_PREFIX_FOR_RULES 
					+ "\n" + VINE_UNREIFICATION_RULE
					+ "\n" + SYM_RULE
			;
			
			Parser parser = Rule.rulesParserFromReader(new BufferedReader(new StringReader(rules)));
			ruleReasoner = new GenericRuleReasoner(Rule.parseRules(parser));
		}
		else {
			rules = RDF_UNREIFICATION_RULE
					+ "\n" + SYM_RULE
			;
			ruleReasoner = new GenericRuleReasoner(Rule.parseRules(rules));
		}
		log.debug("\n" +"RULES:\n" +rules);
		InfModel inf = ModelFactory.createInfModel(ruleReasoner, model);
		log.debug("\n" +JenaUtil2.getOntModelAsString(inf, "N3"));
		int noTotal = 0;
		for ( Statement stmt : inf.listStatements().toList() ) {
			log.debug("^^ " +stmt);
			noTotal++;
		}
		log.debug("Total: " +noTotal);

	}
}

