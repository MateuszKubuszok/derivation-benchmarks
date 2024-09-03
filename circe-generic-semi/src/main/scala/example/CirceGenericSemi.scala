package example

import example.model1._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._
import io.circe.syntax._

object CirceGenericSemi {

  private implicit val in1Decoder: Decoder[In1] = deriveDecoder
  private implicit val in1Encoder: Encoder[In1] = deriveEncoder
  private implicit val in2Decoder: Decoder[In2] = deriveDecoder
  private implicit val in2Encoder: Encoder[In2] = deriveEncoder
  private implicit val in3Decoder: Decoder[In3] = deriveDecoder
  private implicit val in3Encoder: Encoder[In3] = deriveEncoder
  private implicit val in4Decoder: Decoder[In4] = deriveDecoder
  private implicit val in4Encoder: Encoder[In4] = deriveEncoder
  private implicit val in5Decoder: Decoder[In5] = deriveDecoder
  private implicit val in5Encoder: Encoder[In5] = deriveEncoder
  private implicit val outDecoder: Decoder[Out] = deriveDecoder
  private implicit val outEncoder: Encoder[Out] = deriveEncoder

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
