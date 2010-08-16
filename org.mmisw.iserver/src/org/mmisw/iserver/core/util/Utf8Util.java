package org.mmisw.iserver.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collection;

import org.apache.commons.io.IOUtils;
import org.mmisw.iserver.core.util.charset.CharsetDetectorIcu;
import org.mmisw.iserver.core.util.charset.CharsetDetectorJcd;


/**
 * Utility for detection/verification of UTF-8 charset.
 * 
 * @author Carlos Rueda
 */
public class Utf8Util {
	
	public interface ICharsetDetector {
		
		/**
		 * Detects the charsets of the given buffer.
		 * 
		 * @param bytes the buffer to review.
		 * @return the probable charsets
		 */
		public Collection<String> detectCharset(byte[] bytes) ;

	}
	
	// 2010-08-16: Setting ICU as the charset detection implementation
	private static ICharsetDetector detector = new CharsetDetectorIcu();
	
	/**
	 * Verifies that the given contents are in UTF-8 (or ASCII), throwing an exception if not.
	 * 
	 * @param bytes the buffer
	 * @throws Exception If it's not certain that the contents are in UTF-8 or ASCII; 
	 *         the error message will contain an explanation of the problem, including, if possible,
	 *         a list of the probable charsets (which may include UTF-8).
	 */
	public static void verifyUtf8(byte[] bytes) throws Exception {
		Collection<String> charsets = isUtf8(bytes);
		if ( charsets != null ) {
			throw new Exception("Probable charsets: " +charsets);
		}
	}
	
	/**
	 * Is the given buffer in UTF-8 or ASCII?
	 * 
	 * @param bytes            
	 *           the contents to check
	 * @return  
	 *           null iff the given buffer is in UTF-8 or ASCII, that is, the reported
	 *           list of charsets contains UTF-8 or ASCII as the first element.
	 *           Otherwise the collection of probable charsets (which may include UTF-8).
	 * @throws Exception  
	 *           if the charset cannot be determined.
	 */
	public static Collection<String> isUtf8(byte[] bytes) throws Exception {
		
		// check it can be decoded assuming UTF-8:
		_utf8toString(bytes);
		// TODO probably, we should just do the above check, and only do the remaining
		// stuff in case we get an exception.
		
		Collection<String> charsets = detector.detectCharset(bytes);
		
		if ( charsets == null || charsets.size() == 0 ) {
			throw new Exception("Cannot determine the charset of the given contents");
		}
		
		String charset = charsets.iterator().next();
		if ( "UTF-8".equalsIgnoreCase(charset) || "ASCII".equalsIgnoreCase(charset) ) {
			return null; // OK
		}
		else {
			return charsets;
		}
	}

	
	
	public static void verifyUtf8(File file) throws Exception {
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		verifyUtf8(bytes);
	}
	
	/**
	 * Uses a java.nio.charset.CharsetDecoder to decode the contents assuming UTF-8.
	 * This is mainly intended to serve as a first test to verify a
	 * buffer can be read assuming UTF-8.
	 * 
	 * @param bytes
	 * @return
	 * @throws CharacterCodingException
	 * @throws UnsupportedEncodingException
	 */
	private static String _utf8toString(byte[] bytes) throws CharacterCodingException, UnsupportedEncodingException {
		return _utf8toString(bytes, "UTF-8");
	}
	
	private static String _utf8toString(byte[] bytes, String charsetName) throws CharacterCodingException, UnsupportedEncodingException {
//		http://www.exampledepot.com/egs/java.nio.charset/ConvertChar.html
		
		Charset charset = Charset.forName(charsetName);

		CharsetDecoder decoder = charset.newDecoder();
		decoder
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT)
		;
		
		ByteBuffer bbuf = ByteBuffer.wrap(bytes);
		CharBuffer cbuf = decoder.decode(bbuf);
		String str = cbuf.toString();
		return str;
	}
	
	
	/**
	 * Detects the charset of a given file.
	 * @param args args[0] filename
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String filename;
		if ( args.length > 0 ) {
			filename = args[0];
			System.out.println("File: " +filename);
		}
		else {
//			filename = "resource/utf8/SEACOOS_Revisions(2).csv";
//			filename = "resource/utf8/utf8-theme-windows-1250.owl";
			filename = "resource/utf8/theme.owl";
			System.out.println("No arg given. Using a hard-coded filename: " +filename);
		}
		
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(filename));
		
		ICharsetDetector[] detectors = new ICharsetDetector[] { new CharsetDetectorIcu(), new CharsetDetectorJcd(), };
		for ( ICharsetDetector detector : detectors ) {
			Collection<String> charsets = detector.detectCharset(bytes);
			System.out.println(detector.getClass().getSimpleName()+ ": Detected charsets: " +charsets);
			
			if ( true ) {
				for ( String charset : charsets ) {
					System.out.println("  checking that it can be decoded as " +charset);
					try {
						_utf8toString(bytes, charset);
					}
					catch(Throwable thr) {
						thr.printStackTrace();
					}
				}
			}
		}
	}
}

