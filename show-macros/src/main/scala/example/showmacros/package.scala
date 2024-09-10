package example

package object showmacros {

  implicit class FastShowPrettyOps[A](private val value: A) {

    def showPretty(indent: String = "  ", nesting: Int = 0)(implicit fsp: FastShowPretty.AutoDerived[A]): String =
      fsp.showPretty(value, new StringBuilder, indent, nesting).toString()
  }
}
