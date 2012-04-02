package org.mmisw.ont.triplestore.allegro4;

/**
 * Some supporting definitions.
 * 
 * @author Carlos Rueda
 */
class AgSupport {

	static final String[][] SUPPORTING_STATEMENTS = {
	
		//////// SKOS
		{ "!skos:exactMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos:closeMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos:relatedMatch", "!rdf:type", "!owl:SymmetricProperty" },
		
		{ "!skos:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:broadMatch", "!owl:inverseOf", "!skos:narrowMatch" },
		
		//////// SKOS2
		{ "!skos2:exactMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos2:closeMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos2:relatedMatch", "!rdf:type", "!owl:SymmetricProperty" },
		
		{ "!skos2:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos2:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos2:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos2:broadMatch", "!owl:inverseOf", "!skos2:narrowMatch" },
		
		//////// RDFG
		{ "!rdfg:subGraphOf", "!rdf:type", "!owl:TransitiveProperty" },

	};

}
