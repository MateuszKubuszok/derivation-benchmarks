package example

import example.model1.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

object JsoniterScalaSemi {
  implicit private val in1Codec: JsonValueCodec[In1] = JsonCodecMaker.make
  implicit private val in2Codec: JsonValueCodec[In2] = JsonCodecMaker.make
  implicit private val in3Codec: JsonValueCodec[In3] = JsonCodecMaker.make
  implicit private val in4Codec: JsonValueCodec[In4] = JsonCodecMaker.make
  implicit private val in5Codec: JsonValueCodec[In5] = JsonCodecMaker.make
  implicit private val outCodec: JsonValueCodec[Out] = JsonCodecMaker.make

  def roundTrip(out: Out): (String, Either[Throwable, Out]) = {
    val str = writeToString(out)
    val parsed = scala.util.Try(readFromString[Out](str)).toEither
    str -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
