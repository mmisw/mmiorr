package org.mmisw.iserver.core.vine;

import java.util.ArrayList;
import java.util.List;

import org.mmisw.iserver.core.util.Skos;
import org.mmisw.iserver.core.util.Skos2;
import org.mmisw.iserver.gwt.client.rpc.vine.RelationInfo;

/**
 * Vine related utilities.
 * 
 * @author Carlos Rueda
 */
public class VineUtil {

	private static final String EXACT_MATCH_DESCRIPTION = 
		"The property skos:exactMatch is used to link two concepts, indicating a high degree " +
		"of confidence that the concepts can be used interchangeably across a wide range of " +
		"information retrieval applications. [SKOS Section 10.1] (transitive, symmetric)";
	
	private static final String CLOSE_MATCH_DESCRIPTION = 
		"A skos:closeMatch link indicates that two concepts are sufficiently similar that " +
		"they can be used interchangeably in some information retrieval applications. " +
		"[SKOS Section 10.1] (symmetric)";
	
	private static final String BROAD_MATCH_DESCRIPTION = 
		"'has the broader concept': the second (object) concept is broader than the first " +
		"(subject) concept [SKOS Section 8.1] (infers broaderTransitive, a transitive relation)";
	
	private static final String NARROW_MATCH_DESCRIPTION = 
		"'has the narrower concept': the second (object) concept is narrower than the first " +
		"(subject) concept [SKOS Section 8.1] (infers narrowTransitive, a transitive relation)";
	
	private static final String RELATED_MATCH_DESCRIPTION = 
		"The property skos:relatedMatch is used to state an associative mapping link between " +
		"two concepts. [SKOS Section 8.1] (symmetric)";

	
	/** List of SKOS relationInfos -- created on demand */
	private static List<RelationInfo> skosRelInfos = null;
	
	/** List of SKOS2 relationInfos -- created on demand */
	private static List<RelationInfo> skos2RelInfos = null; 

	
	/**
	 * Gets the default list of RelationInfo's. This list is to be used for the creation of
	 * brand new mapping ontologies
	 */
	public static List<RelationInfo> getDefaultVineRelationInfos() {
		return getVineRelationInfos(true);
	}
	
	
	/**
	 * Gets a list of RelationInfo's.
	 * 
	 * @param useSkos 
	 *              true to use the preferred SKOS namespace (see Skos vocabulary);
	 *              false to use the SKOS2 namespace (see Skos2 vocabulary).
	 *              
	 * @return list of RelationInfo's.
	 */
	public static List<RelationInfo> getVineRelationInfos(boolean useSkos) {
		// TODO: determine mechanism to obtain the list of default mapping relations, for example,
		// from an ontology.
		
		if ( useSkos ) {
			if ( skosRelInfos == null ) {
				skosRelInfos = _createSkosRelationInfos();
			}
			return skosRelInfos;
		}
		else {
			if ( skos2RelInfos == null ) {
				skos2RelInfos = _createSkos2RelationInfos();
			}
			return skos2RelInfos;			
		}
		
	}
	
	
	/** creates the SKOS-based relationInfos */
	private static List<RelationInfo>  _createSkosRelationInfos() {
	
		List<RelationInfo> relInfos = new ArrayList<RelationInfo>();
		
		relInfos.add(new RelationInfo(
				"exactMatch28.png", 
				"exactMatch",
				EXACT_MATCH_DESCRIPTION + "\n\n" + Skos.exactMatch.getURI(),
				Skos.exactMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"closeMatch28.png", 
				"closeMatch",
				CLOSE_MATCH_DESCRIPTION + "\n\n" + Skos.closeMatch.getURI(),
				Skos.closeMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"broadMatch28.png", 
				"broadMatch",
				BROAD_MATCH_DESCRIPTION + "\n\n" + Skos.broadMatch.getURI(),
				Skos.broadMatch.getURI()
		));

		relInfos.add(new RelationInfo(
				"narrowMatch28.png", 
				"narrowMatch",
				NARROW_MATCH_DESCRIPTION + "\n\n" + Skos.narrowMatch.getURI(),
				Skos.narrowMatch.getURI()
		));

		relInfos.add(new RelationInfo(
				"relatedMatch28.png", 
				"relatedMatch",
				RELATED_MATCH_DESCRIPTION + "\n\n" + Skos.relatedMatch.getURI(),
				Skos.relatedMatch.getURI()
		));

		return relInfos;
	}

	
	/** creates the SKOS2-based relationInfos */
	private static List<RelationInfo>  _createSkos2RelationInfos() {
		
		List<RelationInfo> relInfos = new ArrayList<RelationInfo>();
		
		relInfos.add(new RelationInfo(
				"exactMatch28.png", 
				"exactMatch",
				EXACT_MATCH_DESCRIPTION + "\n\n" + Skos2.exactMatch.getURI(),
				Skos2.exactMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"closeMatch28.png", 
				"closeMatch",
				CLOSE_MATCH_DESCRIPTION + "\n\n" + Skos2.closeMatch.getURI(),
				Skos2.closeMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"broadMatch28.png", 
				"broadMatch",
				BROAD_MATCH_DESCRIPTION + "\n\n" + Skos2.broadMatch.getURI(),
				Skos2.broadMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"narrowMatch28.png", 
				"narrowMatch",
				NARROW_MATCH_DESCRIPTION + "\n\n" + Skos2.narrowMatch.getURI(),
				Skos2.narrowMatch.getURI()
		));
		
		relInfos.add(new RelationInfo(
				"relatedMatch28.png", 
				"relatedMatch",
				RELATED_MATCH_DESCRIPTION + "\n\n" + Skos2.relatedMatch.getURI(),
				Skos2.relatedMatch.getURI()
		));
		
		return relInfos;
	}
	
}
