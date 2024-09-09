package example.showgeneric

import shapeless._
import shapeless.labelled.FieldType
import scala.reflect.ClassTag
import FastShowPretty._

object FastShowPrettySemiauto {

  case class DerivedFastShowPretty[A](instance: FastShowPretty[A]) extends AnyVal

  def deriveShow[A](implicit derived: DerivedFastShowPretty[A]): FastShowPretty[A] = derived.instance

  implicit def showHList[A, ARepr <: HList](implicit
      gen: LabelledGeneric.Aux[A, ARepr],
      classTag: ClassTag[A],
      instance: DerivedFastShowPretty[ARepr]
  ): DerivedFastShowPretty[A] = DerivedFastShowPretty(new FastShowPretty[A] {
    private val className = classTag.runtimeClass.getName

    def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder = {
      val repr: ARepr = gen.to(value)
      sb.append(className)
      if (repr.productArity > 0) {
        sb.append("(\n")
        instance.instance.showPretty(repr, sb, indent, nesting + 1)
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting + 1).append(")")
      }
      sb
    }
  })

  implicit def showHCons[HeadName <: Symbol, Head, Tail <: HList](implicit
      name: Witness.Aux[HeadName],
      head: Lazy[FastShowPretty[Head]],
      tail: DerivedFastShowPretty[Tail]
  ): DerivedFastShowPretty[FieldType[HeadName, Head] :: Tail] = DerivedFastShowPretty(
    new FastShowPretty[FieldType[HeadName, Head] :: Tail] {
      private val fieldName = name.value.name

      def showPretty(
          value: FieldType[HeadName, Head] :: Tail,
          sb: StringBuilder,
          indent: String,
          nesting: Int
      ): StringBuilder = {
        repeatAppend(sb, indent, nesting + 1).append(fieldName).append(" = ")
        head.value.showPretty(value.head, sb, indent, nesting).append(",\n")
        tail.instance.showPretty(value.tail, sb, indent, nesting)
      }
    }
  )

  implicit val showHNil: DerivedFastShowPretty[HNil] = DerivedFastShowPretty(new FastShowPretty[HNil] {

    def showPretty(value: HNil, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      sb
  })

  implicit def showCoproduct[A, ARepr <: Coproduct](implicit
      gen: Generic.Aux[A, ARepr],
      instance: DerivedFastShowPretty[ARepr]
  ): DerivedFastShowPretty[A] = DerivedFastShowPretty(new FastShowPretty[A] {

    def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      instance.instance.showPretty(gen.to(value), sb, indent, nesting)
  })

  implicit def showCCons[Head, Tail <: Coproduct](implicit
      head: Lazy[FastShowPretty[Head]],
      tail: DerivedFastShowPretty[Tail]
  ): DerivedFastShowPretty[Head :+: Tail] = DerivedFastShowPretty(new FastShowPretty[Head :+: Tail] {

    def showPretty(value: Head :+: Tail, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      value match {
        case Inl(h) => head.value.showPretty(h, sb, indent, nesting)
        case Inr(t) => tail.instance.showPretty(t, sb, indent, nesting)
      }
  })

  implicit val showCNil: DerivedFastShowPretty[CNil] = DerivedFastShowPretty(new FastShowPretty[CNil] {

    def showPretty(value: CNil, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
      ???
  })
}
