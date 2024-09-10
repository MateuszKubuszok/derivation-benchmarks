package example.showmacros.internal

object Runtime {

  def repeatAppend(sb: StringBuilder, str: String, times: Int): StringBuilder = {
    var i = 0
    while (i < times) {
      sb.append(str)
      i += 1
    }
    sb
  }

  private def dropLastButOne(sb: StringBuilder): StringBuilder =
    sb.deleteCharAt(sb.length() - 2)

  def appendCaseClassStart(sb: StringBuilder, className: String): Unit =
    sb.append(className).append("(\n")
  def appendCaseClassEnd(sb: StringBuilder, indent: String, nesting: Int): Unit = {
    dropLastButOne(sb) // removes last ',' (last-but-1 char, where length-1 is last char)
    repeatAppend(sb, indent, nesting).append(")")
  }

  def appendFieldStart(sb: StringBuilder, fieldName: String, indent: String, nesting: Int): Unit =
    repeatAppend(sb, indent, nesting).append(fieldName).append(" = ")
  def appendFieldEnd(sb: StringBuilder): Unit =
    sb.append(",\n")

  def appendCaseObject(sb: StringBuilder, className: String): Unit =
    sb.append(className)
}
