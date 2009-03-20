org.mmisw.ontmd: Ontology Metadata Manager and Voc2RDF
Carlos Rueda  -  http://marinemetadata.org
October 2008

See also ChangeLog.txt


* Want to build and deploy this application?

Prerequisites:
	Third-party:
		- Google Web Toolkit
		- Java compiler
		- Apache Ant
	 MMI developed:
	 	- The "ont" service deployed in the target application server context

Then:
	- copy sample.build.properties into build.properties 
	- edit build.properties and adjust any necessary parameters
	- edit version.properties if you want to change the version
	- run ant to create the WAR file _generated/ontmd.war
	- use your application server to deploy ontmd.war

Enjoy!
