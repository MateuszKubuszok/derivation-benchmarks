package example

import example.model1.Out
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._

object CirceGenericAuto {

  def roundTrip(out: Out): (Json, Result[Out]) = {
    val json = out.asJson
    val parsed = json.as[Out]
    json -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
