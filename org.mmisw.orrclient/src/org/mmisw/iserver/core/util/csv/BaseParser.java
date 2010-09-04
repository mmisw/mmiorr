package org.mmisw.iserver.core.util.csv;

import java.io.File;
import java.io.IOException;

/**
 * Base class for CSV parsers. It's mainly a proxy to the actual implementation.
 * Currently there is an ad hoc implementation, but this interface will more easily
 * allow to incorporate a more robust library in the future.
 * 
 * @author Carlos Rueda
 */
public abstract class BaseParser {
	
	/**
	 * Factory of BaseParser objects.
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static BaseParser createParser(File file) throws IOException {
		if ( true ) {
			return new MyParser(file);
		}
		else {
			// opencsv does not work well.
			return new OpenParser(file);
		}
	}

	protected char separator = ',';
	protected int lineno = 0;
	
	private String[] record = null;
	private boolean first = true;
	private boolean hasNext = true;

	/**
	 * Sets the separator. 
	 * Call this before {@link #hasNext()} or {@link #getNext()}.
	 * @param separator
	 */
	public void setSeparator(char separator) {
		this.separator = separator;
	}
	
	/**
	 * Tells whether there is a next record to be retrieved with {@link #getNext()}.
	 */
	public final boolean hasNext() throws IOException {
		if ( first ) {
			_init();
			first = false;
			record = _reallyGetNext();
			hasNext = record != null;				
		}
		return hasNext ;
	}
	
	/**
	 * Gets the next record.
	 */
	public String[] getNext() throws IOException {
		if ( first ) {
			hasNext();
			return record;
		}
		String[] ret = record;
		record = _reallyGetNext();
		hasNext = record != null;
		return ret;
	}
	
	/** 
	 * Called when the client has actually started the scanning of the input
	 * (ie., by calling {@link #hasNext()} or {@link #getNext()}.
	 * So, a subclass can actually prepare necessary resources in this call
	 * and not in the constructor. 
	 */
	protected abstract void _init() throws IOException;
	
	/** 
	 * Gets the next record with actual contents 
	 */
	protected abstract String[] _reallyGetNext() throws IOException;
	
	/**
	 * Closes whatever resources needed.
	 */
	public abstract void close() throws IOException;

	/**
	 * Convenience method to report a parse error. The message will include
	 * the number of the line just scanned.
	 */
	public IOException error(String string)  {
		return new IOException("Line: " +lineno+ ": " +string);
	}

}
