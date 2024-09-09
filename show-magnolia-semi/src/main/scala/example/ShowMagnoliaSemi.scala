package example

import example.model1._
import example.showmagnolia._
import example.showmagnolia.FastShowPrettySemiauto

object ShowMagnoliaSemi {

  implicit private val showIn1: FastShowPretty[In1] = FastShowPrettySemiauto.derived[In1]
  implicit private val showIn2: FastShowPretty[In2] = FastShowPrettySemiauto.derived[In2]
  implicit private val showIn3: FastShowPretty[In3] = FastShowPrettySemiauto.derived[In3]
  implicit private val showIn4: FastShowPretty[In4] = FastShowPrettySemiauto.derived[In4]
  implicit private val showIn5: FastShowPretty[In5] = FastShowPrettySemiauto.derived[In5]
  implicit private val showOut: FastShowPretty[Out] = FastShowPrettySemiauto.derived[Out]

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
