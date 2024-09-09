package example

import example.model1._
import example.showmagnolia._
import example.showmagnolia.FastShowPrettySemiauto

object ShowMagnoliaSemi {
  
  private implicit val showIn1: FastShowPretty[In1] = FastShowPrettySemiauto.derived[In1]
  private implicit val showIn2: FastShowPretty[In2] = FastShowPrettySemiauto.derived[In2]
  private implicit val showIn3: FastShowPretty[In3] = FastShowPrettySemiauto.derived[In3]
  private implicit val showIn4: FastShowPretty[In4] = FastShowPrettySemiauto.derived[In4]
  private implicit val showIn5: FastShowPretty[In5] = FastShowPrettySemiauto.derived[In5]
  private implicit val showOut: FastShowPretty[Out] = FastShowPrettySemiauto.derived[Out]

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
