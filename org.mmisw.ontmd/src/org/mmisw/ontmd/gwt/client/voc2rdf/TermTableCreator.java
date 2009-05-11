package org.mmisw.ontmd.gwt.client.voc2rdf;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mmisw.ontmd.gwt.client.Main;

/**
 * 
 * @author Carlos Rueda
 */
public class TermTableCreator {

	
	/**
	 * Creates a TermTable
	 * 
	 * @param errorMsg Any error is reported here.
	 * @return
	 */
	public static TermTable createTermTable(char separator, String ascii, boolean readOnly, StringBuffer errorMsg) {
		assert ascii.length() > 0;

		String[] lines = ascii.split("\n|\r\n|\r");
		if ( lines.length == 0 || lines[0].trim().length() == 0 ) {
			errorMsg.append("Empty vocabulary contents");
			// A 1-column table to allow the user to insert columns (make column menu will be available)
			return new TermTable(1, readOnly);
		}
		
		boolean error = false;
		
		List<String> headerCols = parseLine(lines[0], separator);
		final int numHeaderCols = headerCols.size();

		TermTable termTable = new TermTable(numHeaderCols, readOnly);
		
		// header:
		
		// to check not repeated column headers
		Set<String> usedColHeader = new HashSet<String>();
		
		for ( int c = 0; c < numHeaderCols; c++ ) {
			String str = headerCols.get(c).trim();
			if ( str.length() == 0 ) {
				if ( !error ) {
					error = true;
					errorMsg.append("empty column header: " +(c+1));
				}
			}
			else if ( usedColHeader.contains(str) ) {
				if ( !error ) {
					error = true;
					errorMsg.append("repeated column header: " +str);
				}
			}
			else {
				usedColHeader.add(str);
			}
			termTable.setHeader(c, str);
		}		
		
		if ( lines.length  == 1 ) {
			if ( !error ) {
				error = true;
				errorMsg.append("Only a header line is included");
			}
			return termTable;
		}
		

		// to check not repeated values for first column:
		Set<String> usedFirstColValue = new HashSet<String>();

		
		// row = row in termTable:
		int row = -1;

		// remaining rows:
		for ( int r = 1; r < lines.length; r++ ) {
			
			List<String> cols = parseLine(lines[r], separator);
			final int numCols = cols.size();

			// skip empty line
			boolean empty = true;
			for ( int c = 0; empty && c < numCols; c++ ) {
				String str = cols.get(c).trim();
				if ( str.length() > 0 ) {
					empty = false;
				}
			}
			if ( empty ) {
				continue;
			}
			
			row++;
			termTable.addRow(numCols);
			for ( int c = 0; c < numCols; c++ ) {
				String str = cols.get(c).trim();
				
				if ( c == 0 ) {
					if ( str.length() == 0 ) {
						if ( !error ) {
							error = true;
							errorMsg.append("Empty key in first column, line " +r);
						}
					}
					
					else if ( usedFirstColValue.contains(str) ) {
						if ( !error ) {
							error = true;
							errorMsg.append("repeated key in first column: " +str+ ", line " +r);
						}
					}
					else {
						usedFirstColValue.add(str);
					}
				}
				
				if ( c < numHeaderCols ) {
					termTable.setCell(row, c, str);
				}
				else {
					// more columns than expected
					if ( !error ) {
						error = true;
						errorMsg.append("more columns than expected, line " +r);
					}
					termTable.setCell(row, c, str);
				}
			}
			
			// any missing columns? 
			if ( numCols < numHeaderCols ) {
				
				// Accept empty collumns (except the first columns, which is the key) is OK. See issue #119.
//				if ( !error ) {
//					error = true;
//					errorMsg.append("missing columns according to header, line " +r);
//				}
				// Note that no further check for the required key column (the first one) is neccesary,
				// because the has at least one column and the check was already done above.
				
				for ( int c = numCols; c < numHeaderCols; c++ ) {
					termTable.setCell(row, c, "");
				}
			}
			
		}
		
		Main.log("termTable created");
//		Main.log(termTable.toString().replace('<', '{'));
		
		return termTable;
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
	 */
	public static List<String> parseLine(String line, char separator) {
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
