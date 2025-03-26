package example

import example.model1.*
import zio.json.*

object ZiojsonGenericSemi {

  implicit private val in1Decoder: JsonDecoder[In1] = DeriveJsonDecoder.gen
  implicit private val in1Encoder: JsonEncoder[In1] = DeriveJsonEncoder.gen
  implicit private val in2Decoder: JsonDecoder[In2] = DeriveJsonDecoder.gen
  implicit private val in2Encoder: JsonEncoder[In2] = DeriveJsonEncoder.gen
  implicit private val in3Decoder: JsonDecoder[In3] = DeriveJsonDecoder.gen
  implicit private val in3Encoder: JsonEncoder[In3] = DeriveJsonEncoder.gen
  implicit private val in4Decoder: JsonDecoder[In4] = DeriveJsonDecoder.gen
  implicit private val in4Encoder: JsonEncoder[In4] = DeriveJsonEncoder.gen
  implicit private val in5Decoder: JsonDecoder[In5] = DeriveJsonDecoder.gen
  implicit private val in5Encoder: JsonEncoder[In5] = DeriveJsonEncoder.gen
  implicit private val outDecoder: JsonDecoder[Out] = DeriveJsonDecoder.gen
  implicit private val outEncoder: JsonEncoder[Out] = DeriveJsonEncoder.gen

  def roundTrip(out: Out): (String, Either[String, Out]) = {
    val json = out.toJson
    val parsed = json.fromJson[Out]
    json -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
