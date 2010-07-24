watchdog
Luis Bermudez - MMI/SURA
Carlos Rueda - MMI/MBARI

== Getting the code, building, and running ==

	svn checkout http://mmisw.googlecode.com/svn/trunk/org.mmisw.watchdog
	cd org.mmisw.watchdog
	mvn clean install
	mvn exec:java
	more src/main/resources/output/cf.owl

== CF conversion ==

The ``mvn exec:java'' command above reads in src/main/resources/input/cf-standard-name-table.xml
and generates src/main/resources/output/cf.owl

A generic interface ICf2Skos is used to allow different implementations of the conversion.
Currently, there are two implementations: Cf2SkosJena (with basically the implementation by Luis)
and Cf2SkosSkosApi (a new implementation based on the SKOS API).


== TODO ==

- Only CF conversion is included. Other conversions to be included as we work on them. 

- The Cf program accepts arguments but these are not yet passed from the pom.xml 

- Automate the overall process (download inputs -> convert -> register at ORR)
  A URI is already accepted so one could indicate the actual source, which is:
    http://cf-pcmdi.llnl.gov/documents/cf-standard-names/standard-name-table/current/cf-standard-name-table.xml
  and compare the last_modified attribute with the previous processed file to proceeding with
  the conversion/registration if needed.
  
  
  

