package org.mmisw.ont.mmiuri.test;

import java.net.URISyntaxException;

import org.mmisw.ont.mmiuri.MmiUri;

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
    	assertEquals("some Term", new MmiUri("http://mmisw.org/ont/mmi/voc/some%20Term").getTerm());
    	assertEquals("ångström",  new MmiUri("http://mmisw.org/ont/mmi/voc/%C3%A5ngstr%C3%B6m").getTerm());
    	assertEquals("[brakets]", new MmiUri("http://mmisw.org/ont/mmi/voc/%5Bbrakets%5D").getTerm());

        MmiUri m = new MmiUri("http://mmisw.org/ont/qu%C3%A9/ontolog%C3%ADa/t%C3%A9rmino");
    	assertEquals("qué",       m.getAuthority());
    	assertEquals("ontología", m.getTopic());
    	assertEquals("término",   m.getTerm());

        assertEquals("µß∑π", new MmiUri("http://mmisw.org/ont/mmi/voc/%C2%B5%C3%9F%E2%88%91%CF%80").getTerm());
    }

    
    public void testAuthorityStartingWithHyphenIsInvalid() throws URISyntaxException {
    	try {
    		new MmiUri("http://mmisw.org/ont/-/img/mmior.gif");
    		fail(); // test fails!
    	}
    	catch (URISyntaxException ok) {
    	}
    }

    public void testExtensions() throws URISyntaxException {
    	// multiple extensions but equal
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab.owl/someTerm.owl");
    	assertEquals(".owl", mmiUri.getExtension());

    	// multiple extensions but different
    	try {
    		new MmiUri("http://mmisw.org/ont/mmi/someVocab.n3/someTerm.owl");
    		fail(); // test fails!
    	}
    	catch (URISyntaxException ok) {
    	}
    	
    	// multiple extensions but equal, including extension in the authority
    	mmiUri = new MmiUri("http://mmisw.org/ont/mmi.rdf/someVocab.rdf/someTerm.rdf", true);
    	assertEquals(".rdf", mmiUri.getExtension());
    }

    public void testAcceptUntilAuthority() throws URISyntaxException {
    	
    	// actually only until the authority:
    	MmiUri mmiUri = new MmiUri("http://mmisw.org/ont/mmi", true);
    	assertEquals("mmi", mmiUri.getAuthority());
    	assertEquals("", mmiUri.getExtension());
    	assertEquals("", mmiUri.getTopic());
    	
    	// actually only until the authority including extension:
    	mmiUri = new MmiUri("http://mmisw.org/ont/mmi.rdf", true);
    	assertEquals("mmi", mmiUri.getAuthority());
    	assertEquals(".rdf", mmiUri.getExtension());
    	assertEquals("", mmiUri.getTopic());
    	
    	// but with other stuff as well:
    	mmiUri = new MmiUri("http://mmisw.org/ont/mmi/someVocab", true);
    	assertEquals("someVocab", mmiUri.getTopic());
    	assertEquals("mmi", mmiUri.getAuthority());
    }
}
