package example

import example.model1._
import example.showgeneric._
import example.showgeneric.FastShowPrettySemiauto

object ShowGenericProgrammingSemi {

  private implicit val showIn1: FastShowPretty[In1] = FastShowPrettySemiauto.deriveShow[In1]
  private implicit val showIn2: FastShowPretty[In2] = FastShowPrettySemiauto.deriveShow[In2]
  private implicit val showIn3: FastShowPretty[In3] = FastShowPrettySemiauto.deriveShow[In3]
  private implicit val showIn4: FastShowPretty[In4] = FastShowPrettySemiauto.deriveShow[In4]
  private implicit val showIn5: FastShowPretty[In5] = FastShowPrettySemiauto.deriveShow[In5]
  private implicit val showOut: FastShowPretty[Out] = FastShowPrettySemiauto.deriveShow[Out]

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
