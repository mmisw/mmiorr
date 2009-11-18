package org.mmisw.iserver.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.mmisw.iserver.gwt.client.rpc.ReadFileResult;
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
		Set<String> probableCharsets = new HashSet<String>();
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
	 *          probable charsets will be added to this set.
	 *          
	 * @return  true if the given buffer is in UTF-8 or ASCII.
	 */
	public static boolean isUtf8(byte[] bytes, String[] detectedCharset, Set<String> probableCharsets) {
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
	 *          probable charsets will be added to this set.
	 * @return
	 */
	public static String detectCharset(byte[] bytes, Set<String> probableCharsets) {
		
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
	
	
	/**
	 * Reads a file.
	 * If the file is already UTF-8, no conversion if attempted.
	 * Otherwise, conversion to UTF-8 is attempted.
	 * 
	 * @param file The file to read
	 * @return the result of the operation.
	 * @throws Exception
	 */
	public static ReadFileResult readFileWithConversionToUtf8(File file) throws Exception {
		ReadFileResult result = new ReadFileResult();
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		
		String[] detectedCharset = { null };
		Set<String> charsetNames = new LinkedHashSet<String>();
		
		if ( isUtf8(bytes, detectedCharset, charsetNames) ) {
			result.setLogInfo("OK: already in UTF-8.");
			result.setContents(new String(bytes, detectedCharset[0]));
			return result;
		}
		
		if ( detectedCharset[0] != null ) {
			charsetNames.add(detectedCharset[0]);
		}
		
		// give preference for "UTF-8" in case the list contains multiple charsets, including UTF-8, of course.
		
		// this list will contain the possible charsets with "UTF-8" first if that's one:
		List<String> charsetNamesWithUtf8FirstIfThere = new ArrayList<String>(); 
		if ( charsetNames.contains("UTF-8") ) {
			charsetNames.remove("UTF-8");
			charsetNamesWithUtf8FirstIfThere.add("UTF-8");
		}
		charsetNamesWithUtf8FirstIfThere.addAll(charsetNames);
			
		result.setLogInfo("Charset of the file may be one of: " +charsetNamesWithUtf8FirstIfThere+ "\n");
		
		for ( String charsetName : charsetNamesWithUtf8FirstIfThere ) {
			try {
				String outputStr = convertToUtf8(bytes, charsetName);
				result.setContents(outputStr);
				result.addLogInfo("Conversion to " +charsetName+ " was OK.\n");
				return result;
			}
			catch(CharacterCodingException ex) {
				// continue with the other possible charsets...
			}
		}
		
		result.addLogInfo("None of the obove conversions was successful");
		result.setError("None of the conversions from the possible charsets " 
				+charsetNamesWithUtf8FirstIfThere+ " was successful"
		);
		result.setContents(null);
		return result;
	}
	

	
	
	
	/**
	 * A test program to convert a file that may not pass the UTF-8 test into one that is UTF-8.
	 * If the input file is already UTF-8, it does nothing.
	 * @param args NOT used.
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if ( false ) availableCharsets();
		
		String filename = "resource/theme.owl";
		String outFilename = "resource/theme-converted.owl";
		File file = new File(filename);
		
		ReadFileResult result = readFileWithConversionToUtf8(file);
		System.out.println("readFileWithConversionToUtf8:");
		System.out.println("error: " +result.getError());
		System.out.println("logInfo:\n\t" +result.getLogInfo().replaceAll("\n", "\n\t"));
		
		if ( result.getError() == null ) {
			_writeStringTo(result.getContents(), outFilename);
			System.out.println("Output written to " +outFilename);		
		}
	}
	
	
	private static void _writeStringTo(String outputStr, String outFilename) throws IOException {
		File outputFile = new File(outFilename);
		
		FileOutputStream os = new FileOutputStream(outputFile);
		try {			
			IOUtils.write(outputStr, os);
		}
		finally {
			IOUtils.closeQuietly(os);
		}
	}

	private static String convertToUtf8(byte[] bytes,  String charsetName) throws CharacterCodingException {
		Charset charset = Charset.forName(charsetName);

		CharsetDecoder decoder = charset.newDecoder();

		ByteBuffer bbuf = ByteBuffer.wrap(bytes);
		CharBuffer cbuf = decoder.decode(bbuf);
		String str = cbuf.toString();
		return str;

//		http://www.exampledepot.com/egs/java.nio.charset/ConvertChar.html
//		CharsetEncoder encoder = charset.newEncoder();
//
//		try {
//			// Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
//			// The new ByteBuffer is ready to be read.
//			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(bytes));
//
//			// Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
//			// The new ByteBuffer is ready to be read.
//			CharBuffer cbuf = decoder.decode(bbuf);
//			String s = cbuf.toString();
//		} 
//		catch (CharacterCodingException e) {
//		}
	}

	@SuppressWarnings("unchecked")
	private static void availableCharsets() {
		System.out.println("availableCharsets");

		Map map = Charset.availableCharsets();
		Iterator it = map.keySet().iterator();
		while (it.hasNext()) {
			String charsetName = (String)it.next();
			Charset charset = Charset.forName(charsetName);
			System.out.println(charset);
		}
	}

}

