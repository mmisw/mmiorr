package org.mmisw.ont;

import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * @author Carlos Rueda
 */
public class MmiUriTest extends TestCase {

	String fullRequestedUri = "http://mmisw.org/ont/mmi/someVocab.owl/someTerm";
	String requestedUri = "/ont/mmi/someVocab.owl/someTerm";
	String contextPath = "/ont";
	
    public void testBasic() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
    
		assertEquals("http://mmisw.org/ont/mmi/someVocab.owl", mmiUri.getOntologyUri());
        assertEquals("mmi", mmiUri.getAuthority());
        assertEquals("someVocab.owl", mmiUri.getTopic());
        assertEquals("someTerm", mmiUri.getTerm());
    }

    public void testTermUris() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri(fullRequestedUri, requestedUri, contextPath);
    
        assertEquals("http://mmisw.org/ont/mmi/someVocab#someTerm", mmiUri.getTermUri(true, "#"));
        assertEquals("http://mmisw.org/ont/mmi/someVocab/someTerm", mmiUri.getTermUri(true, "/"));
        
        assertEquals("http://mmisw.org/ont/mmi/someVocab.owl#someTerm", mmiUri.getTermUri(false, "#"));
    }
}
