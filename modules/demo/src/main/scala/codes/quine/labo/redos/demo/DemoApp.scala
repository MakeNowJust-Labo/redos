package codes.quine.labo.redos
package demo

import scala.concurrent.duration._
import scala.util.matching.Regex

import org.scalajs.dom.document
import org.scalajs.dom.html.Button
import org.scalajs.dom.html.Input
import org.scalajs.dom.html.Paragraph
import org.scalajs.dom.raw.Event

import scalajs.js.JSON
import Diagnostics._
import automaton.Complexity._
import automaton.Witness
import data.IChar

/** DemoApp is an implementation of demo application in the top page. */
object DemoApp {

  /** A regular expression to extract slash separated input string. */
  val SlashRegExp: Regex = raw"/((?:[^/\\\[\n]*|\\[^\n]|\[(?:[^\\\]\n]|\\[^\n])*\])*)/([gimsuy]*)".r

  /** An input element to input a regexp pattern. */
  lazy val regexpInput: Input = document.querySelector("#regexp").asInstanceOf[Input]

  /** A button element to start analyzing. */
  lazy val checkButton: Button = document.querySelector("#check").asInstanceOf[Button]

  /** A element for showing analyzing result. */
  lazy val resultArea: Paragraph = document.querySelector("#result").asInstanceOf[Paragraph]

  /** A entrypoint of the application. */
  def main(args: Array[String]): Unit = {
    checkButton.addEventListener("click", (_: Event) => check())
  }

  /** A callback function on `checkButon` clicked. */
  def check(): Unit = {
    val input = regexpInput.value
    val (source, flags) = input match {
      case SlashRegExp(source, flags) => (source, flags)
      case _ =>
        resultArea.textContent = "An input text is invalid."
        return
    }

    val result = ReDoS.check(source, flags, 5.second)
    val pattern = s"<code>/${escape(source)}/${escape(flags)}</code>"
    result match {
      case Safe(complexity) =>
        complexity match {
          case Some(Constant) =>
            resultArea.innerHTML = s"$pattern is safe (constant-time matching)."
          case Some(Linear) =>
            resultArea.innerHTML = s"$pattern is safe (linear-time matching)."
          case None =>
            resultArea.innerHTML = s"$pattern is safe."
        }
      case Vulnerable(_, complexity) =>
        val unsafe = "<span class='has-text-danger has-text-weight-bold is-uppercase'>unsafe</span>"
        complexity match {
          case Some(Exponential(w)) =>
            resultArea.innerHTML = s"$pattern is $unsafe (exponential-time matching).<br>"
            val ws = witness(w).take(3).map { s => s"<code>${JSON.stringify(escape(s))}</code>" }
            resultArea.innerHTML ++= s"Example attack strings: ${ws.mkString(", ")}, ..."
          case Some(Polynomial(d, w)) =>
            resultArea.innerHTML = s"$pattern is $unsafe ($d${ordinal(d)} degree polynomial-time matching).<br>"
            val ws = witness(w).take(3).map { s => s"<code>${JSON.stringify(escape(s))}</code>" }
            resultArea.innerHTML ++= s"Example attack strings: ${ws.mkString(", ")}, ..."
          case None =>
            resultArea.innerHTML = s"$pattern is $unsafe."
        }
      case Unknown(err) =>
        resultArea.innerHTML = s"An error is occured on anaylyzing: $err"
    }
  }

  /** Returns an ordinal suffix for the integer value. */
  def ordinal(d: Int): String =
    (d % 10).abs match {
      case 1 => "st"
      case 2 => "nd"
      case 3 => "rd"
      case _ => "th"
    }

  /** Constructs a witness strings. */
  def witness(w: Witness[IChar]): LazyList[String] =
    w.map(_.head.asString).toLazyList.map(_.mkString).drop(1)

  /** Returns an HTML escaped string. */
  def escape(s: String): String =
    s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#039;")
}