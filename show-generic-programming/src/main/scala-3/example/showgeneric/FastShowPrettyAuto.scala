package example.showgeneric

import scala.compiletime.*
import scala.deriving.*
import FastShowPretty.*

object FastShowPrettyAuto {

  // uses  FastShowPretty[Element] for each Product/Sum element
  // gives FastShowPretty[A]
  implicit inline def deriveShowAutomatic[A](implicit m: Mirror.Of[A]): FastShowPretty[A] =
    inline m match {
      case p: Mirror.ProductOf[A]   => deriveProduct[A](using p)
      case s: Mirror.SumOf[A]       => deriveSumType[A](using s)
      case _: Mirror.Singleton      => primitive(_ => valueOf[m.MirroredLabel])
      case _: Mirror.SingletonProxy => primitive(_ => valueOf[m.MirroredLabel])
    }

  inline def deriveProduct[A](implicit p: Mirror.ProductOf[A]): FastShowPretty[A] = ProductImpl(
    name = valueOf[p.MirroredLabel],
    meta = {
      lazy val labels =
        summonAll[Tuple.Map[p.MirroredElemLabels, ValueOf]].toList.asInstanceOf[List[ValueOf[String]]].map(_.value)
      lazy val instances =
        summonAll[Tuple.Map[p.MirroredElemTypes, FastShowPretty]].toList.asInstanceOf[List[FastShowPretty[Any]]]
      labels.zip(instances).toArray
    }
  )
  class ProductImpl[A](name: String, meta: Array[(String, FastShowPretty[Any])]) extends FastShowPretty[A] {

    def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder = {
      sb.append(name)
      if meta.nonEmpty then {
        sb.append("(\n")
        value.asInstanceOf[Product].productIterator.zipWithIndex.foreach { case (field, idx) =>
          val (fieldName, typeclass) = meta(idx)
          repeatAppend(sb, indent, nesting + 1).append(fieldName).append(" = ")
          typeclass.showPretty(field, sb, indent, nesting + 1).append(",\n")
        }
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting).append(")")
      }
      sb
    }
  }

  inline def deriveSumType[A](implicit s: Mirror.SumOf[A]): FastShowPretty[A] = SumType[A](
    s = s,
    instances =
      summonAll[Tuple.Map[s.MirroredElemTypes, FastShowPretty]].toList.asInstanceOf[List[FastShowPretty[Any]]].toArray
  )
  class SumType[A](s: Mirror.SumOf[A], instances: Array[FastShowPretty[Any]]) extends FastShowPretty[A] {

    def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder =
      instances(s.ordinal(value)).showPretty(value, sb, indent, nesting)
  }
}
