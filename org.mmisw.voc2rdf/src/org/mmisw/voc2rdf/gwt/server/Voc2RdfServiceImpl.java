package org.mmisw.voc2rdf.gwt.server;

import java.net.URL;
import java.util.Map;

import org.apache.log4j.Logger;
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

	
	private static Logger log = Logger.getLogger(Voc2RdfServiceImpl.class);
	
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
			String finalUri = ontConverter.getFinalUri();
			result.setFinalUri(finalUri);
			String rdf = ontConverter.getOntologyStringXml();
			result.setRdf(rdf);
		}

		return result;
	}
	
	
	public UploadResult upload(ConversionResult conversionResult, Map<String,String> values) {
		
		log.debug(": uploading ...");
		UploadResult uploadResult = new UploadResult();
		String sessionId;
		
		String userName = values.get("userId");
		String userPassword = values.get("userPassword");
		
		try {
			Login login = new Login(userName, userPassword);
			sessionId = login.getSessionId();

			String uri = conversionResult.getFinalUri();
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
		
		log.debug("uploadResult = " +uploadResult);
		
		return uploadResult;
	}

}
