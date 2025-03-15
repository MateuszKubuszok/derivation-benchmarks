package example

import example.model1.*
import example.showmacros3_7.*

object ShowSanely {

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
