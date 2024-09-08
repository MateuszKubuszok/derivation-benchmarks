package example

import shapeless._
import shapeless.labelled.FieldType
import scala.reflect.ClassTag
import FastShowPretty._

object FastShowPrettyAuto {

  implicit def showHList[A, ARepr <: HList](implicit
      gen: LabelledGeneric.Aux[A, ARepr],
      classTag: ClassTag[A],
      instance: FastShowPretty[ARepr]
  ): FastShowPretty[A] = new FastShowPretty[A] {
    private val className = classTag.runtimeClass.getName

    def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder = {
      val repr: ARepr = gen.to(value)
      sb.append(className)
      if (repr.productArity > 0) {
        sb.append("(\n")
        instance.showPretty(repr, sb, indent, nesting + 1)
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting + 1).append(")")
      }
      sb
    }
  }

  implicit def showHCons[HeadName <: Symbol, Head, Tail <: HList](implicit
      name: Witness.Aux[HeadName],
      head: Lazy[FastShowPretty[Head]],
      tail: FastShowPretty[Tail]
  ): FastShowPretty[FieldType[HeadName, Head] :: Tail] = new FastShowPretty[FieldType[HeadName, Head] :: Tail] {
    private val fieldName = name.value.name

    def showPretty(
        value: FieldType[HeadName, Head] :: Tail,
        sb: StringBuilder,
        indent: String,
        nesting: Int
    ): StringBuilder = {
      repeatAppend(sb, indent, nesting + 1).append(fieldName).append(" = ")
      head.value.showPretty(value.head, sb, indent, nesting).append(",\n")
      tail.showPretty(value.tail, sb, indent, nesting)
    }
  }

  implicit val showHNil: FastShowPretty[HNil] = new FastShowPretty[HNil] {

    def showPretty(value: HNil, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      sb
  }

  implicit def showCoproduct[A, ARepr <: Coproduct](implicit
      gen: Generic.Aux[A, ARepr],
      instance: FastShowPretty[ARepr]
  ): FastShowPretty[A] = new FastShowPretty[A] {

    def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      instance.showPretty(gen.to(value), sb, indent, nesting)
  }

  implicit def showCCons[Head, Tail <: Coproduct](implicit
      head: Lazy[FastShowPretty[Head]],
      tail: FastShowPretty[Tail]
  ): FastShowPretty[Head :+: Tail] = new FastShowPretty[Head :+: Tail] {

    def showPretty(value: Head :+: Tail, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      value match {
        case Inl(h) => head.value.showPretty(h, sb, indent, nesting)
        case Inr(t) => tail.showPretty(t, sb, indent, nesting)
      }
  }

  implicit val showCNil: FastShowPretty[CNil] = new FastShowPretty[CNil] {

    def showPretty(value: CNil, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      ???
  }
}
