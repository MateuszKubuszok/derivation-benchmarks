package example

import example.model1._
import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._

object JsoniterScalaSemi {

  implicit private val outCodec: JsonValueCodec[Out] =
    JsonCodecMaker.make(CodecMakerConfig.withAllowRecursiveTypes(true))

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
