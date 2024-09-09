package example.showmacros

import example.showmacros.internal.FastShowPrettyMacros

trait FastShowPrettyCompanion { this: FastShowPretty.type =>

  inline def derived[A]: FastShowPretty[A] = ${ FastShowPrettyMacros.deriveFastShowPretty[A] }
}

trait FastShowPrettyAutoDerivedCompanion { this: FastShowPretty.AutoDerived.type =>

  inline implicit def derived[A]: FastShowPretty.AutoDerived[A] = ${ FastShowPrettyMacros.deriveFastShowPretty[A] }
}
