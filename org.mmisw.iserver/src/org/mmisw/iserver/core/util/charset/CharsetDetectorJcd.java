package org.mmisw.iserver.core.util.charset;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.mmisw.iserver.core.util.Utf8Util.ICharsetDetector;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

/**
 * Charset detection based on <a href="http://jchardet.sf.net/">jchardet</a>
 * 
 * @author Carlos Rueda
 */
public class CharsetDetectorJcd implements ICharsetDetector {

	public Collection<String> detectCharset(byte[] bytes) {
		
		nsDetector det = new nsDetector(nsDetector.ALL) ;

		final Collection<String> charsets = new LinkedHashSet<String>();
		det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
            	charsets.add(charset);
            }
		});

		boolean isAscii = det.isAscii(bytes, bytes.length);
		if ( !isAscii && charsets.size() == 0 ) {
			det.DoIt(bytes, bytes.length, false);
		}
		det.DataEnd();

		if ( isAscii ) {
			charsets.add("ASCII");
		}
		else if ( charsets.size() == 0 ) {
			String[] pcs = det.getProbableCharsets();
			if ( pcs != null ) {
				charsets.addAll(Arrays.asList(pcs));
			}
		}
		return charsets;
	}
}
