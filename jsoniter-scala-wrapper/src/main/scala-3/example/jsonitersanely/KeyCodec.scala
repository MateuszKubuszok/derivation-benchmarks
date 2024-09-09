package example.jsonitersanely

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonReader, JsonWriter}

final class KeyCodec[A](val codec: JsonKeyCodec[A]) extends AnyVal {
  import KeyCodec.*

  def map[B](to: A => B)(from: B => A): KeyCodec[B] = fromJsoniter(new {
    def decodeKey(in: JsonReader): B = to(codec.decodeKey(in))
    def encodeKey(x: B, out: JsonWriter): Unit = codec.encodeKey(from(x), out)
  })
}
object KeyCodec {
  def fromJsoniter[A](codec: JsonKeyCodec[A]): KeyCodec[A] = KeyCodec(codec)

  given KeyCodec[Boolean] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Boolean = in.readKeyAsBoolean()
    def encodeKey(x: Boolean, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Byte] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Byte = in.readKeyAsByte()
    def encodeKey(x: Byte, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Short] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Short = in.readKeyAsShort()
    def encodeKey(x: Short, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Int] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Int = in.readKeyAsInt()
    def encodeKey(x: Int, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Long] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Long = in.readKeyAsLong()
    def encodeKey(x: Long, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Float] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Float = in.readKeyAsFloat()
    def encodeKey(x: Float, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Double] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Double = in.readKeyAsDouble()
    def encodeKey(x: Double, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[BigInt] = fromJsoniter(new {
    def decodeKey(in: JsonReader): BigInt = in.readKeyAsBigInt()
    def encodeKey(x: BigInt, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[BigDecimal] = fromJsoniter(new {
    def decodeKey(in: JsonReader): BigDecimal = in.readKeyAsBigDecimal()
    def encodeKey(x: BigDecimal, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[Char] = fromJsoniter(new {
    def decodeKey(in: JsonReader): Char = in.readKeyAsChar()
    def encodeKey(x: Char, out: JsonWriter): Unit = out.writeKey(x)
  })
  given KeyCodec[String] = fromJsoniter(new {
    def decodeKey(in: JsonReader): String = in.readKeyAsString()
    def encodeKey(x: String, out: JsonWriter): Unit = out.writeKey(x)
  })
}
