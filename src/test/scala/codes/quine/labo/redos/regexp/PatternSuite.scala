package codes.quine.labo.redos
package regexp

import minitest.SimpleTestSuite

import Pattern._
import data.UChar

object PatternSuite extends SimpleTestSuite {
  test("Pattern.showNode") {
    val x = Character(UChar('x'))
    assertEquals(showNode(Disjunction(Seq(Disjunction(Seq(x, x)), x))), "(?:x|x)|x")
    assertEquals(showNode(Disjunction(Seq(x, x, x))), "x|x|x")
    assertEquals(showNode(Sequence(Seq(Disjunction(Seq(x, x)), x))), "(?:x|x)x")
    assertEquals(showNode(Sequence(Seq(Sequence(Seq(x, x)), x))), "(?:xx)x")
    assertEquals(showNode(Sequence(Seq(x, x, x))), "xxx")
    assertEquals(showNode(Capture(x)), "(x)")
    assertEquals(showNode(NamedCapture("foo", x)), "(?<foo>x)")
    assertEquals(showNode(Group(x)), "(?:x)")
    assertEquals(showNode(Star(false, x)), "x*")
    assertEquals(showNode(Star(true, x)), "x*?")
    assertEquals(showNode(Star(false, Disjunction(Seq(x, x)))), "(?:x|x)*")
    assertEquals(showNode(Star(false, Sequence(Seq(x, x)))), "(?:xx)*")
    assertEquals(showNode(Star(false, Star(false, x))), "(?:x*)*")
    assertEquals(showNode(Star(false, LookAhead(false, x))), "(?:(?=x))*")
    assertEquals(showNode(Star(false, LookBehind(false, x))), "(?:(?<=x))*")
    assertEquals(showNode(Plus(false, x)), "x+")
    assertEquals(showNode(Plus(true, x)), "x+?")
    assertEquals(showNode(Question(false, x)), "x?")
    assertEquals(showNode(Question(true, x)), "x??")
    assertEquals(showNode(Repeat(false, 3, None, x)), "x{3}")
    assertEquals(showNode(Repeat(true, 3, None, x)), "x{3}?")
    assertEquals(showNode(Repeat(false, 3, Some(None), x)), "x{3,}")
    assertEquals(showNode(Repeat(true, 3, Some(None), x)), "x{3,}?")
    assertEquals(showNode(Repeat(false, 3, Some(Some(5)), x)), "x{3,5}")
    assertEquals(showNode(Repeat(true, 3, Some(Some(5)), x)), "x{3,5}?")
    assertEquals(showNode(WordBoundary(false)), "\\b")
    assertEquals(showNode(WordBoundary(true)), "\\B")
    assertEquals(showNode(LineBegin), "^")
    assertEquals(showNode(LineEnd), "$")
    assertEquals(showNode(LookAhead(false, x)), "(?=x)")
    assertEquals(showNode(LookAhead(true, x)), "(?!x)")
    assertEquals(showNode(LookBehind(false, x)), "(?<=x)")
    assertEquals(showNode(LookBehind(true, x)), "(?<!x)")
    assertEquals(showNode(Character(UChar('/'))), "\\/")
    assertEquals(showNode(Character(UChar(1))), "\\cA")
    assertEquals(showNode(Character(UChar('A'))), "A")
    assertEquals(showNode(CharacterClass(false, Seq(x))), "[x]")
    assertEquals(showNode(CharacterClass(false, Seq(ClassRange(UChar('a'), UChar('z'))))), "[a-z]")
    assertEquals(showNode(CharacterClass(false, Seq(SimpleEscapeClass(false, EscapeClassKind.Word)))), "[\\w]")
    assertEquals(showNode(CharacterClass(false, Seq(Character(UChar(1))))), "[\\cA]")
    assertEquals(showNode(CharacterClass(false, Seq(Character(UChar('-'))))), "[\\-]")
    assertEquals(showNode(CharacterClass(true, Seq(x))), "[^x]")
    assertEquals(showNode(SimpleEscapeClass(false, EscapeClassKind.Digit)), "\\d")
    assertEquals(showNode(SimpleEscapeClass(true, EscapeClassKind.Digit)), "\\D")
    assertEquals(showNode(SimpleEscapeClass(false, EscapeClassKind.Word)), "\\w")
    assertEquals(showNode(SimpleEscapeClass(true, EscapeClassKind.Word)), "\\W")
    assertEquals(showNode(SimpleEscapeClass(false, EscapeClassKind.Space)), "\\s")
    assertEquals(showNode(SimpleEscapeClass(true, EscapeClassKind.Space)), "\\S")
    assertEquals(showNode(UnicodeProperty(false, "ASCII")), "\\p{ASCII}")
    assertEquals(showNode(UnicodeProperty(true, "ASCII")), "\\P{ASCII}")
    assertEquals(showNode(UnicodePropertyValue(false, "sc", "Hira")), "\\p{sc=Hira}")
    assertEquals(showNode(UnicodePropertyValue(true, "sc", "Hira")), "\\P{sc=Hira}")
    assertEquals(showNode(Dot), ".")
    assertEquals(showNode(BackReference(1)), "\\1")
    assertEquals(showNode(NamedBackReference("foo")), "\\k<foo>")
  }

  test("Pattern.showFlagSet") {
    assertEquals(showFlagSet(FlagSet(false, false, false, false, false, false)), "")
    assertEquals(showFlagSet(FlagSet(true, true, true, true, true, true)), "gimsuy")
  }

  test("Pattern#toString") {
    assertEquals(
      Pattern(Character(UChar('x')), FlagSet(true, true, false, false, false, false)).toString,
      "/x/gi"
    )
  }
}