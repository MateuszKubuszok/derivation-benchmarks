package example

import example.model1.Out
import io.circe.{Error, Printer}
import io.circe.generic.auto.*
import io.circe.jawn.*
import io.circe.syntax.*

object CirceGenericAuto {
  def roundTrip(out: Out): (String, Either[Error, Out]) = {
    val json = Printer.noSpaces.print(out.asJson)
    val parsed = decodeCharSequence[Out](json)
    json -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
