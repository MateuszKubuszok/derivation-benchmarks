package example.showmagnolia

import magnolia1.*
import FastShowPretty.*

import scala.deriving.Mirror

private[showmagnolia] trait MagnoliaShow extends Derivation[FastShowPretty] {

  def join[A](caseClass: CaseClass[FastShowPretty, A]): FastShowPretty[A] = new FastShowPretty[A] {

    override def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder = {
      sb.append(caseClass.typeInfo.short)
      if (caseClass.parameters.nonEmpty) {
        sb.append("(\n")
        caseClass.parameters.foreach { param =>
          repeatAppend(sb, indent, nesting + 1).append(param.label).append(" = ")
          param.typeclass.showPretty(param.deref(value), sb, indent, nesting + 1).append(",\n")
        }
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting).append(")")
      }
      sb
    }
  }

  def split[A](sealedTrait: SealedTrait[FastShowPretty, A]): FastShowPretty[A] = new FastShowPretty[A] {

    override def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      sealedTrait.choose(value) { subtype =>
        subtype.typeclass.showPretty(subtype.cast(value), sb, indent, nesting)
      }
  }
}

object FastShowPrettySemiauto extends MagnoliaShow

object FastShowPrettyAuto extends MagnoliaShow {

  implicit inline def deriveShow[A](implicit m: Mirror.Of[A]): FastShowPretty[A] = derived
}
