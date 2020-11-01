package codes.quine.labo.redos

import java.util.concurrent.TimeoutException

import scala.util.Failure
import scala.util.Success

import automaton.Complexity
import automaton.Witness
import data.IChar

class DiagnosticsSuite extends munit.FunSuite {
  test("Diagnostics.buildAttack") {
    val w = Witness(Seq((Seq(IChar('a')), Seq(IChar('a')))), Seq(IChar('a')))
    assertEquals(
      Diagnostics.buildAttack(Complexity.Exponential(w)),
      w.buildAttack(Diagnostics.AttackSeqSizeForExponential)
    )
    assertEquals(
      Diagnostics.buildAttack(Complexity.Polynomial(3, w)),
      w.buildAttack(Math.pow(Diagnostics.AttackSeqTimeComplexity, 1.0 / 3).toInt)
    )
  }

  test("Diagnostics.from") {
    val w = Witness(Seq((Seq(IChar('a')), Seq(IChar('a')))), Seq(IChar('a')))
    assertEquals(
      Diagnostics.from(Success(Complexity.Exponential(w))),
      Diagnostics.Vulnerable(w.buildAttack(Diagnostics.AttackSeqSizeForExponential), Some(Complexity.Exponential(w)))
    )
    assertEquals(Diagnostics.from(Success(Complexity.Constant)), Diagnostics.Safe(Some(Complexity.Constant)))

    assertEquals(
      Diagnostics.from(Failure(new TimeoutException("foo"))),
      Diagnostics.Unknown(Diagnostics.ErrorKind.Timeout)
    )
    assertEquals(
      Diagnostics.from(Failure(new UnsupportedException("foo"))),
      Diagnostics.Unknown(Diagnostics.ErrorKind.Unsupported("foo"))
    )
    assertEquals(
      Diagnostics.from(Failure(new InvalidRegExpException("foo"))),
      Diagnostics.Unknown(Diagnostics.ErrorKind.InvalidRegExp("foo"))
    )

    val ex = interceptMessage[RuntimeException]("Unexpected exception") {
      Diagnostics.from(Failure(new RuntimeException("foo")))
    }
    assertEquals(ex.getCause.getMessage, "foo")
  }

  test("Diagnostics#complexity") {
    assertEquals(Diagnostics.Safe(Some(Complexity.Constant)).complexity, Some(Complexity.Constant))
    assertEquals(Diagnostics.Safe(None).complexity, None)
    val w = Witness(Seq((Seq(IChar('a')), Seq(IChar('a')))), Seq(IChar('a')))
    assertEquals(
      Diagnostics.Vulnerable(Seq.empty, Some(Complexity.Exponential(w))).complexity,
      Some(Complexity.Exponential(w))
    )
    assertEquals(Diagnostics.Vulnerable(Seq.empty, None).complexity, None)
    assertEquals(Diagnostics.Unknown(Diagnostics.ErrorKind.Timeout).complexity, None)
  }

  test("Diagnostics.ErrorKind#toString") {
    assertEquals(Diagnostics.ErrorKind.Timeout.toString, "timeout")
    assertEquals(Diagnostics.ErrorKind.Unsupported("foo").toString, "unsupported (foo)")
    assertEquals(Diagnostics.ErrorKind.InvalidRegExp("foo").toString, "invalid RegExp (foo)")
  }
}