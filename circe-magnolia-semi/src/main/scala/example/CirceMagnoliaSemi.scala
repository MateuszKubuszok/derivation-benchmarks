package example

import example.model1._
import example.circemagnolia.{DecoderSemi, EncoderSemi}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

object CirceMagnoliaSemi {

  implicit private val in1Decoder: Decoder[In1] = DecoderSemi.derived
  implicit private val in1Encoder: Encoder[In1] = EncoderSemi.derived
  implicit private val in2Decoder: Decoder[In2] = DecoderSemi.derived
  implicit private val in2Encoder: Encoder[In2] = EncoderSemi.derived
  implicit private val in3Decoder: Decoder[In3] = DecoderSemi.derived
  implicit private val in3Encoder: Encoder[In3] = EncoderSemi.derived
  implicit private val in4Decoder: Decoder[In4] = DecoderSemi.derived
  implicit private val in4Encoder: Encoder[In4] = EncoderSemi.derived
  implicit private val in5Decoder: Decoder[In5] = DecoderSemi.derived
  implicit private val in5Encoder: Encoder[In5] = EncoderSemi.derived
  implicit private val outDecoder: Decoder[Out] = DecoderSemi.derived
  implicit private val outEncoder: Encoder[Out] = EncoderSemi.derived

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
