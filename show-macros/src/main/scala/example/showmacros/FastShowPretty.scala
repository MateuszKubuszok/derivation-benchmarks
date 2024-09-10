package example.showmacros

trait FastShowPretty[A] extends FastShowPretty.AutoDerived[A] {

  def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder
}
object FastShowPretty extends FastShowPrettyCompanion {

  trait AutoDerived[A] {

    def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder
  }
  object AutoDerived extends FastShowPrettyAutoDerivedCompanion
}
