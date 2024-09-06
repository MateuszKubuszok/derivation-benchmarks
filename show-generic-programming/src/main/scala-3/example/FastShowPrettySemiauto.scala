package example

import scala.deriving.*

object FastShowPrettySemiauto {
  
  case class DerivedFastShowPretty[A](instance: FastShowPretty[A]) extends AnyVal

  // uses  FastShowPretty[Element] for each Product/Sum element
  // gives DerivedFastShowPretty[A] =!= FastShowPretty[A]
  inline def deriveShow[A](using m: Mirror.Of[A]): DerivedFastShowPretty[A] =
    // this wouldn't be so easy with Shapeless
    DerivedFastShowPretty(FastShowPrettyAuto.deriveShowAutomatic)
}
