package org.mmisw.ont.graph.allegro;

/**
 * Some supporting definitions.
 * 
 * <p>
 * Note that AllegroGraph does not (directly) support owl:SymmetricProperty for inferencing purposes.
 * However, it does support owl:inverseOf, so for a property P that is symmetric,
 * the following assertion is defined here: P owl:inverseOf P.
 * 
 * <p>
 * In particular, we use this mechanism for the SKOS relations, eg.,
 * skos:exactMatch owl:inverseOf skos:exactMatch
 * 
 * <p>
 * *NOTE*: However, (P owl:inverseOf P) is not processed by AG, which seem to be a bug! 
 * I've reported this to the AG people.
 * 
 * @author Carlos Rueda
 */
class AgSupport {

	static final String[][] SUPPORTING_STATEMENTS = {
	
		// NOTE: If a new version of Allegrograph does support owl:SymmetricProperty directly,
		// the corresponding owl:inverseOf statements below can be removed.
		
		//////// SKOS
		{ "!skos:exactMatch", "!owl:inverseOf", "!skos:exactMatch" },
		{ "!skos:closeMatch", "!owl:inverseOf", "!skos:closeMatch" },
		{ "!skos:relatedMatch", "!owl:inverseOf", "!skos:relatedMatch" },
		// also add the owl:SymmetricProperty statements even though not supported by AG:
		{ "!skos:exactMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos:closeMatch", "!rdf:type", "!owl:SymmetricProperty" },
		{ "!skos:relatedMatch", "!rdf:type", "!owl:SymmetricProperty" },
		
		{ "!skos:exactMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:broadMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:narrowMatch", "!rdf:type", "!owl:TransitiveProperty" },
		{ "!skos:broadMatch", "!owl:inverseOf", "!skos:narrowMatch" },
		
		//////// SKOS2
		{ "!skos2:exactMatch", "!owl:inverseOf", "!skos2:exactMatch" },
		{ "!skos2:closeMatch", "!owl:inverseOf", "!skos2:closeMatch" },
		{ "!skos2:relatedMatch", "!owl:inverseOf", "!skos2:relatedMatch" },
		// also add the owl:SymmetricProperty statements even though not supported by AG:
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
