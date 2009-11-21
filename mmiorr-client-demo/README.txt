org.mmisw.mmiorr.client
Carlos Rueda  -  MBARI
November 2009

Programs demonstrating interaction with MMI ORR

Third-party libraries (included in lib/):
	- apache commons-httpclient
	- apache commons-io
	- apache commons-logging
	- apache commons-codec
	
	- junit, ant-junit (required for unit testing)
	- jena, jenatest, iri, icu4j (Jena libs required for some unit tests)
	
	
- Running RegisterOntology
- Running RetrieveOntology
- Running the tests


Running RegisterOntology:

This program demonstrates the direct registration of an ontology file in the 
MMI Ontology Registry and Repository.

See properties in build.xml for the expected arguments of the program. Here I'm using the -D
mechanism to pass some args from the command line:

	$ ant -Dusername=carueda -Dpassword=xxxxxx register
	Buildfile: build.xml
	
	compile:
	
	register:
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

	$ ant -Dusername=carueda -Dpassword=xxxxxx register
	Buildfile: build.xml
	
	compile:
	
	register:
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


Running RetrieveOntology:

This program demonstrates the access to a registered ontology in 
a desired format from the MMI Ontology Registry and Repository.

$ ant retrieve
Buildfile: build.xml

compile:

retrieve:
     [java] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1&form=owl
     [java] <?xml version="1.0"?>
     [java] <rdf:RDF
     [java]     xmlns="http://example.org/test1/"
     [java]     xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
     [java]     xmlns:omvmmi="http://mmisw.org/ont/mmi/20081020/ontologyMetadata/"
     [java]     xmlns:xsd="http://www.w3.org/2001/XMLSchema#"
     [java]     xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
     [java]     xmlns:owl="http://www.w3.org/2002/07/owl#"
     [java]     xmlns:omv="http://omv.ontoware.org/2005/05/ontology#"
     [java]     xml:base="http://example.org/test1">
     [java]     <owl:Ontology rdf:about="">
     [java]         <omv:hasCreator>CR</omv:hasCreator>
     [java]         <omv:acronym>OOI-CI-text1</omv:acronym>
     [java]         <omv:name>Test 1</omv:name>
     [java]         <omvmmi:hasContentCreator>CR</omvmmi:hasContentCreator>
     [java]         <omv:uri>http://example.org/test1</omv:uri>
     [java]         <omv:description>Test ontology to demonstrate direct registration in the MMI ORR</omv:description>
     [java]         <omvmmi:hasResourceType>test1</omvmmi:hasResourceType>
     [java]     </owl:Ontology>
     [java]     <owl:Class rdf:about="http://example.org/test1/DemoClass">
     [java]         <rdfs:label>DemoClass</rdfs:label>
     [java]     </owl:Class>
     [java]     <owl:DatatypeProperty rdf:about="http://example.org/test1/name">
     [java]         <rdfs:label>name</rdfs:label>
     [java]         <rdfs:domain rdf:resource="http://example.org/test1/DemoClass"/>
     [java]     </owl:DatatypeProperty>
     [java]     <owl:DatatypeProperty rdf:about="http://example.org/test1/description">
     [java]         <rdfs:label>description</rdfs:label>
     [java]         <rdfs:domain rdf:resource="http://example.org/test1/DemoClass"/>
     [java]     </owl:DatatypeProperty>
     [java]     <DemoClass rdf:about="http://example.org/test1/termThree">
     [java]         <rdfs:label>termThree</rdfs:label>
     [java]         <name>termThree</name>
     [java]         <description>description of termThree</description>
     [java]     </DemoClass>
     [java]     <DemoClass rdf:about="http://example.org/test1/termOne">
     [java]         <rdfs:label>termOne</rdfs:label>
     [java]         <name>termOne</name>
     [java]         <description>description of termOne</description>
     [java]     </DemoClass>
     [java]     <DemoClass rdf:about="http://example.org/test1/termTwo">
     [java]         <rdfs:label>termTwo</rdfs:label>
     [java]         <name>termTwo</name>
     [java]         <description>description of termTwo</description>
     [java]     </DemoClass>
     [java] </rdf:RDF>
     [java] 

BUILD SUCCESSFUL
Total time: 0 seconds


Running the tests

$ ant  -Dusername=carueda -Dpassword=xxxxxx test 
Buildfile: build.xml

initexec:

compile:
    [javac] Compiling 1 source file to /Users/carueda/mmiworkspace/mmiorr-client-demo/_generated/classes

test:
    [junit] Running org.mmisw.mmiorr.client.test.MmiOrrTest
    [junit] Testsuite: org.mmisw.mmiorr.client.test.MmiOrrTest
    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 1.876 sec
    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 1.876 sec
    [junit] ------------- Standard Output ---------------
    [junit] Executing POST request to http://mmisw.org/orr/direg
    [junit] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1&form=owl
    [junit] ------------- ---------------- ---------------
    [junit] 
    [junit] Testcase: test42 took 1.872 sec

BUILD SUCCESSFUL
Total time: 2 seconds


