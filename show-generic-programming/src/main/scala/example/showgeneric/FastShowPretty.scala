package example.showgeneric

trait FastShowPretty[A] {

  def showPretty(value: A, sb: StringBuilder, indent: String = "  ", nesting: Int = 0): StringBuilder
}
object FastShowPretty {

  def primitive[A](f: A => String): FastShowPretty[A] =
    (value: A, sb: StringBuilder, _: String, _: Int) => sb.append(f(value))

  def collection[A: FastShowPretty, CC](
      name: String,
      isEmpty: CC => Boolean,
      foreach: CC => (A => Unit) => Unit
  ): FastShowPretty[CC] =
    (value: CC, sb: StringBuilder, indent: String, nesting: Int) => {
      sb.append(name).append("(")
      if (isEmpty(value)) {
        sb.append(")")
      } else {
        sb.append("\n")
        foreach(value) { (a: A) =>
          repeatAppend(sb, indent, nesting + 1)
          implicitly[FastShowPretty[A]].showPretty(a, sb, indent, nesting + 1).append(",\n")
          ()
        }
        sb.deleteCharAt(sb.length() - 2) // removes last ',' (last-but-1 char, where length-1 is last char)
        repeatAppend(sb, indent, nesting).append(")")
      }
    }

  def repeatAppend(sb: StringBuilder, str: String, times: Int): StringBuilder = {
    var i = 0
    while (i < times) {
      sb.append(str)
      i += 1
    }
    sb
  }

  implicit val showString: FastShowPretty[String] =
    (value: String, sb: StringBuilder, _: String, _: Int) => sb.append('"').append(value).append('"')
  implicit val showChar: FastShowPretty[Char] =
    (value: Char, sb: StringBuilder, _: String, _: Int) => sb.append("'").append(value).append("'")

  implicit val showBoolean: FastShowPretty[Boolean] = primitive(_.toString())
  implicit val showByte: FastShowPretty[Boolean] = primitive(_.toString() + ".toByte")

  implicit val showShort: FastShowPretty[Short] = primitive(_.toString + ".toShort")
  implicit val showInt: FastShowPretty[Int] = primitive(_.toString)
  implicit val showLong: FastShowPretty[Long] = primitive(_.toString + "L")

  implicit val showFloat: FastShowPretty[Float] = primitive(_.toString + "f")
  implicit val showDouble: FastShowPretty[Double] = primitive(_.toString)

  implicit val showBigInt: FastShowPretty[BigInt] = primitive(_.toString)
  implicit val showBigDecimal: FastShowPretty[BigDecimal] = primitive(_.toString)

  implicit val showUnit: FastShowPretty[Unit] = primitive(_ => "()")

  implicit def showArray[A: FastShowPretty]: FastShowPretty[Array[A]] =
    collection[A, Array[A]]("Array", _.isEmpty, cc => cc.foreach(_: A => Unit))
  implicit def showList[A: FastShowPretty]: FastShowPretty[List[A]] =
    collection[A, List[A]]("List", _.isEmpty, cc => cc.foreach(_: A => Unit))
  implicit def showVector[A: FastShowPretty]: FastShowPretty[Vector[A]] =
    collection[A, Vector[A]]("Vector", _.isEmpty, cc => cc.foreach[Unit](_: A => Unit))
}
