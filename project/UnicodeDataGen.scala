import java.io.File

import sbt.io.IO
import sbt.io.syntax._

/** UnicodeDataGen is a common interface for Unicode data generator. */
trait UnicodeDataGen {

  /** A file to generate. */
  def file(dir: File): File

  /** A generated source code. */
  def source(pkg: String): String

  /** Run this generator. */
  def gen(pkg: String, dir: File): Unit =
    IO.write(file(dir), source(pkg))
}
