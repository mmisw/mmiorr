package org.mmisw.iserver.core.util;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Helps extract info from an XML document.
 * 
 * @author Carlos Rueda
 */
public class XmlAccessor {
	
	private XPath xpath;
	private Document doc;
	
	private static XPathFactory xfactory;
	
	/**
	 * Creates a new accessor from a string.
	 * @param stringSource
	 * @throws Exception 
	 */
	public XmlAccessor(String stringSource) throws Exception {
		this(new InputSource(new StringReader(stringSource)));
	}
	
	/**
	 * Creates an accessor from an InputSource.
	 * @param inputSource
	 * @throws Exception
	 */
	public XmlAccessor(InputSource inputSource) throws Exception {
		if ( xfactory == null ) {
			xfactory = XPathFactory.newInstance();
		}
		xpath = xfactory.newXPath();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true); // never forget this!
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.parse(inputSource);
	}

	/** 
	 * Gets a string value using the expression <code>"//" +name+ "/text()"</code>.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public String getString(String name) throws Exception {
		String str = (String) xpath.evaluate("//" +name+ "/text()", doc, XPathConstants.STRING);
		return str;
	}

	/**
	 * Returns true if a tag with the given name exists in the document.
	 * @param name
	 * @return
	 * @throws Exception
	 */
	public boolean containsTag(String name) throws Exception {
		NodeList nl = doc.getElementsByTagName(name);
		return nl != null && nl.getLength() > 0 ;
	}


	public static void main(String[] _) throws Exception {
		
		String response="<?xml version=\"1.0\" encoding=\"UTF-8\"?> " +
				"<success> " +
					"<accessedResource>/bioportal/rest/users</accessedResource> " +
					"<accessDate>2009-09-11 13:09:13.874 PDT</accessDate> " +
					"<sessionId>33333333333333</sessionId> " +
					"<data> " +
						"<user> " +
							"<id>1011</id> " +
							"<username>u1</username> " +
							"<email>e1</email> " +
							"<firstname>f1</firstname> " +
							"<lastname>l1</lastname> " +
							"<phone>p1</phone> " +
							"<roles> " +
								"<string>ROLE_LIBRARIAN</string> " +
							"</roles> " +
						"</user> " +
					"</data> " +
				"</success>"
		;	
		
		XmlAccessor xa = new XmlAccessor(response);
		
		String[] tagnames = new String[] {
				"success",  
				"error",
		} ;
		for ( String name : tagnames) {
			System.out.println("contains tag " +name+ ": " +xa.containsTag(name));
		}
		
		String[] names = new String[] {
				"success/sessionId", 
				"success/data/user/id",
				"success/data/user/username", 
				"success/data/user/firstname", 
				"success/data/user/lastname", 
				"success/data/user/phone", 
				"success/data/user/roles/string"
		} ;
		for ( String name : names) {
			String val = xa.getString(name);
			System.out.println(name + ": " +val);
		}
	}

}
