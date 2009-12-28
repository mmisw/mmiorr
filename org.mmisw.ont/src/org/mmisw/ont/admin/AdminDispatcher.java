package org.mmisw.ont.admin;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.ont.Db;
import org.mmisw.ont.JenaUtil2;
import org.mmisw.ont.OntConfig;
import org.mmisw.ont.OntServlet.Request;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;



/**
 * Dispatcher of admin-related operations.
 * 
 * @author Carlos Rueda
 */
public class AdminDispatcher {
	
	private final Log log = LogFactory.getLog(AdminDispatcher.class);
	
	private Db db;
	
	public AdminDispatcher(Db db) {
		this.db = db;
	}

	
	/**
	 * Responds an RDF with registered users. Every user URI will be *versioned* with the current time.
	 */
	public void getUsersRdf(Request req) throws ServletException, IOException {
		final String MMIORR_NS = "http://mmisw.org/ont/mmi/mmiorr/";
		
		log.debug("getUsersRdf called.");
		
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'hhmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String version = sdf.format(date);

		final String users_ns = OntConfig.Prop.ONT_SERVICE_URL.getValue()+ "/mmiorr-internal/" +version+ "/users/";
		
		final Model model = ModelFactory.createDefaultModel();
		final Resource userClass = model.createResource( MMIORR_NS + "User" );
		model.setNsPrefix("mmiorr", MMIORR_NS);
		model.setNsPrefix("", users_ns);
		
		final String[][] fieldPropNames = {
				{ "username",  "hasUserName" },
				{ "firstname", "hasFirstName" },
				{ "lastname",  "hasLastName" },
				{ "email",     "hasEmail" },
				{ "date_created", "hasDateCreated" },
		};
		
		List<Map<String, String>> list = db.getAllUserInfos();
		for (Map<String, String> user : list) {
			
			String username = user.get("username");
			if ( username == null || username.length() == 0 ) {
				continue;
			}
			
			Resource userInstance = model.createResource( users_ns + username );
			
			// type:
			model.add(userInstance, RDF.type, userClass);
			
			for (String[] fieldPropName : fieldPropNames ) {
				
				String propValue = user.get(fieldPropName[0]);
				
				if ( propValue == null || propValue.length() == 0 ) {
					continue;
				}
				
				Property propUri = model.createProperty( MMIORR_NS , fieldPropName[1] );
				
				if ( "hasDateCreated".equals(fieldPropName[1]) ) {
					model.add(userInstance, propUri, propValue,  XSDDatatype.XSDdateTime);
				}
				else {
					model.addLiteral(userInstance, propUri, propValue);
				}
			}
		}
		
		String result = JenaUtil2.getOntModelAsString(model, "RDF/XML-ABBREV");
		
		req.response.setContentType("application/rdf+xml");
		ServletOutputStream os = req.response.getOutputStream();
		IOUtils.write(result, os);
		os.close();
	}

	
	
}
