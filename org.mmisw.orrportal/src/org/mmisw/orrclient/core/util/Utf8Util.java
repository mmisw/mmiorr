package org.mmisw.orrclient.core.util;

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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mmisw.orrclient.core.util.charset.CharsetDetectorIcu;
import org.mmisw.orrclient.core.util.charset.CharsetDetectorJcd;


/**
 * Utility for detection/verification of UTF-8 charset.
 * 
 * @author Carlos Rueda
 */
public class Utf8Util {
	
	private static final Log log = LogFactory.getLog(Utf8Util.class);
	
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
	 * This test is mainly based on java.nio.charset.CharsetDecoder to convert
	 * a byte array to string given a certain charset (UTF-8 used here). 
	 * If this fails, an exception is immediately thrown. 
	 * If not, this method does some further check which may be unnecesary.
	 * TODO Under testing.
	 * 
	 * @param bytes            
	 *           the contents to check
	 * @return  
	 *           null to indicate success, ie., the given buffer is in UTF-8 or ASCII.
	 *           
	 * @throws Exception  
	 *           if conversion to a string assuming UTF-8 generates an exception.
	 */
	public static Collection<String> isUtf8(byte[] bytes) throws Exception {
		
		// check it can be decoded assuming UTF-8:
		String str = _utf8toString(bytes);
		// TODO probably, we should just do the above check, and only do the remaining
		// stuff in case we get an exception.
		
		if ( log.isDebugEnabled() ) {
			int len = Math.min(50, str.length());
			log.debug("isUtf8: basic test OK: " +str.subSequence(0, len));
		}
		
		Collection<String> charsets = detector.detectCharset(bytes);
		
		if ( charsets == null || charsets.size() == 0 ) {
			// just return null, so OK.  The following is to drastic a result given that the
			// conversion above was succesful
			return null; // OK
			// NO: throw new Exception("Cannot determine the charset of the given contents");
		}
		
		if ( charsets.contains("UTF-8")  || charsets.contains("ASCII") ) {
			return null; // OK
		}
		
		// some previous version had this check instead of the containment ones above:
//		String charset = charsets.iterator().next();
//		if ( "UTF-8".equalsIgnoreCase(charset) || "ASCII".equalsIgnoreCase(charset) ) {
//			return null; // OK
//		}

		// we give up - return the charsets.
		if ( log.isDebugEnabled() ) {
			log.debug("isUtf8: WARN: basic conversion ok but detected charsets did not include " +
					"UTF-8  or ASCII) !!");
		}
		return charsets;
	}

	
	
	public static void verifyUtf8(File file) throws Exception {
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		verifyUtf8(bytes);
	}
	
	/**
	 * Converts a byte array to a string assuming UTF-8 encoding.
	 * 
	 * <p>
	 * Uses a java.nio.charset.CharsetDecoder to decode the contents assuming UTF-8.
	 * This is mainly intended to serve as a first test to verify that a
	 * buffer can be read assuming UTF-8.
	 * 
	 * @param bytes
	 * @return
	 * @throws CharacterCodingException
	 * @throws UnsupportedEncodingException
	 */
	private static String _utf8toString(byte[] bytes) throws CharacterCodingException, UnsupportedEncodingException {
		return _byteArrayToString(bytes, "UTF-8");
	}

	/**
	 * Converts a byte array to a string using the given encoding.
	 * <p>
	 * Uses a java.nio.charset.CharsetDecoder.
	 */
	private static String _byteArrayToString(byte[] bytes, String charsetName) throws CharacterCodingException, UnsupportedEncodingException {
//		http://www.exampledepot.com/egs/java.nio.charset/ConvertChar.html
		
		// Note that <code>new String(bytes, charsetName)</code> is not useful because: 
		// "The behavior of this constructor when the given bytes are not valid in the 
		// given charset is unspecified."
		
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
//			filename = "resource/utf8/theme.owl";
			filename = "resource/utf8/with-non-utf8.csv";
			System.out.println("No arg given. Using a hard-coded filename: " +filename);
		}
		
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(filename));
		
		Set<String> triedCharsets = new HashSet<String>();
		
		ICharsetDetector[] detectors = new ICharsetDetector[] { new CharsetDetectorIcu(), new CharsetDetectorJcd(), };
		for ( ICharsetDetector detector : detectors ) {
			Collection<String> charsets = detector.detectCharset(bytes);
			System.out.println(detector.getClass().getSimpleName()+ ": Detected charsets: " +charsets);
			
			if ( true ) {
				for ( String charset : charsets ) {
					if ( ! triedCharsets.contains(charset) ) {
						triedCharsets.add(charset);
						System.out.println("  checking that it can be decoded as " +charset);
						try {
							String result = _byteArrayToString(bytes, charset);
							if ( true )
								System.out.println("    Result: [ " +result+ "]");
						}
						catch(Throwable thr) {
							thr.printStackTrace();
						}
					}
				}
			}
		}
		if ( ! triedCharsets.contains("UTF-8") ) {
			String charset = "UTF-8";
			System.out.println("  checking that it can be decoded as " +charset);
			try {
				_byteArrayToString(bytes, charset);
			}
			catch(Throwable thr) {
				thr.printStackTrace();
			}
		}

	}
}

