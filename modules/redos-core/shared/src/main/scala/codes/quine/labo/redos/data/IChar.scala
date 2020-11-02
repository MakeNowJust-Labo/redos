package codes.quine.labo.redos.data

import unicode.IntervalSet
import unicode.IntervalSet._
import unicode.CaseMap
import unicode.CaseMap.Conversion
import unicode.Property

/** IChar is a code point interval set with extra informations. */
final case class IChar(
    set: IntervalSet[UChar],
    isLineTerminator: Boolean = false,
    isWord: Boolean = false
) extends Ordered[IChar] {

  /** Marks this set contains line terminator characters. */
  def withLineTerminator: IChar = copy(isLineTerminator = true)

  /** Marks this set contains word characters. */
  def withWord: IChar = copy(isWord = true)

  /** Checks whether this interval set is empty or not. */
  def isEmpty: Boolean = set.isEmpty

  /** Negates [[isEmpty]]. */
  def nonEmpty: Boolean = set.nonEmpty

  /** Computes a complement of this interval set. */
  def complement(unicode: Boolean): IChar = {
    val (xys, z) = set.intervals.foldLeft((IndexedSeq.empty[(UChar, UChar)], UChar(0))) { case ((seq, x), (y, z)) =>
      (seq :+ (x, y), z)
    }
    IChar(IntervalSet.from(xys :+ (z, UChar(if (unicode) 0x110000 else 0x10000))), isLineTerminator, isWord)
  }

  /** Computes a union of two interval sets. */
  def union(that: IChar): IChar =
    IChar(set.union(that.set), isLineTerminator || that.isLineTerminator, isWord || that.isWord)

  /** Computes a partition of two interval sets. */
  def partition(that: IChar): Partition[IChar] = {
    val Partition(is, ls, rs) = set.partition(that.set)
    val ic = IChar(is, isLineTerminator || that.isLineTerminator, isWord || that.isWord)
    Partition(ic, copy(set = ls), that.copy(set = rs))
  }

  /** Computes a difference interval set. */
  def diff(that: IChar): IChar = partition(that).diffThat

  /** Checks whether the character is contained in this interval set or not. */
  def contains(value: UChar): Boolean = set.contains(value)

  /** Gets the first code poiint of this interval set.
    *
    * If this interval set is empty, it throws a NoSuchElementException.
    */
  def head: UChar = set.intervals.head._1

  /** Compares to other code point interval set. */
  def compare(that: IChar): Int = IChar.ordering.compare(this, that)

  /** Converts to string representation. */
  override def toString: String = {
    // This `showUCharInClass` is copied from `Pattern` for avoiding a circular dependency.
    def showUCharInClass(u: UChar): String =
      if (u.value.isValidChar && "[^-]".contains(u.value.toChar)) s"\\${u.value.toChar}"
      else if (1 <= u.value && u.value < 32) s"\\c${(u.value + 0x40).toChar}"
      else u.toString
    val cls = set.intervals.map {
      case (x, y) if x.value + 1 == y.value => showUCharInClass(x)
      case (x, y)                           => s"${showUCharInClass(x)}-${showUCharInClass(UChar(y.value - 1))}"
    }
    cls.mkString("[", "", "]" ++ (if (isLineTerminator) "n" else "") ++ (if (isWord) "w" else ""))
  }
}

/** IChar utilities. */
object IChar {

  /** [[scala.math.Ordering]] instance for [[IChar]]. */
  private val ordering: Ordering[IChar] =
    Ordering.by[IChar, IntervalSet[UChar]](_.set).orElseBy(_.isLineTerminator).orElseBy(_.isWord)

  /** Creates an interval set containing any code points. */
  def Any: IChar = IChar(IntervalSet((UChar(0), UChar(0x110000))))

  /** Creates an interval set containing any UTF-16 code points. */
  def Any16: IChar = IChar(IntervalSet((UChar(0), UChar(0x10000))))

  /** Returns an interval set containing digit characters. */
  def Digit: IChar = IChar(IntervalSet((UChar('0'), UChar('9' + 1))))

  /** Returns an interval set containing white-space characters. */
  def Space: IChar = IChar(
    IntervalSet(
      (UChar('\t'), UChar('\t' + 1)), // <TAB>
      (UChar(0x000a), UChar(0x000d + 1)), // <LF>, <VT>, <FF>, <CR>
      (UChar(0x0020), UChar(0x0020 + 1)), // <SP>
      (UChar(0x00a0), UChar(0x00a0 + 1)), // <NBSP>
      (UChar(0x2028), UChar(0x2029 + 1)), // <LS>, <PS>
      (UChar(0xfeff), UChar(0xfeff + 1)) // <ZWNBSP>
    ).union(Property.generalCategory("Space_Separator").get)
  )

  /** Returns an interval set containing line-terminator characters. */
  def LineTerminator: IChar = IChar(
    IntervalSet(
      (UChar(0x000a), UChar(0x000a + 1)), // <LF>
      (UChar(0x000d), UChar(0x000d + 1)), // <CR>
      (UChar(0x2028), UChar(0x2029 + 1)) // <LS>, <PS>
    )
  )

  /** Returns an interval set containing word characters. */
  def Word: IChar = IChar(
    IntervalSet(
      (UChar('0'), UChar('9' + 1)),
      (UChar('A'), UChar('Z' + 1)),
      (UChar('_'), UChar('_' + 1)),
      (UChar('a'), UChar('z' + 1))
    )
  )

  /** Return an interval set containing the unicode property code points. */
  def UnicodeProperty(name: String): Option[IChar] =
    Property.generalCategory(name).orElse(Property.binary(name)).map(IChar(_))

  /** Return an interval set containing the unicode property code points. */
  def UnicodePropertyValue(name: String, value: String): Option[IChar] = {
    val optSet = Property.NonBinaryPropertyAliases.getOrElse(name, name) match {
      case "General_Category"  => Property.generalCategory(value)
      case "Script"            => Property.script(value)
      case "Script_Extensions" => Property.scriptExtensions(value)
      case _                   => None
    }
    optSet.map(IChar(_))
  }

  /** Creates an interval set containing the character only. */
  def apply(ch: UChar): IChar = IChar(IntervalSet((ch, UChar(ch.value + 1))))

  /** Creates an empty interval set. */
  def empty: IChar = IChar(IntervalSet.empty[UChar])

  /** Creates an interval set ranged in [begin, end]. */
  def range(begin: UChar, end: UChar): IChar = IChar(IntervalSet((begin, UChar(end.value + 1))))

  /** Computes union of the interval sets. */
  def union(chars: Seq[IChar]): IChar =
    chars.foldLeft(IChar.empty)(_.union(_))

  /** Normalizes the code point interval set. */
  def canonicalize(c: IChar, unicode: Boolean): IChar = {
    val conversions = if (unicode) CaseMap.Fold else CaseMap.Upper
    val set = conversions.foldLeft(c.set) { case (set, Conversion(dom, offset)) =>
      set.mapIntersection(dom)(u => UChar(u.value + offset))
    }
    c.copy(set = set)
  }
}