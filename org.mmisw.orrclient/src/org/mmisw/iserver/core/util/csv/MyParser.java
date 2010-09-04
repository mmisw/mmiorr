package org.mmisw.iserver.core.util.csv;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * 
 * @author Carlos Rueda
 */
public class MyParser extends BaseParser {
	private FileInputStream is;
	private LineIterator lines;
	private String line; 


	public MyParser(File file) throws IOException {
		this.is = new FileInputStream(file);
		lines = IOUtils.lineIterator(is, "utf8");
		lineno = 0;
	}
	
	protected void _init() throws IOException {
		// no need to prepare anything else.
	}
	
	private String[] _splitColumns(String line) {
		List<String> toks = parseLine(line, separator);
		return toks.toArray(new String[toks.size()]);
	}

	protected String[] _reallyGetNext() throws IOException {
		String[] record = null;
		while ( lines.hasNext() ) {
			line = lines.nextLine();
			lineno++;
			record = _splitColumns(line);
			if ( record.length == 0 || record[0].trim().length() == 0 || record[0].trim().startsWith("#") ) {
				// skip empty line or line starting with #
				record = null;
				continue;
			}
			break;
		}
		return record;
	}
	
	public void close() {
		IOUtils.closeQuietly(is);
	}

	/**
	 * Parses the line using the given separator and respecting quoted strings, 
	 * which are, however, returned without the quotes (the only handled quoted is the
	 * double quote (")).
	 * 
	 * <p>
	 * Note that the removal of quotes step is very simplistic (no nested quoted
	 * substring or escaped quotes handling is performed).
	 * <br/>Examples: 
	 * <table border=1>
	 *   <tr> <th>input</th> <th>output</th> </tr> 
	 *   <tr> <td>string with no quotes</td> <td>string with no quotes</td> </tr>
	 *   <tr> <td>"a quoted string"</td> <td>a quoted string</td> </tr>
	 *   <tr> <td>"hello "world""</td> <td>"hello "world""</td> </tr>
	 *   <tr> <td>"unbalanced string</td> <td>"unbalanced string</td> </tr>
	 * </table>
	 * <p>
	 * (NOTE: this method copied from the ORR client module).
	 */
	private static List<String> parseLine(String line, char separator) {
		List<String> toks = new ArrayList<String>();
		
		// contents of current token under analysis:
		StringBuffer currTok = new StringBuffer();
		
		boolean inQuote = false;
		
		for ( int i = 0; i < line.length(); i++ ) {
			char chr = line.charAt(i);
			
			if ( chr == '"' ) {
				inQuote = !inQuote; 
				currTok.append(chr);
			}
			else if ( chr == separator ) {
				if ( inQuote ) {
					currTok.append(chr);
				}
				else {
					// token completed.
					toks.add(removeMatchingQuotes(currTok.toString()));
					currTok.setLength(0);
				}
			}
			else {
				currTok.append(chr);
			}
		}
		
		// pending token?
		if ( currTok.length() > 0 ) {
			toks.add(removeMatchingQuotes(currTok.toString()));
		}

		return toks;
	}
	
	/**
	 * Removes the surrounding quotes in the string if they are the only ones, ie., the 
	 * string does not have any other internal quotes. Otherwise, the string is returned
	 * without modification. More details: {@link #parseLine(String, char)}.
	 */
	private static String removeMatchingQuotes(String str) {
		String chkStr = str.trim();
		if ( chkStr.startsWith("\"") && chkStr.endsWith("\"") ) {
			chkStr = chkStr.substring(1, chkStr.length() -1);
			if ( chkStr.indexOf('"') < 0 ) {
				return chkStr;
			}
		}
		return str;
	}


}