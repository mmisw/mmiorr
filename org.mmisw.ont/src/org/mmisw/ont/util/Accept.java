package org.mmisw.ont.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

/**
 * A helper class to query the "accept" header values.
 * 
 * @author Carlos Rueda
 */
@Unfinished
public class Accept {
	
	// contentType entry
	public static class Entry {
		String contentType;
		Map<String,Float> params = new LinkedHashMap<String,Float>();
		
		public String toString() {
			StringBuilder sb = new StringBuilder(contentType);
			
			for ( String parName: params.keySet() ) {
				sb.append("; " +parName+ " = " +params.get(parName));
			}
			
			return sb.toString();
		}
	}
	
	private List<Entry> entries = new ArrayList<Entry>();

	private Entry dominating;

	/**
	 * Parses the list of values according to:
	 * <a href="http://www.w3.org/Protocols/HTTP/HTRQ_Headers.html#z3"
	 * >http://www.w3.org/Protocols/HTTP/HTRQ_Headers.html#z3</a>
	 * 
	 * @param acceptList Accept list from the request.
	 */
	public Accept(HttpServletRequest request) {
		List<String> acceptList = Util.getHeader(request, "accept");
		
		for ( String field: acceptList ) {
			String[] tokEntries = field.split("\\s*,\\s*");
			for (String entry : tokEntries ) {
				String[] parts = entry.split("\\s*;\\s*");
				
				Entry ct = new Entry();
				ct.contentType = parts[0];
				entries.add(ct);
				
				for ( int i = 1; i < parts.length; i ++ ) {
					String paramPart = parts[i];
					String[] nameVal = paramPart.split("\\s*=\\s*");
					
					try {
						ct.params.put(nameVal[0], Float.valueOf(nameVal[1]));
					}
					catch ( NumberFormatException ignore) {
					}
				}
			}
		}
		
		// determine dominating entry:
		// TODO Entry paramenters not yet considered.
		for ( Entry ctEntry : entries ) {
			if ( "application/rdf+xml".equalsIgnoreCase(ctEntry.contentType) ) {
				dominating = ctEntry;
				break;
			}
			else if ( "text/html".equalsIgnoreCase(ctEntry.contentType) ) {
				dominating = ctEntry;
				break;
			}
		}
		
		if ( dominating == null && entries.size() > 0 ) {
			// FIXME arbitrarely choosing first entry.
			dominating = entries.get(0);
		}
		
		entries = Collections.unmodifiableList(entries);
	}
	
	public List<Entry> getEntries() {
		return entries;
	}

	/** @returns the dominating accept content type. May be null
	 *           if the client didn't send any "accept" values.
	 */
	public String getDominating() {
		return dominating == null ? null : dominating.contentType;
	}
	
	public String toString() {
		return String.valueOf(entries)+ "  Dominating: " +dominating;
	}

	public boolean contains(String contentType) {
		for ( Entry ctEntry : entries ) {
			if ( contentType.equalsIgnoreCase(ctEntry.contentType) ) {
				return true;
			}
		}
		return false;
	}

	public boolean isEmpty() {
		return entries.isEmpty();
	}
}
