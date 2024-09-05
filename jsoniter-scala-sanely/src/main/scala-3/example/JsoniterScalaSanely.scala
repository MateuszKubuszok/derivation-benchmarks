package example

import example.model1._

object JsoniterScalaSanely {

  def roundTrip(out: Out): (String, Either[Throwable, Out]) = {
    val str = write(out)
    val parsed = scala.util.Try(read[Out](str)).toEither
    str -> parsed
  }

  def main(args: Array[String]): Unit = {
    val (json, parsed) = roundTrip(Out.example)
    println(json)
    println(parsed)
  }
}
