package example

import com.github.plokhotnyuk.jsoniter_scala.core

def read[A: JsonCodec.AutoDerived](str: String): A =
  core.readFromString[A](str)(using summon[JsonCodec.AutoDerived[A]].codec)

def write[A: JsonCodec.AutoDerived](a: A): String =
  core.writeToString(a)(using summon[JsonCodec.AutoDerived[A]].codec)
