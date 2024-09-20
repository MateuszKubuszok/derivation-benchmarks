package example

import example.model1.*
import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*

object JsoniterScalaAuto {
  implicit private val outCodec: JsonValueCodec[Out] = JsonCodecMaker.make

  def roundTrip(out: Out): (String, Either[Throwable, Out]) = {
    val str = writeToString(out)
    val parsed = scala.util.Try(readFromString(str)).toEither
    str -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
