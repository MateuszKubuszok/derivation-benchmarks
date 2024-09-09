package example

import example.circemagnolia.DecoderAuto._
import example.circemagnolia.EncoderAuto._
import example.model1.Out
import io.circe.Decoder.Result
import io.circe.Json
import io.circe.syntax._

object EncodeHelper {

  def encode(out: Out): Json = out.asJson
}

object DecoderHelper {

  def decode(json: Json): Result[Out] = json.as[Out]
}

object CirceMagnoliaAuto {

  def roundTrip(out: Out): (Json, Result[Out]) = {
    // workaround for:
    // [error] Error while emitting example/CirceMagnoliaAuto$
    // [error] Class too large: example/CirceMagnoliaAuto$
    // [error] one error found
    // on Scala 3 xD
    val json = EncodeHelper.encode(out)
    val parsed = DecoderHelper.decode(json)
    json -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
