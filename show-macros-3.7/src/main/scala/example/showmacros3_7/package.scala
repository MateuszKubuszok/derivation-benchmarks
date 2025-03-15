package example

package object showmacros3_7 {

  implicit class FastShowPrettyOps[A](private val value: A) {

    def showPretty(indent: String = "  ", nesting: Int = 0)(implicit fsp: FastShowPretty[A]): String =
      fsp.showPretty(value, new StringBuilder, indent, nesting).toString()
  }
}
