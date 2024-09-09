package example.showmagnolia

import magnolia1._
import FastShowPretty._

import scala.language.experimental.macros

private[showmagnolia] trait MagnoliaShow {

  def join[A](caseClass: CaseClass[FastShowPretty, A]): FastShowPretty[A] = new FastShowPretty[A] {

    override def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder = {
      sb.append(caseClass.typeName.short)
      if (caseClass.parameters.nonEmpty) {
        sb.append("(\n")
        caseClass.parameters.foreach { param =>
          repeatAppend(sb, indent, nesting + 1).append(param.label).append(" = ")
          param.typeclass.showPretty(param.dereference(value), sb, indent, nesting + 1).append(",\n")
        }
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting).append(")")
      }
      sb
    }
  }

  def split[A](sealedTrait: SealedTrait[FastShowPretty, A]): FastShowPretty[A] = new FastShowPretty[A] {

    override def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      sealedTrait.split(value) { subtype =>
        subtype.typeclass.showPretty(subtype.cast(value), sb, indent, nesting)
      }
  }
}

object FastShowPrettySemiauto extends MagnoliaShow {

  type Typeclass[A] = FastShowPretty[A]

  def derived[A]: Typeclass[A] = macro Magnolia.gen[A]
}

object FastShowPrettyAuto extends MagnoliaShow {

  type Typeclass[A] = FastShowPretty[A]

  implicit def derivedShow[A]: Typeclass[A] = macro Magnolia.gen[A]
}
