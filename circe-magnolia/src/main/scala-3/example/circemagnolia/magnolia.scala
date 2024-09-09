package example.circemagnolia

import cats.syntax.either.*
import io.circe.{Decoder, DecodingFailure, Encoder, HCursor, Json}
import io.circe.Decoder.Result
import magnolia1.*

import scala.deriving.Mirror

// based on https://github.com/vpavkin/circe-magnolia/

private[circemagnolia] trait MagnoliaDecoder extends Derivation[Decoder] {

  def join[A](caseClass: CaseClass[Decoder, A]): Decoder[A] =
    (c: HCursor) =>
      caseClass
        .constructEither { p =>
          p.typeclass.tryDecode(c.downField(p.label))
        }
        .leftMap(_.head)

  def split[A](sealedTrait: SealedTrait[Decoder, A]): Decoder[A] =
    (c: HCursor) => {
      val constructorLookup = sealedTrait.subtypes.map { s =>
        s.typeInfo.short -> s
      }.toMap
      c.keys match {
        case Some(keys) if keys.size == 1 =>
          val key = keys.head
          for {
            theSubtype <- Either.fromOption(
              constructorLookup.get(key),
              DecodingFailure(
                s"""Can't decode coproduct type: couldn't find matching subtype.
                   |JSON: ${c.value},
                   |Key: $key
                   |Known subtypes: ${constructorLookup.keys.toSeq.sorted.mkString(",")}\n""".stripMargin,
                c.history
              )
            )
            result <- c.get(key)(theSubtype.typeclass)
          } yield result
        case _ =>
          Left(
            DecodingFailure(
              s"""Can't decode coproduct type: zero or several keys were found, while coproduct type requires exactly one.
                 |JSON: ${c.value},
                 |Keys: ${c.keys.map(_.mkString(","))}
                 |Known subtypes: ${constructorLookup.keys.toSeq.sorted.mkString(",")}\n""".stripMargin,
              c.history
            )
          )
      }
    }
}

object DecoderSemi extends MagnoliaDecoder

object DecoderAuto extends MagnoliaDecoder {

  implicit inline def deriveDecoder[A](implicit m: Mirror.Of[A]): Decoder[A] = derived
}

private[circemagnolia] trait MagnoliaEncoder extends Derivation[Encoder] {

  def join[A](caseClass: CaseClass[Encoder, A]): Encoder[A] =
    (a: A) =>
      Json.obj(caseClass.params.map { p =>
        p.label -> p.typeclass(p.deref(a))
      }*)

  def split[A](sealedTrait: SealedTrait[Encoder, A]): Encoder[A] = (a: A) =>
    sealedTrait.choose(a) { subtype =>
      Json.obj(subtype.typeInfo.short -> subtype.typeclass(subtype.cast(a)))
    }
}

object EncoderSemi extends MagnoliaEncoder

object EncoderAuto extends MagnoliaEncoder {

  implicit inline def deriveEncoder[A](implicit m: Mirror.Of[A]): Encoder[A] = derived
}
