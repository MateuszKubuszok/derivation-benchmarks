package example

import example.model1.Out
import FastShowPrettyAuto._

object ShowGenericProgrammingAuto {

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
