package example

import example.model1._
import FastShowPrettySemiauto._

object ShowGenericProgrammingSemi {

  private implicit val showIn1: FastShowPretty[In1] = deriveShow[In1]
  private implicit val showIn2: FastShowPretty[In2] = deriveShow[In2]
  private implicit val showIn3: FastShowPretty[In3] = deriveShow[In3]
  private implicit val showIn4: FastShowPretty[In4] = deriveShow[In4]
  private implicit val showIn5: FastShowPretty[In5] = deriveShow[In5]
  private implicit val showOut: FastShowPretty[Out] = deriveShow[Out]

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
