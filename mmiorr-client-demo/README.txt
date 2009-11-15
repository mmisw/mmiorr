org.mmisw.mmiorr.client
Carlos Rueda  -  MBARI
November 2009

Programs demonstrating interaction with MMI ORR

Third-party libraries (included in lib/):
	- apache commons-httpclient
	- apache commons-io
	- apache commons-logging
	- apache commons-codec
	
	
Running RegisterOntology:

This program demonstrates the direct registration of an ontology file in the 
MMI Ontology Registry and Repository.

See properties in build.xml for the expected arguments of the program. Here I'm using the -D
mechanism to pass some args from the command line:

	$ ant -Dusername=carueda -Dpassword=xxxxxx
	Buildfile: build.xml
	
	compile:
	
	run:
	     [java] Executing POST request to http://mmisw.org/orr/direg
	     [java] Response status: 200: OK
	     [java] Response body:
	     [java] <success>
	     [java]   <ontologyUri>http://example.org/test1</ontologyUri>
	     [java]   <graphId>ooi-ci</graphId>
	     [java]   <msg>New ontology registered</msg>
	     [java] </success>
	     [java] 
	     [java] 
	
	BUILD SUCCESSFUL
	Total time: 1 second

The above created a new entry because the ontology URI "http://example.org/test1" was not yet
registerd. Runing the same command again registers a new version:

	$ ant -Dusername=carueda -Dpassword=xxxxxx
	Buildfile: build.xml
	
	compile:
	
	run:
	     [java] Executing POST request to http://mmisw.org/orr/direg
	     [java] Response status: 200: OK
	     [java] Response body:
	     [java] <success>
	     [java]   <ontologyUri>http://example.org/test1</ontologyUri>
	     [java]   <graphId>ooi-ci</graphId>
	     [java]   <msg>New version of ontology registered</msg>
	     [java] </success>
	     [java] 
	     [java] 
	
	BUILD SUCCESSFUL
	Total time: 1 second
