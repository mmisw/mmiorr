MMISW watchdog
Carlos Rueda - Luis Bermudez 

Status: Alpha

The watchdog module is a collections of programs for the conversion and registration of
key vocabularies/ontologies in the community.

Currently, only the CF Standard Names vocabulary is implemented [1].
We are planning to support other vocabs/ontologies, including UDUnits [2] and SWEET [3].

Watchdog runs on the command line. For getting the code and building the software 
Subversion and Maven are needed in your system.
 
== Getting the code, building, and running one of the programs ==

	svn checkout http://mmisw.googlecode.com/svn/trunk/org.mmisw.watchdog
	cd org.mmisw.watchdog
	mvn clean install
	mvn exec:java -Dexec.args="Cf --ws workspace/cf"

The last command above runs the "Cf" program (as indicated by the first argument). 
Run ``mvn exec:java'' (with no args) to get the list of available "watchdog" programs.
Usage details for each program are displayed when called with no arguments or with the "--help"
argument, eg.:
    mvn exec:java -Dexec.args="Cf --help"
    
    
== CF conversion ==

The Cf program:

- Runs the "Cf" program (as indicated by the first argument), which:
- reads in http://cf-pcmdi.llnl.gov/documents/cf-standard-names/standard-name-table/current/cf-standard-name-table.xml
- extracts the value of version_number 
- prepares to write the contents out to workspace/cf/cf-standard-name-table-{version_number}.xml
- if such a file already exists, then:
  - program exits with a message
- otherwise: 
  - writes the remote file out to workspace/cf/cf-standard-name-table-{version_number}.xml
  - does conversion of this file to OWL
  - writes ontology result out to workspace/cf/cf-standard-name-table-{version_number}.owl
  

  Tip: A self-contained jar can be generated with:
    $ mvn assembly:assembly
  which can be run as follows:
    $ java -jar target/watchdog-0.0.1-SNAPSHOT-jar-with-dependencies.jar

== TODO ==

- Only CF conversion is included. Other vocabularies/ontologies to be included as we work on them.
- Generate skos:hiddenLabel properties for CF aliases 

- Code for registration at ORR is already included but not functional yet.
  

  
== Refs ==

[1] http://cf-pcmdi.llnl.gov
[2] http://www.unidata.ucar.edu/software/udunits/udunits-2-units.html
[3] http://sweet.jpl.nasa.gov/ontology/


