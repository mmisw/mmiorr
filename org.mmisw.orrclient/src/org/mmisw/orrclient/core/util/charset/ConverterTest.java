package org.mmisw.orrclient.core.util.charset;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedMap;

import org.apache.commons.io.IOUtils;
import org.mmisw.orrclient.core.util.Utf8Util;
import org.mmisw.orrclient.gwt.client.rpc.ReadFileResult;

/**
 * Charset conversion of a file.
 * <p>
 * Note: this was testing code previously located in Utf8Util, but now kept here
 * mainly for back-up purposes (it's not used anywhere in the ORR modules at this point).
 * Code minimally maintained; could be improved.
 * 
 * @author Carlos Rueda
 */
public class ConverterTest  {

	
	/**
	 * Reads a file.
	 * If the file can be read using charset UTF-8, no conversion is attempted,
	 * but an error is signaled so the main program does not create the output file.
	 * Otherwise, conversion to UTF-8 is attempted.
	 * 
	 * @param file The file to read
	 * @return the result of the operation.
	 * @throws Exception
	 */
	private static ReadFileResult readFileWithConversionToUtf8(File file) throws Exception {
		ReadFileResult result = new ReadFileResult();
		
		try {
			String str = IOUtils.toString(new FileInputStream(file), "UTF-8");
			result.setLogInfo("OK: file can be read as UTF-8 (no conversion necessary)");
			result.setError("setting error to avoid unnecesary conversion");
			result.setContents(str);
			return result;

		}
		catch(Throwable ex) {
			result.setLogInfo("OK: file cannot be read as UTF-8 directly.");
		}
		
		byte[] bytes = IOUtils.toByteArray(new FileInputStream(file));
		
		Collection<String> charsets = Utf8Util.isUtf8(bytes);
		
		if ( charsets == null ) {
			// charsets == null means the bytes are good UTF-8, so this
			// should NOT happen.
			result.addLogInfo("OK: already in UTF-8.");
			result.setError("setting error to avoid unnecesary conversion");
			result.setContents(new String(bytes, "UTF-8"));
			return result;
		}
		
		result.addLogInfo("Charset of the file may be one of: " +charsets+ "\n");
		result.addLogInfo("Attempting conversiones..\n");
		
		for ( String charsetName : charsets ) {
			try {
				String outputStr = _asString(bytes, charsetName);
				result.addLogInfo("Conversion from " +charsetName+ ": OK.\n");
				result.setContents(outputStr);
				return result;
			}
			catch(CharacterCodingException ex) {
				// continue with the other possible charsets...
			}
		}
		
		result.setError("None of the conversions from the possible detected charsets " 
				+ " was successful: " +charsets
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
		
		String filename;
		if ( args.length > 0 ) {
			filename = args[0];
			System.out.println("File: " +filename);
		}
		else {
			filename = "resource/utf8/theme-windows-1250.owl";
			System.out.println("No arg given. Using a hard-coded filename: " +filename);
		}

		File file = new File(filename);
		
		File outFile = new File(file.getParent(), "utf8-" +file.getName());
		String outFilename = outFile.getPath();
		
		ReadFileResult result = readFileWithConversionToUtf8(file);
		System.out.println("readFileWithConversionToUtf8:");
		String error = result.getError();
		if ( error != null ) {
			System.out.println("error: " +result.getError());
		}
		System.out.println("logInfo:\n\t" +result.getLogInfo().replaceAll("\n", "\n\t"));
		
		if ( error == null ) {
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

	private static String _asString(byte[] bytes,  String charsetName) throws CharacterCodingException, UnsupportedEncodingException {
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
	
	

	private static void availableCharsets() {
		System.out.println("availableCharsets");

		SortedMap<String, Charset> map = Charset.availableCharsets();
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			String charsetName = (String)it.next();
			Charset charset = Charset.forName(charsetName);
			System.out.println(charset);
		}
	}

}

