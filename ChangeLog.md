## change log ##

* 2016-04-25  orrportal 2.6.4
  - sparql/index.jsp: use yasgui 2.3.1 with fix to https://github.com/OpenTriply/YASGUI/issues/80
  
* 2016-01-02: orrportal 2.6.3
  - include small version of logo (favicon.ico) as part of "Powered by" message
  - add favicon.ico

* 2016-01-01: orrportal 2.6.3: Fix \#400 "more general email settings"
  - MailSender now uses the new properties from configuration ({template.}mmiorr.conf)
  
* 2015-12-08:
  Re \#364 "dispatch portal functionality through ont-based URL directly"
  Although the general desired behavior is already implemented, here are some observations that make the
  completion of this task not as straightforward.
  - While it would be great re-use any login session from "orr" in "ont" (so the user doesn't have 
    to login again), this is not possible as neither app is a subdirectory of the other.
  - As it is right now, once logged in under "ont", the user would have to login again for any other "ont" based URL. 
    A solution to this is to make the "login" action go to a route at the "ont" root so, once the users logs in,
    the session can be inherited for any other sub-path. (Currently the login action opens a dialog.)
  - So far, not too bad. The above would require some additional effort but can be implemented.
  - However, during local testing I've seen an occasional GWT error that seems to happen, in some cases, when trying 
    to navigate to another page while ontology information is being loaded. Unfortunately there's no additional
    information anywhere when this happens!
  
* 2015-12-07: ont\&orrportal 2.6.2
  Preparations for \#364 "dispatch portal functionality through ont-based URL directly"
  - For now the new dispatch in orrportal depends on the auxiliary js global variable `__rUri` being defined. 
    This in turn depends on the new (interim) `OntServlet._364` flag on the Ont module.
    When this is true, instead of redirection, Ont generates HTML with `<base href=".../orr/">` pointing to the 
    portal UI. 
  - Most of the links in orrportal are adjusted for the new dispatch so they reflect the intended "ont" (instead
    of the "orr" for the portal).
  - Pending links include: "advanced sparql UI" (and from there the links back to the main list)
  - Other pending aspects: 
    - for any "bookmarks" out there, redirect from the orrportal to the "ont" service, for example:
      - `http://mmisw.org/orr/#http://mmisw.org/ont/cf/parameter -> http://mmisw.org/ont/cf/parameter` 
      - `http://mmisw.org/orr/#http://purl.oclc.org/NET/ssnx/ssn -> http://mmisw.org/ont?uri=http://purl.oclc.org/NET/ssnx/ssn`
      The idea here is to define a new app only for the purposes of the redirection, while deploying the updated 
      "orrportal" with any name different from the one traditionally used for the portal.

* 2015-12-07: ont\&orrportal 2.6.1
  - Fix \#390 "edit new version of uploaded ontology asks for inline editing of contents"
    PortalMainPanel.editNewVersion: use the associated type when the content hasn't been loaded to determine how
    to dispatch the editing
  - MiscDispatcher.listAll now sets ontology type to "other" for non Ont-resolvable entries (previously it 
    always defaulted to "vocabulary"
  
  - remove some unused/obsolete code
  
* 2015-11-30: orrportal 2.6.0
  - complete \#371 "improve UI for SPARQL queries"
    - in \#st page: msg adjustment; no \_blank target for sparql/ link;
    - in sparql/ page: link to main page.
  
  - Fix \#386 "empty 'Synopsis of ontology contents' for some entries"
    - ontologies displayed with empty synopsis of contents were those having no classes, individuals or properties. 
    - The fix is to also display the subjects
    - in all cases the subsection heading is now included even if with zero elements.
     
  - Fix \#383 "Upload of ontology file without associated URI causes error for re-hosting"
  
* 2015-11-24: orrportal 2.5.9
  - Fix \#258 "reset password in effect but lost if error while sending email"
    Additional handling including a message like "please try again later ..if the problem persists
    contact the administrator." But note that the likely problem is actually from inappropriate configuration.
    
  - Re \#351, several edits in titles and hints to not refer in particular to the "MMI ORR"; instead rely on the branding
    provided by the main logo and title of the page.
  - add notify.recipientsFilename config parameter for filename with list of email addresses to be notified of registrations.
  - configuration additions related with information used in emails
  
  - Fix \#315 "leaving page with edited mappings causes them to be lost"
    PortalMainPanel.onHistoryChanged: add logic to handles the confirmation 
    about abandoning ongoing edits or upload operation upon change in browser history.
    The strategy is to return to the previous history token when the user decides
    to stay in the ongoing edit or upload operation. A flag (returningToOngoingEdit) 
    helps with not doing any of the usual interface preparations upon navigating
    back to the associated previous token.
  - Also consider upload operations in onWindowClosing
    
* 2015-11-23: orrportal 2.5.8
  - Fix \#349 "support OWL/XML format in upload operation (was: errors uploading ice-of-land-origin ontology)"
    - Accept "OWL/XML" as an additional format in the Upload operation
    - Via a new OwlApiHelper class use OWL API library to load the uploaded file and internally convert it 
      to RDF/XML to then simply use Jena to load the model.
    Note: Using OWL API v3.4.5 (2013-07-26); I tried a much more recent version but it brings 
    *a lot* of dependencies and, worse, make the whole ORR application fail to load or 
    operate normally possibly because of conflicts with other already used libraries.
    Didn't investigate much but once I noted that http://mowl-power.cs.man.ac.uk:8080/converter/
    worked fine to convert the ice-of-land-origin ontology and that it said it used OWL API 
    Version 3.4.5-SNAPSHOT, I decided just to try the 3.4.5 version available from the sourceforge 
    download page. Only adding owlapi-distribution-3.4.5.jar to the classpath was enough.
    
    
* 2015-11-20: ont\&orrportal 2.5.7
  - Fix \#350 "search for 'dataset' produces some literals as subjects"
    Simply do a correct parsing of the CSV!
  - BTW, update opencsv to 3.6 from 2.3 (see note in BaseParser)
     
  - implement correct fix to \#366 "space in term URI causes malformed SPARQL query"
    A goodIriCharactersPattern regex is now used to determine which template to use for the entity query.
    In any case, the original requested URI is *not* altered at all!
     
* 2015-11-19: ont\&orrportal 2.5.7
  - Complete \#351 "Branding" and advance \#371 "improve UI for SPARQL queries"
    - orrportal: New query UI under `/sparql/`. Branding elements included (logo, title). 
      This new UI uses [YASGUI](http://doc.yasgui.org/).
    - ont: the old `/sparql.html` now redirects to `${portal.service.url}/sparql/`

* 2015-11-18: ont\&orrportal 2.5.6
  - Fix \#367 "unwanted '.html' on the end"
    ...ont.util.Util.getLink was not considering that the value should be ont-resolvable to append the .html, only that 
    it was an MmiUri (which a URI like http://vocab.nerc.ac.uk/collection/P07/current/M4KOX5A0/ satisfies.
    
  - Fix \#366 "space in term URI causes malformed SPARQL query"
    UriDispatcher now "fixes" each URI prior to instantiating the SPARQL query by replacing spaces with underscores.
    Note: although it would probably be more correct to respond with an explicit error, the practical solution
    is to just replace each space with an underscore and let the query be processed. This could be documented.
    Relevant information:
      - http://stackoverflow.com/a/9056636/830737 
      - http://answers.semanticweb.com/questions/8244/how-do-you-handle-non-iri-compatible-urirefs
    
* 2015-11-18: ont\&orrportal 2.5.6
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
	- Went ahead and implemented my suggested "simple solution": exclude the ontologies themselves in 
	  any found entities so no mappings can be created against them using the Vine interface.
	  
	- Related adjustments:
		- MappingDataCreationInfo: use "URI" terminology (referring to the mapped ontologies) as opposed to "namespace". 
          This is more consistent with the actual effect of creating the owl:Import's, which are for actual ontology URIs, not namespaces
        - re-enabled vine unit tests
        
  
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
