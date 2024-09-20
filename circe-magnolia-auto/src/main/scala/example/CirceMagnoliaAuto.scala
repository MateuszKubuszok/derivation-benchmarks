package example

import example.circemagnolia.DecoderAuto.*
import example.circemagnolia.EncoderAuto.*
import example.model1.Out
import io.circe.Decoder.Result
import io.circe.jawn.*
import io.circe.{Error, Json, Printer}
import io.circe.syntax.*

object EncodeHelper {
  def encode(out: Out): String = Printer.noSpaces.print(out.asJson)
}

object DecoderHelper {
  def decode(json: String): Either[Error, Out] = decodeCharSequence[Out](json)
}

object CirceMagnoliaAuto {
  def roundTrip(out: Out): (String, Either[Error, Out]) = {
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
