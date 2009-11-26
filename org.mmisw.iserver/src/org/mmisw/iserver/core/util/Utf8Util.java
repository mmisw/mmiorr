package org.mmisw.iserver.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
			String error = "error";
			if ( detectedCharset[0] != null ) {
				error = "Detected charset: " +detectedCharset[0];
			}
			else if ( probableCharsets.size() > 0 ) {
				error = "Probable charsets: " +probableCharsets;
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
	 * If the file can be read using charset UTF-8, no conversion is attempted.
	 * Otherwise, conversion to UTF-8 is attempted.
	 * 
	 * @param file The file to read
	 * @return the result of the operation.
	 * @throws Exception
	 */
	public static ReadFileResult readFileWithConversionToUtf8(File file) throws Exception {
		ReadFileResult result = new ReadFileResult();
		
		try {
			String str = IOUtils.toString(new FileInputStream(file), "UTF-8");
			result.setLogInfo("OK: file can be read as UTF-8 (no conversion necessary)");
			result.setContents(str);
			return result;

		}
		catch(Throwable ex) {
			result.setLogInfo("OK: file cannot be read as UTF-8 directly.");
		}
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		
		
		String[] detectedCharset = { null };
		Set<String> charsetNames = new LinkedHashSet<String>();
		
		if ( isUtf8(bytes, detectedCharset, charsetNames) ) {
			// but the true return should not happen becuase we already attempted reading UTF-8 above.
			result.addLogInfo("OK: already in UTF-8.");
			result.setContents(new String(bytes, "UTF-8"));
			return result;
		}
		
		if ( detectedCharset[0] != null ) {
			charsetNames.add(detectedCharset[0]);
		}
		
		result.addLogInfo("Charset of the file may be one of: " +charsetNames+ "\n");
		result.addLogInfo("Attempting conversiones..\n");
		
		for ( String charsetName : charsetNames ) {
			try {
				String outputStr = convertToUtf8(bytes, charsetName);
				
				// conversion apparently ok, but check that the output is UTF-8
				if ( ! isUtf8(outputStr.getBytes(), null, null) ) {
					result.addLogInfo("Conversion from " +charsetName+ ": ERROR\n");
					continue;
				}

				result.addLogInfo("Conversion from " +charsetName+ ": OK.\n");
				result.setContents(outputStr);
				return result;
			}
			catch(CharacterCodingException ex) {
				// continue with the other possible charsets...
			}
		}
		
		result.setError("None of the conversions from the possible detected charsets " 
				+ " was successful: " +charsetNames
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
		String outFilename = "resource/theme-output.owl";
		File file = new File(filename);
		
		ReadFileResult result = readFileWithConversionToUtf8(file);
		System.out.println("readFileWithConversionToUtf8:");
		if ( result.getError() != null ) System.out.println("error: " +result.getError());
		System.out.println("logInfo:\n\t" +result.getLogInfo().replaceAll("\n", "\n\t"));
		
		if ( result.getError() == null ) {
			_writeStringTo(result.getContents(), outFilename);
			System.out.println("Output written to " +outFilename);		
		}
	}
	
	
	private static void _writeStringTo(String outputStr, String outFilename) throws IOException {
		File outputFile = new File(outFilename);
		
		OutputStreamWriter os = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
		try {			
			IOUtils.write(outputStr, os);
		}
		finally {
			IOUtils.closeQuietly(os);
		}
	}

	private static String convertToUtf8(byte[] bytes,  String charsetName) throws CharacterCodingException, UnsupportedEncodingException {
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
		
//		return str;

		
		Charset encoder = Charset.forName("UTF-8");

		ByteBuffer bbuf2 = encoder.newEncoder()
			.onMalformedInput(CodingErrorAction.REPORT)
			.onUnmappableCharacter(CodingErrorAction.REPORT)
			.encode(cbuf);
		
		str = new String(bbuf2.array(), "UTF-8");

		return str;
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

