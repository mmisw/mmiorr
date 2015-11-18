package org.mmisw.orrclient.core.util.ontinfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mmisw.ont.vocabulary.Skos;
import org.mmisw.orrclient.core.vine.VineUtil;
import org.mmisw.orrclient.gwt.client.rpc.BaseOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.ClassInfo;
import org.mmisw.orrclient.gwt.client.rpc.IndividualInfo;
import org.mmisw.orrclient.gwt.client.rpc.MappingOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OntologyData;
import org.mmisw.orrclient.gwt.client.rpc.OtherOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.PropValue;
import org.mmisw.orrclient.gwt.client.rpc.PropertyInfo;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData;
import org.mmisw.orrclient.gwt.client.rpc.VocabularyOntologyData.ClassData;
import org.mmisw.orrclient.gwt.client.rpc.vine.Mapping;
import org.mmisw.orrclient.gwt.client.rpc.vine.RelationInfo;

import com.hp.hpl.jena.vocabulary.RDFS;


/**
 * Base implementation with common stuff.
 *
 * @author Carlos Rueda
 */
abstract class BaseOntInfo implements IOntInfo {

	// Addresses in part #290 - "Alphabetized order while editing vocabulary table" but
	// the effect here is just to display the table in order; the actual editing will
	// not necessarily preserve the order of the terms mainly upon row insertions.
	private static final Comparator<IndividualInfo> individualComparator = new Comparator<IndividualInfo>() {
		public int compare(IndividualInfo arg0, IndividualInfo arg1) {
			return arg0.getLocalName().compareToIgnoreCase(arg1.getLocalName());
		}
	};

	/**
	 * Helper to get the localName of an entity given the entityUri and
	 * the URI of an ontology to see if the entity "belongs" to the ontology.
	 *
	 * @param entityUri
	 * @param ontologyUri
	 * @return
	 */
	protected static String _getLocalName(String entityUri, String ontologyUri) {
		String localName;
		// is ontologyUri a prefix of entityUri?
		if ( entityUri.indexOf(ontologyUri) == 0 ) {
			localName = entityUri.substring(ontologyUri.length());
			// remove any leading separator:
			localName = localName.replaceAll("^(/|#)+", "");
		}
		else {
			// use the given entityUri to extract the local name.
			localName = _getLocalName(entityUri);
		}
		return localName;
	}

	/**
	 * Helper to obtain the localname from a URI.
	 * This is the last suffix after the rightmost slash (/) or hash (#).
	 *
	 * <p>
 	 * Examples:<br/>
 	 * uri="http://example.org/onts/myont#someterm" result="someterm" <br/>
 	 * uri="http://example.org/otherterm" result="otherterm" <br/>
	 *
	 * @param uri
	 * @return  the last suffix after the rightmost slash (/) or hash (#),
	 *         or the same given uri if none of these characters is present.
	 */
	protected static String _getLocalName(String uri) {
		int idx_slash = uri.lastIndexOf('/');
		int idx_hash = uri.lastIndexOf('#');
		if ( idx_slash >= 0 || idx_hash >= 0 ) {
			int idx = Math.max(idx_slash, idx_hash);
			String localName = uri.substring(idx + 1);
			return localName;
		}
		else {
			return uri;
		}
	}

	/**
	 * It assigns the classInfo corresponding to the domain for each property.
	 * @param classes      List of known classes
	 * @param properties   Properties to be updated
	 */
	protected static void _setDomainClassesForProperties(List<ClassInfo> classes,
			List<PropertyInfo> properties) {

		for ( PropertyInfo propertyInfo : properties ) {
			String domainClassUri = propertyInfo.getDomainUri();

			if ( domainClassUri == null ) {
				continue;
			}

			// search corresponding classInfo in classes:
			ClassInfo domainClassInfo = null;

			for ( ClassInfo classInfo : classes ) {
				if ( domainClassUri.equals(classInfo.getUri()) ) {
					domainClassInfo = classInfo;
					break;
				}
			}

			if ( domainClassInfo != null ) {
				propertyInfo.setDomainClassInfo(domainClassInfo);
			}
		}
	}


	protected static OntologyData _createMappingOntologyData(
			BaseOntologyData baseOntologyData,
			List<Mapping> mappings,
			List<IndividualInfo> individuals
	) {

		List<String> namespaces = new ArrayList<String>();

		// add the namespaces corresponding to the already provided mappings:
		// 299: Not consistent assignment of short-hand prefixes in mapping tool
		// To fix this, first scan the mapping subjects and then the objects so
		// the first short-hand letters are given to the left hand-side in the ORR
		// interface
		for ( Mapping mapping : mappings ) {
			_addNamespace(namespaces, mapping.getLeft(), null);
		}
		for ( Mapping mapping : mappings ) {
			_addNamespace(namespaces, mapping.getRight(), null);
		}

		// true: assume this ontology uses relation is the Skos namespace (and not Skos2, specifically).
		// This flag is to determine which list of relationInfos to associate.
		boolean useSkos = true;
		for ( Mapping mapping : mappings ) {
			if ( ! mapping.getRelation().startsWith(Skos.NS) ) {
				// there is a mapping with a relation not in the Skos.NS namespace
				useSkos = false;
				//
				// NOTE: We don't consider the potential case of a mixture of Skos and
				// Skos2 namespaces (and any other namespaces for that matter).
				// This again is because we assume that Vine-created ontologies are normally to
				// be handled by Vine itself principally.
				//
				break;
			}
		}
		// get corresponding RelationInfos
		List<RelationInfo> relInfos = VineUtil.getVineRelationInfos(useSkos);

		MappingOntologyData ontologyData = new MappingOntologyData();
		ontologyData.setNamespaces(namespaces);
		ontologyData.setMappings(mappings);
		ontologyData.setRelationInfos(relInfos);
		ontologyData.setBaseOntologyData(baseOntologyData);

		return ontologyData;
	}

	/** adds the namespace associated with the uri to the given set.
	 * It uses the given localName as a basis if non-null;
	 * otherwise it gets the local name from the uri as the last non-empty
	 * (see #311) fragment starting right after a slash or hash.
	 *
	 * It does nothing if uri starts with "urn:" (ignoring case).
	 */
	private static void _addNamespace(List<String> namespaces, String uri, String localName) {

		if ( uri == null || uri.toLowerCase().startsWith("urn:") ) {
			return;
		}


		String ns;
		final int uriLen = uri.length();
		if ( localName == null ) {
			// #311: the -2 below is to exclude last character in case it is '/' or '#' so
			// we avoid ending up with empty local names in the rest of the processing
			int idx_slash = uri.lastIndexOf('/', uriLen - 2);
			int idx_hash = uri.lastIndexOf('#', uriLen - 2);
			if ( idx_slash >= 0 || idx_hash >= 0 ) {
				int idx = Math.max(idx_slash, idx_hash);
				//localName = uri.substring(idx);
				ns = uri.substring(0, idx);
			}
			else {
				//localName = "";
				ns = uri;
			}
		}
		else {
			int locLen = +1 + localName.length();   // +1 to also omit the separator
			ns = uriLen > locLen ? uri.substring(0, uriLen - locLen) : "";
		}

		ns = ns.trim();
		if ( ns.length() > 0 && ! namespaces.contains(ns) ) {
			namespaces.add(ns);
		}
	}




	protected static OntologyData _createVocabularyOntologyData(BaseOntologyData baseData) {
		VocabularyOntologyData ontologyData = new VocabularyOntologyData();

		ontologyData.setBaseOntologyData(baseData);


		Map<String, ClassData> classMap = new HashMap<String, ClassData>();

		List<PropertyInfo> properties = baseData.getProperties();
		for ( PropertyInfo entity : properties ) {
			if ( ! entity.isDatatypeProperty() ) {
				continue;
			}

			String classUri = entity.getDomainUri();
			if ( classUri == null ) {
				continue;
			}

			ClassData classData = classMap.get(classUri);
			if ( classData == null ) {
				classData = new ClassData();
				classMap.put(classUri, classData);
				classData.setClassUri(classUri);
				classData.setClassInfo(entity.getDomainClassInfo());
				classData.setDatatypeProperties(new ArrayList<String>());
			}

			classData.getDatatypeProperties().add(entity.getLocalName());
		}

		// add the found classes and add corresponding individuals:

		List<ClassData> classes = new ArrayList<ClassData>();
		ontologyData.setClasses(classes);

		for ( String classUri : classMap.keySet() ) {
			ClassData classData = classMap.get(classUri);
			classes.add(classData);

			// add individuals whose type is classUri

			List<IndividualInfo> individuals = new ArrayList<IndividualInfo>();
			classData.setIndividuals(individuals);

			List<IndividualInfo> individualInfos = baseData.getIndividuals();

			Collections.sort(individualInfos, individualComparator);

			for ( IndividualInfo individualInfo : individualInfos ) {
				String individualClass = individualInfo.getClassUri();
				if ( classUri.equals(individualClass) ) {
					individuals.add(individualInfo);
				}
			}

			_putKeyColumnAsFirst(classData, individuals);
		}

		return ontologyData;
	}


	/**
	 * the following is an attempt to guess the datatype property that was used
	 * as the 'key', so as to put that column as the first.
	 * The strategy is to see what datatype property corresponds to rdfs:label.
	 *
	 * <p>
	 * TODO Remove this mechanims once #240 "preserve column order" is implemented.
	 *
	 * @param classData
	 * @param individuals
	 */
	private static void _putKeyColumnAsFirst(ClassData classData, List<IndividualInfo> individuals) {
		// diffFlags[col] will be true if the corresponding column does not seem to coincide
		// with value of rdfs:label
		boolean[] diffFlags = null;

		// but we do the check for a maximum of individuals:
		final int maxIndivs = 20;
		int indivNum = 0;
		for ( IndividualInfo individualInfo : individuals ) {

			// will contain the value of RDFS.label.getURI() if any:
			String rdfsLabelValue = null;

			Map<String, String> vals = new HashMap<String, String>();
			List<PropValue> props = individualInfo.getProps();
			for ( PropValue pv : props ) {

				if ( RDFS.label.getURI().equals(pv.getPropUri()) ) {
					rdfsLabelValue = pv.getValueName();
				}

				vals.put(pv.getPropName(), pv.getValueName());
			}

			if ( rdfsLabelValue == null ) {
				// do not continue making the check.
				break;
			}

			// let's ignore case, and replace spaces with underscores for purposes of
			// the comparison below
			rdfsLabelValue = rdfsLabelValue.toLowerCase().replace(' ', '_');

			List<String> datatypeProperties = classData.getDatatypeProperties();
			int numCols = datatypeProperties.size();
			if ( diffFlags == null ) {
				diffFlags = new boolean[numCols];
			}
			for ( int i = 0; i < numCols; i++ ) {
				String colValue = vals.get(datatypeProperties.get(i));
				if ( colValue != null ) {
					colValue = colValue.toLowerCase().replace(' ', '_');
				}
				diffFlags[i] = diffFlags[i] || !rdfsLabelValue.equals(colValue);
			}

			if ( ++indivNum >= maxIndivs ) {
				break;
			}
		}

		if ( diffFlags != null ) {
			// now, pick first column whose values coincided with rdfs:label:
			int foundColumn = -1;
			for ( int i = 0; i < diffFlags.length; i++ ) {
				if ( !diffFlags[i] ) {
					foundColumn = i;
					break;
				}
			}

			if ( foundColumn > 0 ) {
				// if we found the column, and that is not already the first, then
				// make it the first:
				List<String> datatypeProperties = classData.getDatatypeProperties();
				String keyColumnName = datatypeProperties.remove(foundColumn);
				datatypeProperties.add(0, keyColumnName);
			}
		}

	}

	protected static OntologyData _createOtherOntologyData(BaseOntologyData baseOntologyData) {
		OtherOntologyData ontologyData = new OtherOntologyData();
		ontologyData.setBaseOntologyData(baseOntologyData);
		// TODO
		return ontologyData;
	}

}

