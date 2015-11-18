## change log ##

* 2015-11-18: ont\&orrportal 2.5.5
  - Fix \#311 "strange case when mapping to http://mmisw.org/ont/cf" 
  	- disable loading of imported ontologies in two places
		- OntClientUtil.retrieveModel
		- OntInfoUtil.loadModel
		so the UI responsive again (in particular with http://mmisw.org/ont/bodc/MMI_Cf_2_NVS2_mapping)
		(My recollection is that we are in general avoiding loading any imports
		when processing any ontology; not sure why those places missed the setting.)
	- in BaseOntInfo._createMappingOntologyData fix extraction of namespaces from the term 
	  URIs having a trailing slash (or hash). Now the resulting local name must be non-empty, meaning
	  that a trailing slash (or hash) in the URI will *not* determine the extraction of the 
	  namespace, but rather any previous such separator.
	  
	- Related adjustments:
		- MappingDataCreationInfo: use "URI" terminology (referring to the mapped ontologies) as opposed to "namespace". 
          This is more consistent with the actual effect of creating the owl:Import's, which are for actual ontology URIs, not namespaces
  
* 2015-11-15: ont\&orrportal 2.5.4
  - preparations for #353 "streamline installation of ORR system"
    - use build.properties as master and local.build.properties for local overwrites
	
* 2015-11-11: orrportal 2.5.4
  - implement #351 "Branding"
    - new "branding.logo" configuration parameter that allows to overwrite the default MMI ORR logo
    - Application name in the footer prefixed with "Powered by" if branding.logo is given
    - new "branding.app.title" configuration parameter that allows to overwrite `<title>` of the page
    - new "branding.tou.url" configuration parameter that allows to overwrite the "Terms of Use" link.
      Only has effect if branding.logo is given.
    
  - preparations for #353 "streamline installation of ORR system"
    - simpler handling of the "ga.uanumber" build property. As part of this, remove PortalConfig, not needed anymore.
    - remove "orrportal.resourcetype.class" and "orrportal.authority.class" build properties; they are
      now specified in (template.)mmiorr.conf 
  	- remove unused "appserver.host" build parameter; remove obsolete ont-browser reference; simplify PortalBaseInfo
  
  - remove obsolete OrrClientVersion
  - version/build properties now captured in a version.properties file in the classpath
  - simplify OrrServiceImpl by directly interacting with the IOrrClient implementation (avoiding OrrClientProxy)
  
* 2015-11-10: orrportal 2.5.4
  - preparations for #353 "streamline installation of ORR system"
  	- add template.mmiorr.conf and typesafe config library
  	- add application.conf and OrrConfig
  	- remove "orrportal.pre.uploads.dir" build property; it's now a runtime property dependent on workspace directory.
  
* 2015-11-06: ont\&orrportal 2.5.3
  - refactoring in orrportal to facilitate possible reimplementation of interface against repository
  	- extract Manager from IOrrClient such that it becomes easier to replace the implementation in 
  	  new factory OrrClientFactory.
    - extract some basic definitions from ont-client library into a new ont-defs library.
  
* 2015-11-06: ont\&orrportal 2.5.2
  - fix #359 "namespace for empty prefix not retained in re-hosted registration"
    Also verified fully-hosted submission continue to work correctly (in this case, as usual, with 
    translation to using the `/` fragment separator regardless of original separator.
    
* 2015-11-05: ont\&orrportal 2.5.2
  - more adjustments to fix #356 "incorrect handling when multiple ontologies in OntModel"
  	 
* 2015-11-04: orrportal 2.5.2
  - Adjustments re #356 "incorrect handling when multiple ontologies in OntModel"
    - more use of Util2.getDefaultNamespace2, which only tries namespace of empty prefix and xml:base
  	- OrrClientImpl._createOntologyFullyHosted: more direct mechanism to get Ontology resource
  	- OrrClientImpl._createOntologyReHosted:    more direct mechanism to get Ontology resource
  
* 2015-11-02: orrportal 2.5.2
  - remove unused org.mmisw.orrportal.gwt.server.Util
  
* 2015-10-29: orrportal 2.5.1
  - fix #354 "error registering the SSN sensor ontology"
    The createOntologyInfo.getUri() now has precedence to retrieve the ontology resource from the model.

* 2015-10-28: 2.5.0
  - build with java 7; other minor non-code adjustments 
