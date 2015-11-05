## change log ##

* 2015-11-04: orrportal 2.5.2
  - Adjustments re #356 "incorrect handling when multiple ontologies in OntModel"
  	- OrrClientImpl._createOntologyFullyHosted: more direct mechanism to get Ontology resource
  	- OrrClientImpl._createOntologyReHosted:    more direct mechanism to get Ontology resource
  
* 2015-11-02: orrportal 2.5.2
  - remove unused org.mmisw.orrportal.gwt.server.Util
  
* 2015-10-29: orrportal 2.5.1
  - fix #354 "error registering the SSN sensor ontology"
    The createOntologyInfo.getUri() now has precedence to retrieve the ontology resource from the model.

* 2015-10-28: 2.5.0
  - build with java 7; other minor non-code adjustments 
