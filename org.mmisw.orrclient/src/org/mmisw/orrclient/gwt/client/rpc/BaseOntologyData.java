package org.mmisw.orrclient.gwt.client.rpc;

import java.io.Serializable;
import java.util.List;

/**
 * Base data for the contents of an ontology.
 * @author Carlos Rueda
 */
public class BaseOntologyData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	
	private List<IndividualInfo> individuals;

	private List<PropertyInfo> properties;

	private List<ClassInfo> classes;
	
	
	public BaseOntologyData() {
	}


	/**
	 * @return the individuals
	 */
	public List<IndividualInfo> getIndividuals() {
		return individuals;
	}


	/**
	 * @param individuals the individuals to set
	 */
	public void setIndividuals(List<IndividualInfo> individuals) {
		this.individuals = individuals;
	}


	/**
	 * @return the properties
	 */
	public List<PropertyInfo> getProperties() {
		return properties;
	}


	/**
	 * @param properties the properties to set
	 */
	public void setProperties(List<PropertyInfo> properties) {
		this.properties = properties;
	}


	/**
	 * @return the classes
	 */
	public List<ClassInfo> getClasses() {
		return classes;
	}


	/**
	 * @param classes the classes to set
	 */
	public void setClasses(List<ClassInfo> classes) {
		this.classes = classes;
	}
	
	


}
