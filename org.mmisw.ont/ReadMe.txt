org.mmisw.ont - The MMI Ont service 
Carlos Rueda - carueda@mbari.org
$Id$

org.mmisw.ont is a Web service that supports programmatic interaction with a
BioPortal repository instance as well as ontology and term URI dereferencing
for users and clients in general.

= Using the service =

The following description assumes http://example.net as the base address of the involved
services. In particular, the Ont service address would be http://example.net/ont.
However, note that the code is written in a way that is independent of the actual server 
and root components in the URI.

A central functionality is that any URI that starts with "http://example.net/ont/"
is resolved by this service, for example: "http://example.net/ont/mmi/someVocab".
Resolution means that the given URI is used to search for the corresponding
ontology in the database. If found, the contents of the ontology is returned to
the client in the appropriate format according to content negotiation or explicit
file extension or "form" parameter if given.


= Requirements =

== BioPortal back-end ==

The Ont service requires a running instance of the BioPortal back-end in the same machine. 
Please see ReadMe-BioPortal.txt for instructions to build and deploy the BioPortal back-end
so it can be used by the Ont service. 

== AllegroGraph triple store server ==

Although the Ont service can operate with various triple store implementations, the most
robust option at the moment is AllegroGraph (http://www.franz.com/agraph). Please have an
AllegroGraph server running somewhere in your network.

Here are some quick instructions:
    * Download the package for your environment from http://www.franz.com/agraph/downloads/
    * Extract the contents into /some/directory/ of your choice
    * cd /some/directory/
    * sudo ./AllegroGraphServer
    The server continues running in the background. The log file is /some/directory/agraph.log.
    You can of course instruct your OS to launch this server at boot time.
It is highly recommended that you install the most recent stable version of AllegroGraph.

= Building and deploying the Ont service =

Using latest code image from trunk in the SVN repository:
  cd SOME_DIRECTORY
  svn checkout http://mmisw.googlecode.com/svn/trunk/org.mmisw.ont
  cd org.mmisw.ont
  cp sample.build.properties build.properties

Edit build.properties and complete/adjust the following as appropriate, for example:
  appserver.home = /usr/local/tomcat
  appserver.host = http://example.net
  aquaportal.resource.directory = /mmiorr_workspace/bioportal/resources
  aquaportal.voc2rdf.dir = /mmiorr_workspace/mmiregistry/preuploads/voc2rdf/
  aquaportal.jdbc.url = jdbc:mysql://localhost:3306/bioportal
  aquaportal.jdbc.password = THE_ACTUAL_PASSWORD
  aquaportal.rest.url = http://example.net/bioportal/rest
  agraph.host = localhost
  agraph.ts.dir = /mmiorr_workspace/mmiregistry/agraph/ts
  portal.service.url = http://example.net/orr
  ont.internal.dir = /mmiorr_workspace/mmiregistry/internal
  ga.uanumber = UA-00000000-0
  ga.domainName = example.com
  ga.dir = /mmiorr_workspace/mmiregistry/ga
More details in build.properties.

Some of the mentioned working directories need to exist so:
  mkdir -p /mmiorr_workspace/mmiregistry/agraph/ts
  mkdir -p /mmiorr_workspace/bioportal/resources
  mkdir -p /mmiorr_workspace/mmiregistry/preuploads/voc2rdf
  mkdir -p /mmiorr_workspace/mmiregistry/internal
  mkdir -p /mmiorr_workspace/mmiregistry/ga
  
We're ready to build Ont, but the following is needed so the MySQL JDBC driver
is found at runtime: the driver library should be located under Tomcat's lib/
directory. If not already there, make a copy (for example, using the library 
coming with BioPortal, mysql-connector-java-5.1.6-bin.jar) and restart Tomcat.

Now:
  ant war
  ant deploy-war     # you may need to run this as 'root' (eg., with sudo)

Inspect the ont.log and catalina.out logs (under /usr/local/tomcat/logs/) to verify
the installation.


= Change log =

See ChangeLog.txt

