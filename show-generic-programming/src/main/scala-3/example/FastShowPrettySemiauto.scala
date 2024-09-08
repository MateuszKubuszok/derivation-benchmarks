package example

import scala.deriving.*

object FastShowPrettySemiauto {

  inline def deriveShow[A](using m: Mirror.Of[A]): FastShowPretty[A] =
    // this wouldn't be so easy with Shapeless
    FastShowPrettyAuto.deriveShowAutomatic


}
