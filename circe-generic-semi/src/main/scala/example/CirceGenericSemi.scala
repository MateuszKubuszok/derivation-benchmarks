package example

import example.model1._
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto._
import io.circe.syntax._

object CirceGenericSemi {

  implicit private val in1Decoder: Decoder[In1] = deriveDecoder
  implicit private val in1Encoder: Encoder[In1] = deriveEncoder
  implicit private val in2Decoder: Decoder[In2] = deriveDecoder
  implicit private val in2Encoder: Encoder[In2] = deriveEncoder
  implicit private val in3Decoder: Decoder[In3] = deriveDecoder
  implicit private val in3Encoder: Encoder[In3] = deriveEncoder
  implicit private val in4Decoder: Decoder[In4] = deriveDecoder
  implicit private val in4Encoder: Encoder[In4] = deriveEncoder
  implicit private val in5Decoder: Decoder[In5] = deriveDecoder
  implicit private val in5Encoder: Encoder[In5] = deriveEncoder
  implicit private val outDecoder: Decoder[Out] = deriveDecoder
  implicit private val outEncoder: Encoder[Out] = deriveEncoder

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
