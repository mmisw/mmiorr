package org.mmisw.ont;

import junit.framework.TestCase;

import java.util.regex.Matcher;

public class MiscTest extends TestCase {

  public void testInvalidIriCharacters() {
    assertSomeInvalidIriCharacter("http://ex/with blank");
    assertSomeInvalidIriCharacter("http://ex/with\ttab");
    assertSomeInvalidIriCharacter("http://ex/with\ffeed-form");
    assertSomeInvalidIriCharacter("http://ex/with\nnewline");
    assertSomeInvalidIriCharacter("http://ex/with\rcr");
    assertSomeInvalidIriCharacter("http://ex/foo`");
    assertSomeInvalidIriCharacter("<http://ex/foo>");
    assertSomeInvalidIriCharacter("http://ex/\"foo\"");
    assertSomeInvalidIriCharacter("http://ex/{foo}");
    assertSomeInvalidIriCharacter("http://ex/foo|");
    assertSomeInvalidIriCharacter("http://ex/\\foo");
    assertSomeInvalidIriCharacter("http://ex/^foo");
    assertSomeInvalidIriCharacter("http://ex/`foo`");
  }

  public void testValidIriCharacters() {
    // not many tests here (more interested in the invalid cases)
    assertOnlyValidIriCharacters("http://ex/good");
    assertOnlyValidIriCharacters("http://ex/Ω");
  }

  private void assertSomeInvalidIriCharacter(String s) {
    Matcher m = UriDispatcher.goodIriCharactersPattern.matcher(s);
    assertFalse("'" + s + "' must have some invalid IRI character", m.matches());
  }

  private void assertOnlyValidIriCharacters(String s) {
    Matcher m = UriDispatcher.goodIriCharactersPattern.matcher(s);
    assertTrue("'" + s + "' must only have valid IRI characters", m.matches());
  }

}
