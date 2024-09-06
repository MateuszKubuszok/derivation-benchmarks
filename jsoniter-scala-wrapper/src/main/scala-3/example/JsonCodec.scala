package example

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonKeyCodec, JsonValueCodec}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

final class JsonCodec[A](val codec: JsonValueCodec[A]) extends JsonCodec.AutoDerived[A]

object JsonCodec {
  def fromJsoniter[A](codec: JsonValueCodec[A]): JsonCodec[A] = JsonCodec(codec)

  inline def derived[A](using
      inline config: Config = Config.withAllowRecursiveTypes(true)
  ): JsonCodec[A] = {
    given [A: KeyCodec]: JsonKeyCodec[A] = summon[KeyCodec[A]].codec
    given [A: JsonCodec]: JsonValueCodec[A] = summon[JsonCodec[A]].codec
    fromJsoniter(JsonCodecMaker.make[A](config))
  }

  type Config = com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig
  inline def Config = com.github.plokhotnyuk.jsoniter_scala.macros.CodecMakerConfig

  sealed trait AutoDerived[A] {
    val codec: JsonValueCodec[A]
  }
  object AutoDerived extends LowPriorityAutoDerived {
    inline def derived[A](using
        inline config: Config = Config.withAllowRecursiveTypes(true)
    ): JsonCodec.AutoDerived[A] = {
      given [A: KeyCodec]: JsonKeyCodec[A] = summon[KeyCodec[A]].codec
      given [A: JsonCodec.AutoDerived]: JsonValueCodec[A] = summon[JsonCodec.AutoDerived[A]].codec
      fromJsoniter(JsonCodecMaker.make[A](config))
    }
  }
  trait LowPriorityAutoDerived {
    inline given autoDerived[A](using
        inline config: Config = Config.withAllowRecursiveTypes(true)
    ): JsonCodec.AutoDerived[A] = JsonCodec.derived
  }
}
