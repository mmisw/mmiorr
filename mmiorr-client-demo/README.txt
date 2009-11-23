org.mmisw.mmiorr.client
Carlos Rueda  -  MBARI
November 2009

Programs demonstrating interaction with the MMI Ontology Registry and Repository for registration 
and retrieval of ontologies and terms. Unit tests include comparison of retrieved ontologies and
terms against the ontologies used for registration.
 
	- Third-party libraries
	- Running the tests
	- Registering ontologies
	- Retrieving a registered ontology or term
	

** Third-party libraries (included in lib/) **
	For interacting with MMI ORR:
	- apache commons-httpclient
	- apache commons-io
	- apache commons-logging
	- apache commons-codec
	
	For unit testing:
	- junit
	- ant-junit (if you run the tests with ant)
	- jena, jenatest, iri, icu4j (Jena libs required for some tests)
	- xercesImpl (required by Jena when running tests from within eclipse)
	
	
** Running the tests **

Registration tests
	ant -Dusername=MyUserName -Dpassword=MyPassword registrationTests
Note: If you are going to run the registration tests with the default ontologies (under resource/)
then you will likely get a "no permission" kind of error because the ontology is already registered
under my account. If that's the case, you can just run the retrieval and comparison tests.
If you want to try your own ontologies using your ORR account, you may have to adjust some of the 
affected retrieval and comparison tests accordingly.

The following is the general registration of an ontology:

	$ ant  -Dusername=carueda -Dpassword=xxxxx registrationTests
	Buildfile: build.xml
	
	loginInfo:
	
	compile:
	
	registrationTests:
	    [junit] Running org.mmisw.mmiorr.client.test.RegistrationTest
	    [junit] Testsuite: org.mmisw.mmiorr.client.test.RegistrationTest
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.867 sec
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.867 sec
	    [junit] ------------- Standard Output ---------------
	    [junit] ** testRegistration
	    [junit] Executing POST request to http://mmisw.org/orr/direg
	    [junit] Registration response:
	    [junit]    <success>
	    [junit]      <ontologyUri>http://example.org/test1</ontologyUri>
	    [junit]      <graphId>ooi-ci</graphId>
	    [junit]      <msg>New version of ontology registered</msg>
	    [junit]    </success>
	    [junit]    
	    [junit]    
	    [junit] ------------- ---------------- ---------------
	    [junit] 
	    [junit] Testcase: testRegistration took 0.865 sec
	
	BUILD SUCCESSFUL
	Total time: 1 second

The following is the generation of a new version of an ontology by adding properties to a resource,
int this case adding a new description for the term http://example.org/test1/termThree (see UpdateTest):

	$ ant  -Dusername=carueda -Dpassword=xxxxx updateTests
	Buildfile: build.xml
	
	loginInfo:
	
	compile:
	
	updateTests:
	    [junit] Running org.mmisw.mmiorr.client.test.UpdateTest
	    [junit] Testsuite: org.mmisw.mmiorr.client.test.UpdateTest
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 3.099 sec
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 3.099 sec
	    [junit] ------------- Standard Output ---------------
	    [junit] ** test31
	    [junit] ** retrieveOntology
	    [junit] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1&form=owl
	    [junit] New statement: [http://example.org/test1/termThree, http://example.org/test1/description, "Generated description 1258932618157"]
	    [junit] ** registerOntology
	    [junit] Executing POST request to http://mmisw.org/orr/direg
	    [junit] Registration response:
	    [junit]    <success>
	    [junit]      <ontologyUri>http://example.org/test1</ontologyUri>
	    [junit]      <graphId>ooi-ci</graphId>
	    [junit]      <msg>New version of ontology registered</msg>
	    [junit]    </success>
	    [junit]    
	    [junit]    
	    [junit] ** retrieveOntology
	    [junit] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1&form=owl
	    [junit] ------------- ---------------- ---------------
	    [junit] 
	    [junit] Testcase: test31 took 3.09 sec
	
	BUILD SUCCESSFUL
	Total time: 4 seconds



Retrieval and comparison tests
	$ ant retrievalTests
	Buildfile: build.xml
	
	compile:
	
	retrievalTests:
	    [junit] Running org.mmisw.mmiorr.client.test.RetrievalTest
	    [junit] Testsuite: org.mmisw.mmiorr.client.test.RetrievalTest
	    [junit] Tests run: 4, Failures: 0, Errors: 0, Time elapsed: 1.22 sec
	    [junit] Tests run: 4, Failures: 0, Errors: 0, Time elapsed: 1.22 sec
	    [junit] ------------- Standard Output ---------------
	    [junit] ** testRetrievalOfOntology
	    [junit] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1&form=owl
	    [junit] ** test42
	    [junit] ** testRetrievalOfTerm
	    [junit] HTTP GET: http://mmisw.org/ont/?uri=http%3A%2F%2Fexample.org%2Ftest1%2FtermThree&form=owl
	    [junit] Term retrieval response:
	    [junit]    <?xml version="1.0"?>
	    [junit]    <rdf:RDF
	    [junit]        xmlns:j.0="http://example.org/test1/"
	    [junit]        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	    [junit]        xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
	    [junit]        <j.0:DemoClass rdf:about="http://example.org/test1/termThree">
	    [junit]            <rdfs:label>termThree</rdfs:label>
	    [junit]            <j.0:name>termThree</j.0:name>
	    [junit]            <j.0:description>description of termThree</j.0:description>
	    [junit]        </j.0:DemoClass>
	    [junit]    </rdf:RDF>
	    [junit]    
	    [junit] ** test30
	    [junit] ------------- ---------------- ---------------
	    [junit] 
	    [junit] Testcase: testRetrievalOfOntology took 0.186 sec
	    [junit] Testcase: test42 took 0.933 sec
	    [junit] Testcase: testRetrievalOfTerm took 0.082 sec
	    [junit] Testcase: test30 took 0.011 sec
	
	BUILD SUCCESSFUL
	Total time: 1 second

SPARQL queries
	$ ant queryTests
	Buildfile: build.xml
	
	compile:
	
	queryTests:
	    [junit] Running org.mmisw.mmiorr.client.test.QueryTest
	    [junit] Testsuite: org.mmisw.mmiorr.client.test.QueryTest
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.563 sec
	    [junit] Tests run: 1, Failures: 0, Errors: 0, Time elapsed: 0.563 sec
	    [junit] ------------- Standard Output ---------------
	    [junit] ** test47
	    [junit] HTTP GET: http://mmisw.org/ont/?form=csv&sparql=SELECT+%3Fp+%3Fo+WHERE+%7B+++%3Chttp%3A%2F%2Fmotherlode.ucar.edu%3A8080%2Fthredds%2FfileServer%2Fstation%2Fmetar%2FSurface_METAR_20091106_0000.nc%3E+++%3Fp+%3Fo+%7D
	    [junit] HTTP GET: http://mmisw.org/ont/?form=csv&sparql=PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E+SELECT+%3Fs+WHERE+%7B%3Fs+rdfs%3AisDefinedBy+%3Chttp%3A%2F%2Fxmlns.com%2Ffoaf%2F0.1%2F%3E.+%7D+
	    [junit] HTTP GET: http://mmisw.org/ont/?form=csv&sparql=PREFIX+rdf%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F1999%2F02%2F22-rdf-syntax-ns%23%3E+PREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E+SELECT+%3Fcomment+WHERE+%7B+%3Chttp%3A%2F%2Fwww.w3.org%2F2003%2F01%2Fgeo%2Fwgs84_pos%23%3E+rdfs%3Acomment+%3Fcomment+%7D+
	    [junit] ------------- ---------------- ---------------
	    [junit] 
	    [junit] Testcase: test47 took 0.552 sec
	
	BUILD SUCCESSFUL
	Total time: 1 second


** Registering ontologies **

RegisterOntology is a program that demonstrates the direct registration of an ontology file.  

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


** Retrieving a registered ontology or term **

RetrieveOntology is a program that demonstrates the access to a registered ontology or term in 
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


