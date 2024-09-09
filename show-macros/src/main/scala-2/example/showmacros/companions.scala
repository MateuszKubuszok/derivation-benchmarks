package example.showmacros

import example.showmacros.internal.FastShowPrettyMacros

import scala.language.experimental.macros

trait FastShowPrettyCompanion { this: FastShowPretty.type =>

  def derived[A]: FastShowPretty[A] = macro FastShowPrettyMacros.deriveFastShowPretty[A]
}

trait FastShowPrettyAutoDerivedCompanion { this: FastShowPretty.AutoDerived.type =>

  implicit def derived[A]: FastShowPretty.AutoDerived[A] = macro FastShowPrettyMacros.deriveFastShowPretty[A]
}
