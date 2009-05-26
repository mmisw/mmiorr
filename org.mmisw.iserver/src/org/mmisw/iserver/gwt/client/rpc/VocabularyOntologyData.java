package org.mmisw.iserver.gwt.client.rpc;

import java.io.Serializable;
import java.util.List;

/**
 * TODO
 * @author Carlos Rueda
 */
public class VocabularyOntologyData extends OntologyData {
	private static final long serialVersionUID = 1L;
	
	
	public static class ClassData implements Serializable {
		private static final long serialVersionUID = 1L;
	
		private String classUri;
		
		private List<String> datatypeProperties;
		
		private List<IndividualInfo> individuals;
		
		
		public ClassData() {
		}

		
		public String getClassUri() {
			return classUri;
		}

		public void setClassUri(String classUri) {
			this.classUri = classUri;
		}


		public List<String> getDatatypeProperties() {
			return datatypeProperties;
		}


		public void setDatatypeProperties(List<String> datatypeProperties) {
			this.datatypeProperties = datatypeProperties;
		}


		public List<IndividualInfo> getIndividuals() {
			return individuals;
		}


		public void setIndividuals(List<IndividualInfo> individuals) {
			this.individuals = individuals;
		}
		
		
	}
	
	
	private List<ClassData> classes;
	
	
	public VocabularyOntologyData() {
	}


	public List<ClassData> getClasses() {
		return classes;
	}


	public void setClasses(List<ClassData> classes) {
		this.classes = classes;
	}

}
