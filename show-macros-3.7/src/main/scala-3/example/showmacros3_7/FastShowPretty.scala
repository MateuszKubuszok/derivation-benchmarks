package example.showmacros3_7

import example.showmacros3_7.internal.FastShowPrettyMacros

trait FastShowPretty[A] {

  def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder
}
object FastShowPretty extends FastShowPrettyLowPriorityImplicits
trait FastShowPrettyLowPriorityImplicits { this: FastShowPretty.type =>

  implicit inline def derived[A]: FastShowPretty[A] = ${ FastShowPrettyMacros.deriveFastShowPretty[A] }
}
