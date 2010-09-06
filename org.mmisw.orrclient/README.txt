org.mmisw.orrclient - ORR Client Library
Carlos Rueda  -  http://marinemetadata.org
May 2009

This module provides a library for clients to interact with an ORR deployment.
By ORR deployment we mean:
 - A BioPortal back-end service, and
 - A corresponding Ont service

The "exported" (ie., public) types for clients of the library are:
	- org.mmisw.orrclient.OrrClientConfiguration -- allows to configure the library
	- org.mmisw.orrclient.IOrrClient.Manager -- initializes the library
	- org.mmisw.orrclient.IOrrClient -- main interface to the library
	- and all types in packages org.mmisw.orrclient.gwt.**  -- types handled by the library
	
Typical usage starts with:
	OrrClientConfiguration config = new OrrClientConfiguration();
	occ.setOntServiceUrl(ontServiceUrl);
	occ.setPreviewDirectory(previewDirectory)
	// ... other config parameters
	IOrrClient orrClient = IOrrClient.Manager.init(occ);
	// use the library	
	

NOTES:
 
1- This library's primary goal is to support the ORR Portal application, and in fact
   both modules have been developed pretty much in parallel. 
   The use of the library by other client applications is certainly possible but 
   the API is still evolving so changes may occur without any notice and without any 
   guarantee of backward compatibility.

2- Though this library does not have any dependencies on GWT, it can act as a GWT module.
   Add the orrclient library to your classpath and include in your GWT module:
       <inherits name='org.mmisw.orrclient.gwt.OrrClient15x'/>
   This is suitable for GWT 1.5.x. A future version of orrclient will a more recent GWT version.
 
  
See also ChangeLog.txt
