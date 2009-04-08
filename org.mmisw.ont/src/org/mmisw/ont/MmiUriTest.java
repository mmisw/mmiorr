package org.mmisw.ont;

import java.net.URISyntaxException;

import junit.framework.TestCase;

/**
 * @author Carlos Rueda
 */
public class MmiUriTest extends TestCase {
    public void testBasic0() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/someTerm.owl");
    
		assertEquals("http://mmisw.org/ont/mmi/someVocab",          mmiUri.getOntologyUri());
		assertEquals("http://mmisw.org/ont/mmi/someVocab/someTerm", mmiUri.getTermUri());
        assertEquals("mmi",       mmiUri.getAuthority());
        assertEquals(null,        mmiUri.getVersion());
        assertEquals("someVocab", mmiUri.getTopic());
        assertEquals("someTerm",  mmiUri.getTerm());
        assertEquals(".owl",      mmiUri.getExtension());
        assertEquals("http://mmisw.org/ont/",  mmiUri.getUntilRoot());
    }
    
    public void testBasic1() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/20081101/someVocab/someTerm.owl");
    
		assertEquals("http://mmisw.org/ont/mmi/20081101/someVocab", mmiUri.getOntologyUri());
        assertEquals("mmi",       mmiUri.getAuthority());
        assertEquals("20081101",  mmiUri.getVersion());
        assertEquals("someVocab", mmiUri.getTopic());
        assertEquals("someTerm",  mmiUri.getTerm());
        assertEquals(".owl",      mmiUri.getExtension());
    }
    
    public void testBasic2() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab.owl/someTerm");
    
		assertEquals("http://mmisw.org/ont/mmi/someVocab", mmiUri.getOntologyUri());
        assertEquals("mmi", mmiUri.getAuthority());
        assertEquals("someVocab", mmiUri.getTopic());
        assertEquals("someTerm", mmiUri.getTerm());
        assertEquals(".owl", mmiUri.getExtension());
    }
    
    public void testTermUris() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab.owl/someTerm");
    
    	assertEquals(".owl", mmiUri.getExtension());
    	
        assertEquals("http://mmisw.org/ont/mmi/someVocab#someTerm", mmiUri.getTermUri("#"));
        assertEquals("http://mmisw.org/ont/mmi/someVocab/someTerm", mmiUri.getTermUri("/"));
    }

    public void testNoTerm() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab");
    
    	assertEquals("", mmiUri.getTerm());
    	assertEquals("mmi", mmiUri.getAuthority());
    	assertEquals("someVocab", mmiUri.getTopic());
    	assertEquals(null, mmiUri.getVersion());
    }

    public void testNoTerm2() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/");
    
    	assertEquals("", mmiUri.getTerm());
    	assertEquals(null, mmiUri.getVersion());
    }

    public void testTopicExt() throws URISyntaxException {
    	// topic without extension
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/someTerm");
    
    	assertEquals("", mmiUri.getExtension());

    	// ontologyUri with an extension:
        assertEquals("http://mmisw.org/ont/mmi/someVocab.owl", mmiUri.getOntologyUriWithExtension(".owl"));
    }

    public void testVersionNull() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/someTerm");
    
    	assertEquals(null, mmiUri.getVersion());
    }
    public void testVersions() throws URISyntaxException {
    	MmiUri.checkVersion("2008");
    	MmiUri.checkVersion("200810");
    	MmiUri.checkVersion("20081030");
    	MmiUri.checkVersion("20081030T21");
    	MmiUri.checkVersion("20081030T2130");
    	MmiUri.checkVersion("20081030T213059");
    	MmiUri.checkVersion(MmiUri.LATEST_VERSION_INDICATOR);
    }
    public void testVersion1() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/20081021/someVocab/someTerm");
    	assertEquals("20081021", mmiUri.getVersion());
    }
    public void testVersionInvalid() throws URISyntaxException {
    	try {
    		new MmiUri("http://mmisw.org/ont/mmi/2008x1021/someVocab/someTerm");
    		fail(); // test fails!
    	}
    	catch (URISyntaxException ok) {
    	}
    }
    public void testVersionInvalid2() throws URISyntaxException {
    	try {
    		// Note: 4 parts={mmi, badversion, someVocab, someTerm} forces parts[1] to be the version,
    		// which is malformed in this case.
    		new MmiUri("http://mmisw.org/ont/mmi/badversion/someVocab/someTerm");
    		fail(); // test fails!
    	}
    	catch (URISyntaxException ok) {
    	}
    }
    public void testExtensionAndVersion() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/20081021/someVocab.owl/someTerm");
    
    	assertEquals(".owl", mmiUri.getExtension());

    	// ontologyUri with an extension:
        assertEquals("http://mmisw.org/ont/mmi/20081021/someVocab.n3", mmiUri.getOntologyUriWithExtension(".n3"));
    }

    public void testChangeExt1() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/a/20081021/v/t");
    
    	assertEquals("", mmiUri.getExtension());
    	assertEquals("http://mmisw.org/ont/a/20081021/v.owl", mmiUri.getOntologyUriWithExtension(".owl"));
    }
    public void testChangeExt2() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/a/20081021/v.owl/t");
    
    	assertEquals(".owl", mmiUri.getExtension());
    	assertEquals("http://mmisw.org/ont/a/20081021/v", mmiUri.getOntologyUriWithExtension(""));
    }

    public void testCloneCopy() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/a/20081021/v.owl/t");
    
    	MmiUri clone = mmiUri.clone();
		assertEquals(mmiUri, clone);
		
		MmiUri copyVer;
		
		copyVer = mmiUri.copyWithVersion(null);
		assertEquals("http://mmisw.org/ont/a/v", copyVer.getOntologyUri());
		
		copyVer = mmiUri.copyWithVersion("20210121");
		assertEquals("http://mmisw.org/ont/a/20210121/v", copyVer.getOntologyUri());
		
		copyVer = mmiUri.copyWithVersionNoCheck("%myversion%");
		assertEquals("http://mmisw.org/ont/a/%myversion%/v", copyVer.getOntologyUri());
    }
 
    public void testCopyWithExtension() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab.owl/someTerm");    
    	MmiUri copy = mmiUri.copyWithExtension(".NEW");
    	assertEquals(".NEW", copy.getExtension());
    	assertEquals("http://mmisw.org/ont/mmi/someVocab/someTerm", copy.getTermUri("/"));
    }

    public void testGetOntologyUri1() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/someTerm.owl");
    	assertEquals("http://mmisw.org/ont/mmi/someVocab", mmiUri.getOntologyUri());
    }
    public void testGetOntologyUri2() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab.html/someTerm");
    	assertEquals("http://mmisw.org/ont/mmi/someVocab", mmiUri.getOntologyUri());
    }

    /** See <a href="http://code.google.com/p/mmisw/issues/detail?id=123">Issue #123</a> */
    public void testEncodedUri() throws URISyntaxException {
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab/some%20Term");
    	assertEquals("some%20Term", mmiUri.getTerm());
    }

}
