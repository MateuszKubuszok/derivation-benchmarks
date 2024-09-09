package example

import example.model1.Out
import example.showmagnolia._
import example.showmagnolia.FastShowPrettyAuto._

object ShowMagnoliaAuto {
  
  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
