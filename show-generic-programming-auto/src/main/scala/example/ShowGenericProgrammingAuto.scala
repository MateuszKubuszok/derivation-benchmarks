package example

import example.model1.Out
import example.showgeneric._
import example.showgeneric.FastShowPrettyAuto._

object ShowGenericProgrammingAuto {

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
