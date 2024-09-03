package example

import example.DecoderAuto._
import example.EncoderAuto._
import example.model1.Out
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax._

object CirceMagnoliaAuto {

  def roundTrip(out: Out): (Json, Result[Out]) = {
    val json = out.asJson//(EncoderAuto.derived)
    val parsed = json.as[Out]//(DecoderAuto.derived)
    json -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
