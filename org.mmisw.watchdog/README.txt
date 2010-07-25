MMISW watchdog
Luis Bermudez - MMI/SURA
Carlos Rueda - MMI/MBARI

Status: Alpha

The watchdog module provides conversion and registration facilities for certain vocabularies
that are available in non-ontological formats.
Currently, only the CF Standard Names vocabulary is supported [1].

The programs are to be run from the command line and it is assumed that your system 
has Subversion and Maven
 
== Getting the code, building, and running ==

	svn checkout http://mmisw.googlecode.com/svn/trunk/org.mmisw.watchdog
	cd org.mmisw.watchdog
	mvn clean install
	mvn exec:java -Dexec.args="--ws workspace/cf"

== CF conversion ==

The ``mvn exec:java'' command above:
- reads in http://cf-pcmdi.llnl.gov/documents/cf-standard-names/standard-name-table/current/cf-standard-name-table.xml
- extracts the value of version_number 
- prepares to write the contents out to workspace/cf/cf-standard-name-table-{version_number}.xml
- if such a file already exists, then:
  - program exits with a message
- otherwise: 
  - writes the remote file out to workspace/cf/cf-standard-name-table-{version_number}.xml
  - does conversion of this file to OWL
  - writes result out to workspace/cf/cf-standard-name-table-{version_number}.owl
  
Run ``mvn exec:java'' (with no args) to get a help message.

  Note: you can generate a self-contained jar with:
  $ mvn assembly:assembly
  and then ejecute the jar directly, eg.:
  $ java -jar target/watchdog-0.0.1-SNAPSHOT-jar-with-dependencies.jar

=== Implementation ===

A generic interface ICf2Skos is used to allow different implementations of the CF conversion.
Currently, there are two implementations: Cf2SkosJena (based on the Jena framework [2])
and Cf2SkosSkosApi (a new implementation based on the SKOS API [3]).


== TODO ==

- Only CF conversion is included. Other conversions to be included as we work on them. 

- Include registration of resulting ontology to MMI ORR
  
  
== Refs ==
[1] http://cf-pcmdi.llnl.gov
[2] http://jena.sourceforge.net/
[3] http://skosapi.sourceforge.net/


