package example

import example.model1._
import example.showgeneric._
import example.showgeneric.FastShowPrettySemiauto

object ShowGenericProgrammingSemi {

  implicit private val showIn1: FastShowPretty[In1] = FastShowPrettySemiauto.deriveShow[In1]
  implicit private val showIn2: FastShowPretty[In2] = FastShowPrettySemiauto.deriveShow[In2]
  implicit private val showIn3: FastShowPretty[In3] = FastShowPrettySemiauto.deriveShow[In3]
  implicit private val showIn4: FastShowPretty[In4] = FastShowPrettySemiauto.deriveShow[In4]
  implicit private val showIn5: FastShowPretty[In5] = FastShowPrettySemiauto.deriveShow[In5]
  implicit private val showOut: FastShowPretty[Out] = FastShowPrettySemiauto.deriveShow[Out]

  def printObject(out: Out): String = out.showPretty()

  def main(args: Array[String]): Unit = {
    val output = printObject(Out.example)
    println(output)
  }
}
