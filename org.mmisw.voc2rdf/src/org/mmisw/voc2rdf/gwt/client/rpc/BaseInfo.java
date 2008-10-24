package org.mmisw.voc2rdf.gwt.client.rpc;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Provides the main elements used to create the attributes to
 * be captured.
 * 
 * @author Carlos Rueda
 */
public class BaseInfo implements IsSerializable {
	
	// metadata related
	private List<Attribute> mdAttributes = new ArrayList<Attribute>();
	private List<PrimaryConcept> primaryConcepts = new ArrayList<PrimaryConcept>();
	
	// vocabulary related
	private List<Attribute> vocAttributes = new ArrayList<Attribute>();
	private List<ColumnSeparator> columnSeparators = new ArrayList<ColumnSeparator>();
	
	
	public BaseInfo() {
		prepareMdAttributes();
		preparePrimaryConcepts();
		
		prepareVocAttributes();
		prepareColumnSeparators();
	}
	
	/** Gets the metadata attributes */
	public List<Attribute> getAttributes() {
		return mdAttributes;
	}
	
	/** Gets the vocabulary attributes */
	public List<Attribute> getVocAttributes() {
		return vocAttributes;
	}
	
	public List<PrimaryConcept> getPrimaryConcepts() {
		return primaryConcepts;
	}

	public List<ColumnSeparator> getColumnSeparators() {
		return columnSeparators;
	}


	
	private void prepareMdAttributes() {
		Attribute attr;
		
		mdAttributes.add(attr = new Attribute("title", "Title:", "TODO", "Project X Parameters"));
		
		mdAttributes.add(attr = new Attribute("creator", "Creator's name:", "TODO", "John Smith"));
		
		mdAttributes.add(attr = new Attribute("contributor", "Contributor(s):", "TODO"));
		attr.setRequired(false);
		
		mdAttributes.add(attr = new Attribute("orgAbbreviation", "Abbreviation of your organization:", "TODO", "mmi"));
		
		mdAttributes.add(attr = new Attribute("description", "Description:", "TODO", "parameters used in project X"));
		
		mdAttributes.add(attr = new Attribute("primaryConcept", "Main theme of this vocabulary:", "TODO", "parameter"));
		
	}

	private void preparePrimaryConcepts() {
		primaryConcepts.add(new PrimaryConcept("parameter", "Parameters (It will include terms like 'sea surface salinity')"));
		primaryConcepts.add(new PrimaryConcept("sensorType", "Sensor types (It will include terms like 'Thermometer')"));
		primaryConcepts.add(new PrimaryConcept("platform types", "Platform types (It will include terms like 'Mooring')"));
		primaryConcepts.add(new PrimaryConcept("units", "Units  (It will include terms like 'meter')"));
		primaryConcepts.add(new PrimaryConcept("keyword", "Keywords  (It will include terms like 'climate', 'oceans')"));
		primaryConcepts.add(new PrimaryConcept("organization", "Organizations  (It will include terms like 'MBARI' or 'MMI')"));
		primaryConcepts.add(new PrimaryConcept("process", "Process  (It will include terms like 'data quality control')"));
		primaryConcepts.add(new PrimaryConcept("missingflag", "Missing flags  (It will include terms like '-999')"));
		primaryConcepts.add(new PrimaryConcept("qualityflag", "Quality flags  (It will include terms like '10')"));
		primaryConcepts.add(new PrimaryConcept("featureType", "Feature types  (It will include terms like 'body of water')"));
		primaryConcepts.add(new PrimaryConcept("GeographicFeature", "Geographic features  (It will include terms like 'Monterey Bay')"));
				
	}

	
	private void prepareVocAttributes() {
		Attribute attr;
		
		vocAttributes.add(attr = new Attribute("ascii", "Vocabulary in text format:", "TODO",
				"name,description\n" +
				"sea surface salinity, salinity at the sea surface >10 m.\n" +
				"sst, sea surface temperature\n"
				));
		
		vocAttributes.add(attr = new Attribute("fieldSeparator", "Column character separator:", "TODO", "csv"));
		
		vocAttributes.add(attr = new Attribute("namespaceRoot", "Namespace root:", "TODO", "http://mmisw.org/ont"));
	}


	private void prepareColumnSeparators() {
		columnSeparators.add(new ColumnSeparator("csv", "comma"));
		columnSeparators.add(new ColumnSeparator("tab", "tabulator"));
	}

}
