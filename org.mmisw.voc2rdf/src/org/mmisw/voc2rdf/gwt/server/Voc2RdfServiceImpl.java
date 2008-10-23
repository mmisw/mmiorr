package org.mmisw.voc2rdf.gwt.server;

import java.net.URL;
import java.util.Map;

import org.mmisw.voc2rdf.gwt.client.rpc.BaseInfo;
import org.mmisw.voc2rdf.gwt.client.rpc.ConversionResult;
import org.mmisw.voc2rdf.gwt.client.rpc.UploadResult;
import org.mmisw.voc2rdf.gwt.client.rpc.Voc2RdfService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;



/**
 * Implementation of Voc2RdfService. 
 * 
 * @author Carlos Rueda
 * @version $Id$
 */
public class Voc2RdfServiceImpl extends RemoteServiceServlet implements Voc2RdfService {
	private static final long serialVersionUID = 1L;

	
	private BaseInfo baseInfo = new BaseInfo();
	
	
	public BaseInfo getBaseInfo() {
		return baseInfo;
	}
	
	
	public ConversionResult convert(Map<String, String> values) {
		
		Converter ontConverter = new Converter(values);
		
		ConversionResult result = new ConversionResult();
		
		String res = ontConverter.createOntology();
		if ( res.equals("failure") ) {
			result.setError(res);
		}
		else {
			String finalNamespace = ontConverter.getFinalNamespace();
			System.out.println(this.getClass().getName()+ " convert: finalNamespace = " +finalNamespace);
			result.setFinalNamespace(finalNamespace);
			String rdf = ontConverter.getOntologyStringXml();
			result.setRdf(rdf);
		}

		return result;
	}
	
	
	public UploadResult upload(ConversionResult conversionResult, Map<String,String> values) {
		
		System.out.println(this.getClass().getName()+ ": uploading ...");
		UploadResult uploadResult = new UploadResult();
		String sessionId;
		
		String userName = values.get("userId");
		String userPassword = values.get("userPassword");
		
		try {
			Login login = new Login(userName, userPassword);
			sessionId = login.getSessionId();

			String uri = conversionResult.getFinalNamespace();
			String fileName;
			fileName = new URL(uri).getPath();
			String rdf = conversionResult.getRdf();
			OntologyUploader createOnt = new OntologyUploader(uri, fileName, rdf , userName, sessionId);
			String res = createOnt.create();
			
			if ( res.startsWith("OK") ) {
				uploadResult.setInfo(res);
			}
			else {
				uploadResult.setError(res);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			uploadResult.setError(ex.getClass().getName()+ ": " +ex.getMessage());
		}
		
		System.out.println("uploadResult = " +uploadResult);
		
		return uploadResult;
	}

}
