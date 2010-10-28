= BioPortal back-end =

The Ont service requires a running instance of the BioPortal back-end in the same machine. 

BioPortal requires a MySQL server. Please install MySQL if necessary.

The Ont service currently relies on version tagged "1005" of the BioPortal back-end:

  cd SOME_DIRECTORY
  mkdir bioportal
  cd bioportal
  svn checkout https://bmir-gforge.stanford.edu/svn/bioportal_core/tags/1005
  cd 1005/

The following per BioPortal instructions (docs/deployment/BioPortal Deployment.doc):

$ mysql -u root -p 

  mysql> create database bioportal;
  Query OK, 1 row affected (0.02 sec)
  
  mysql> create database bioportal_protege;
  Query OK, 1 row affected (0.00 sec)
  
  mysql> create database bioportal_lexgrid;
  Query OK, 1 row affected (0.00 sec)
  
  mysql> create user bioportal_user; 
  Query OK, 0 rows affected (0.13 sec)
  
  mysql> create user bp_lexgrid_user;
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> create user bp_protege_user;
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> grant all on *.* to 'bioportal_user'@'localhost';
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> grant all on *.* to 'bp_lexgrid_user'@'localhost';
  Query OK, 0 rows affected (0.00 sec)
  
  mysql> grant all on *.* to 'bp_protege_user'@'localhost';
  Query OK, 0 rows affected (0.00 sec)
  
Then, to initialize the database:
$ mysql -u root -p < db/sql/bioportal_db.sql
$ mysql -u root -p < db/sql/bioportal_lookup_data.sql


Before building BioPortal, the following source file needs an adjustment: 
   WebRoot/WEB-INF/resources/xslt/ontology_light.xsl
The change is to add the line ``<xsl:copy-of select="urn" />'' between the
corresponding lines for "id" and "displayLabel":

  <xsl:copy-of select="id" />
  <xsl:copy-of select="urn" />               <<< THIS IS THE NEW LINE
  <xsl:copy-of select="displayLabel"/>
  
Note, this change is not part of the BioPortal set-up, and in fact the "urn"
column in the database is not used by BioPortal itself (presumably it was in an
older version). But we do exploit this "urn" column to handle ontology
identification via URIs (so, we actually store generic URIs in this column).

Then, continuing with the BioPortal instructions:
  cp build.properties.sample build.properties
Edit build.properties and complete/adjust the following as appropriate, for example:
  bioportal.resource.path=/mmiorr_workspace/bioportal/resources
  appserver.home=/usr/local/tomcat
  appserver.lib=${appserver.home}/lib
  bioportal.jdbc.username=bioportal_user
  bioportal.jdbc.password=THE_ACTUAL_PASSWORD
  protege.jdbc.username=bp_protege_user
  protege.jdbc.password=THE_ACTUAL_PASSWORD
  lexgrid.db.user=bp_lexgrid_user
  lexgrid.db.password=THE_ACTUAL_PASSWORD

NOTE:
  - The path above, /mmiorr_workspace/bioportal/resources, is a suggestion. Adjust
    as necessary. In particular, you may want to designate a base directory, eg.,
    /mmiorr_workspace/, under which to locate the various resources associated with
    the MMI ORR system.
  
Then:
  mkdir -p /mmiorr_workspace/bioportal/resources
  ant clean
  ant deploywar
 
Assuming BioPortal was deployed as http://example.net/bioportal, then open 
http://example.net/bioportal/rest/ontologies in your browser to verify the installation. 
You can also inspect the logs, ${appserver.home}/logs/bioportal.log and others under
${appserver.home}/logs/, for any necessary diagnostics.
