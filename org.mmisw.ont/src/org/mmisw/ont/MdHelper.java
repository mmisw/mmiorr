package org.mmisw.ont;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.OWL;

import edu.drexel.util.rdf.JenaUtil;
import edu.drexel.util.rdf.OwlModel;

public class MdHelper {
	private static String title = "Dublin Core attributes";
	
	private static Property[] dcProps = {
		DC.contributor,
		DC.coverage,
		DC.creator,
		DC.date,
		DC.description,
		DC.format,
		DC.identifier,
		DC.language,
		DC.publisher,
		DC.relation,
		DC.rights,
		DC.source,
		DC.subject,
		DC.title,
		DC.type,
	};
	
	private static Map<String,Attribute> _initAttributes(Map<String,Attribute> attributes) {
		for ( Property dcProp : dcProps) {
			attributes.put(dcProp.getLocalName(), new Attribute(dcProp));
		}
		return attributes;	
	}
	
	/**
	 * attributes that can/should be associated
	 */
	private Map<String,Attribute> attributes;
	
	
	/** 
	 * Creates an ontology metadata helper.
	 */
	public MdHelper() {
		if ( attributes == null ) {
			attributes = _initAttributes(new LinkedHashMap<String,Attribute>());	
		}
	}

	public String getTitle() {
		return title;
	}

	/** 
	 * Gets all the attributes
	 * @return All the attributes
	 */
	public Collection<Attribute> getAttributes() {
		return attributes.values();
	}
	
	
	/**
	 * Updates the given model with the non-empty-valued attributes in this
	 * object.
	 * 
	 * @param ontModel The model to be updated.
	 */
	public void updateModel(OntModel ontModel) {

		// see that at least one attribute has a value associated
		boolean hasValues = false;
		for ( Attribute attr : getAttributes() ) {
			String values = attr.getValue();
			if ( values.length() > 0 ) {
				hasValues = true;
				break;
			}
		}
		
		if ( !hasValues ) {
			System.out.println(this.getClass().getName()+ ": no metadata to update in model...");
			return;
		}
		
		System.out.println(this.getClass().getName()+ ": updating metadata in model ...");
		OwlModel newOntModel = new OwlModel(ontModel);
		Ontology ontolgy = newOntModel.createOntology(JenaUtil.getURIForBase(""));
		
		for ( Attribute attr : getAttributes() ) {
			String value = attr.getValue();
			if ( value.length() > 0 ) {
				ontolgy.addProperty(attr.dcProp, value);
			}
		}
	}

	
	/**
	 * Updates the attributes in this object using the metadata in the 
	 * given model.
	 * 
	 * @param model The model to read metadata from.
	 */
	public void updateAttributes(Model ontModel) {

		System.out.println(this.getClass().getName()+ ": updating attributes with model metadata ...");
		
		Resource ontRes = JenaUtil.getFirstIndividual(ontModel, OWL.Ontology);
		
		if ( ontRes == null ) {
			return;
		}
		
		for ( Property dcProp : dcProps ) {
			String value = JenaUtil.getValue(ontRes, dcProp);
			if (value == null) {
				continue;
			}
			//	value = JenaUtil.getBaseURI(ontModel);
			
			Attribute attr = attributes.get(dcProp.getLocalName());
			if ( attr != null ) {
				attr.setValue(value);
			}
			
		}
		System.out.println();
	}

	
	/**
	 * A metadata attribute.
	 * Each attribute has a pre-determined name, but its value can be set.
	 */
	public static class Attribute {
		private Property dcProp;
		private String value = "";
		
		public Attribute(Property dcProp) {
			// TODO Auto-generated constructor stub
			this.dcProp = dcProp;
		}

		public String getLabel() {
			return dcProp.getLocalName();
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String value) {
			this.value = value.trim();
		}
		
	}
	

}
