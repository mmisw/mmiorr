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

== TODO ==

- Only CF conversion is included. Other conversions to be included as we work on them. 

- make the programs more flexible in terms of parameters 
- automate the overall process (download inputs -> convert -> register at ORR)

