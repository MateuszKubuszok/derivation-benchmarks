package example

import example.model1._
import example.{DecoderSemi, EncoderSemi}
import io.circe.Decoder.Result
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

object CirceMagnoliaSemi {

  private implicit val in1Decoder: Decoder[In1] = DecoderSemi.derived
  private implicit val in1Encoder: Encoder[In1] = EncoderSemi.derived
  private implicit val in2Decoder: Decoder[In2] = DecoderSemi.derived
  private implicit val in2Encoder: Encoder[In2] = EncoderSemi.derived
  private implicit val in3Decoder: Decoder[In3] = DecoderSemi.derived
  private implicit val in3Encoder: Encoder[In3] = EncoderSemi.derived
  private implicit val in4Decoder: Decoder[In4] = DecoderSemi.derived
  private implicit val in4Encoder: Encoder[In4] = EncoderSemi.derived
  private implicit val in5Decoder: Decoder[In5] = DecoderSemi.derived
  private implicit val in5Encoder: Encoder[In5] = EncoderSemi.derived
  private implicit val outDecoder: Decoder[Out] = DecoderSemi.derived
  private implicit val outEncoder: Encoder[Out] = EncoderSemi.derived

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
