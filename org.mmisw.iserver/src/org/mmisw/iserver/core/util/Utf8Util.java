package org.mmisw.iserver.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;

/**
 * Utility for detection/verification of UTF-8 charset.
 * <p>
 * Implementation based on <a href="http://jchardet.sf.net/">jchardet</a>
 * 
 * @author Carlos Rueda
 */
public class Utf8Util {

	/**
	 * Verifies that the given file is in UTF-8.
	 * @param file
	 * @throws Exception If it's not certain that the contents are in UTF-8; 
	 *         the error message will contain, if possible,
	 *         the detected charset or a list of probable charsets.
	 */
	public static void verifyUtf8(File file) throws Exception {
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		verifyUtf8(bytes);
	}
	
	/**
	 * Verifies that the contents from the input stream is in UTF-8.
	 * <p>
	 * Note: the given stream is fully read; you may need to re-open, use
	 * a mark, or something appropriate if you want to re-read the stream
	 * after this method is called.
	 * @param is
	 * @throws Exception If it's not certain that the contents are in UTF-8; 
	 *         the error message will contain, if possible,
	 *         the detected charset or a list of probable charsets.
	 */
	public static void verifyUtf8(InputStream is) throws Exception {
		byte[] bytes = IOUtils.toByteArray(is);
		verifyUtf8(bytes);
	}
	
	
	/**
	 * Verifies that the contents in the given buffer is in UTF-8.
	 * @param bytes the buffer
	 * @throws Exception If it's not certain that the contents are in UTF-8; 
	 *         the error message will contain, if possible,
	 *         the detected charset or a list of probable charsets.
	 */
	public static void verifyUtf8(byte[] bytes) throws Exception {
		String[] detectedCharset = { null };
		List<String> probableCharsets = new ArrayList<String>();
		if ( ! isUtf8(bytes, detectedCharset, probableCharsets) ) {
			String error = "UTF-8 charset is not certain.";
			if ( detectedCharset[0] != null ) {
				error += " (detected: " +detectedCharset[0]+ ")";
			}
			else if ( probableCharsets.size() > 0 ) {
				error += " (probable charsets: " +probableCharsets+ ")";
			}
			throw new Exception(error);
		}
	}

	/**
	 * Is the given buffer in UTF-8 or ASCII?
	 * 
	 * @param bytes            the contents to check
	 * @param detectedCharset  if non-null and has enough space, the detected charset is stored at [0].
	 * @param probableCharsets if non-null and the charset is not UTF-8 nor ASCII and cannot be determined, then the
	 *          probable charsets will be added to this list.
	 *          
	 * @return  true if the given buffer is in UTF-8 or ASCII.
	 */
	public static boolean isUtf8(byte[] bytes, String[] detectedCharset, List<String> probableCharsets) {
		String charset = detectCharset(bytes, probableCharsets);
		if ( detectedCharset != null && detectedCharset.length > 0 ) {
			detectedCharset[0] = charset;
		}
		return "UTF-8".equals(charset) || "ASCII".equals(charset);
	}
	
	/**
	 * Detects the charset of the given buffer.
	 * Uses <a href="http://jchardet.sf.net/">jchardet</a>
	 * 
	 * @param bytes the buffer to review.
	 * @param probableCharsets if non-null and the charset cannot be determined, then the
	 *          probable charsets will be added to this list.
	 * @return
	 */
	public static String detectCharset(byte[] bytes, List<String> probableCharsets) {
		
		nsDetector det = new nsDetector(nsDetector.ALL) ;

		final String[] detectedCharset = { null };
		det.Init(new nsICharsetDetectionObserver() {
            public void Notify(String charset) {
            	detectedCharset[0] = charset ;
            }
		});

		boolean isAscii = det.isAscii(bytes, bytes.length);
		if ( !isAscii && detectedCharset[0] == null ) {
			det.DoIt(bytes, bytes.length, false);
		}
		det.DataEnd();

		if ( isAscii ) {
			return detectedCharset[0] = "ASCII";
		}
		else if ( detectedCharset[0] == null && probableCharsets != null ) {
			String[] pcs = det.getProbableCharsets();
			if ( pcs != null ) {
				probableCharsets.addAll(Arrays.asList(pcs));
			}
		}
		return detectedCharset[0];
	}

}

