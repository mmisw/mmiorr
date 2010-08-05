package org.mmisw.iserver.core.util.csv;

import java.io.File;
import java.io.IOException;

/**
   Implementation based on opencsv, but opencsv (tested 2.3) does not work well.
   For an input line like this (and using defauls parameters --comma separator, " quote char): 
   
   <p><code>
           ontologyURI = "http://example.org/myont"
	</code>
	<p>
	opencsv gets the String array:
	<p><code> 
		[ontologyURI]  ["http://example.org/myont]
	</code><p>
	ie., it messes up with the quotes! :(
	
	<p>
	So, after various tests, I'm finally not including the library (opencsv-2.3.jar), 
	and all the methods here throw UnsupportedOperationException (code commented in case
	we look into this again). 
	
	<p>
	I also did a quick test of SuperCSV-1.52, but didn't like it either.

 * @author Carlos Rueda
 */
public class OpenParser extends BaseParser {
//	CSVReader csvReader;
//	private File file;
	
	OpenParser(File file) throws IOException {
		throw new UnsupportedOperationException();
//		this.file = file;
	}
	
	protected void _init() throws IOException {
		throw new UnsupportedOperationException();
//		csvReader = new CSVReader(new FileReader(file), separator); 	
	}
	
	protected String[] _reallyGetNext() throws IOException {
		throw new UnsupportedOperationException();
//		String[] record = null;
//		while ( (record = csvReader.readNext()) != null ) {
//			lineno++;
//			if ( record.length == 0 || record[0].trim().length() == 0 || record[0].trim().startsWith("#") ) {
//				// skip empty line or line starting with #
//				continue;
//			}
//			break;
//		}
//		return record;
	}
	
	public void close() throws IOException {
		throw new UnsupportedOperationException();
//		csvReader.close();
	}

}
