package codes.quine.labo.redos
package automaton

/** Witness is a witness, which is a pump string with suffix.
  *
  * For example, when a witness object forms `Witness(Seq((x, y), (z, w)), u)`,
  * an actual witness string is `x y^n z w^n u` for any integer `n`.
  */
final case class Witness[A](pumps: Seq[(Seq[A], Seq[A])], suffix: Seq[A]) {

  /** Transforms each values of this witness by the function. */
  def map[B](f: A => B): Witness[B] =
    Witness(pumps.map { case (pre, pump) => (pre.map(f), pump.map(f)) }, suffix.map(f))

  /** Builds an attack string with `n` times repetition. */
  def buildAttack(n: Int): Seq[A] =
    pumps.flatMap { case (pre, pump) => pre ++ Vector.fill(n)(pump).flatten } ++ suffix

  /** Constructs a witness strings.
    *
    * This result's `n`-th element means `n` times repeated sequence.
    */
  def toLazyList: LazyList[Seq[A]] =
    LazyList.from(0).map(buildAttack(_))
}