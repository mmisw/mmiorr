org.mmisw.orrportal: The MMI Ontology Registry and Repository, ORR
Carlos Rueda  -  MBARI  -  http://marinemetadata.org
$Id$

This is the code base for the Portal to the MMI Ontology Registry and Repository system.

MMISW Project page: http://code.google.com/p/mmisw/
MMI ORR subproject: http://code.google.com/p/mmisw/source/browse/#svn/trunk/org.mmisw.orrportal

See ChangeLog.txt


Building and deploying

Prerequisites:
	Third-party:
		- Google Web Toolkit (using 1.5.2; adjustments may be required for a later version)
		- Java compiler (using 1.5+)
		- Apache Ant (using 1.7)
	 MMI developed:
	 	- The "ont" service deployed in the target application server context
	 	  (http://code.google.com/p/mmisw/source/browse/#svn/trunk/org.mmisw.ont)
	 	- the "orrclient" library (http://code.google.com/p/mmisw/source/browse/#svn/trunk/org.mmisw.orrclient)
	 	  copy this library in base_war/WEB-INF/lib/
	 	

Then:
	- copy sample.build.properties into build.properties 
	- edit build.properties and adjust any necessary parameters
	- edit version.properties if you want to change the version
	- run 'ant war' to create the WAR file _generated/ontmd.war
	- run 'ant deploy-war' to deploy orr.war in your application server

Enjoy!
