org.mmisw.ont - Ontology URI resolver 
Carlos Rueda - carueda@mbari.org

org.mmisw.ont is a Web application to dereference Ontology URIs against the
"aquaportal" database. The application is packaged as "ont.war" so it gets
appropriately deployed in the tomcat container, that is, the "ont" part in 
the address of the service coincides with the <ontologiesRoot> component of 
the ontology URIs as described in the MMI recommendation. 

NOTE: URI <version> component not yet handled.

* Using the service

For the following description, I use our deployment in http://mmisw.org/ont.
(Note that the code is written in a way that is independent of the actual server 
and root components in the URI.)

The central functionality is that any URI that starts with "http://mmisw.org/ont/"
is resolved by this service, for example: http://mmisw.org/ont/mmi/someVocab.owl.
Resolution means that the given URI is used to search for the corresponding
ontology in the database. If found, the contents of the ontology is returned to
the client in the appropriate format (currently, only the original format typically
RDF/XML).

As a convenience, the parameter "info" can be added to the URI to retrieve general
information about the requested URI, in particular, it shows the parse result 
according to the MMI recommendation (not fully implemented yet), as well as some 
attributes in the database that are relevant to locate the uploaded ontology file. 

For example, the request:

  http://mmisw.org/ont/mmi/someVocab.owl/someTerm?info

gets an HTML response that looks like:

	Full requested URI: http://mmisw.org/ont/mmi/someVocab.owl/someTerm
	
	Parse result:
	       Ontology URI: http://mmisw.org/ont/mmi/someVocab.owl
	          authority: mmi
	              topic: someVocab.owl
	               Term: someTerm
	
	Database result: ERROR: Ontology not found by the given URI. 
	


* Creating the WAR file:

  - Copy sample.build.properties as build.properties.
  - Edit build.properties to adjust any properties.
  - Run Apache Ant: 
	   ant
The generated file is _generated/ont.war.

* Current design

Main classes in package org.mmisw.ont:
	- UriResolver: the main servlet.
	- OntConfig: configuration.
	- MmiUri: helper class to decompose a requested URI.
	- Ontology: basic info about an ontology retrieved from the database.
	- Db: a helper class to interact with the database.

See ChangeLog.txt

