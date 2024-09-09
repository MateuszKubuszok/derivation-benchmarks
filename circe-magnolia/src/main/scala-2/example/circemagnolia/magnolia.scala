package example.circemagnolia

import cats.syntax.either._
import io.circe._
import magnolia1._

import scala.language.experimental.macros

// based on https://github.com/vpavkin/circe-magnolia/

private[circemagnolia] trait MagnoliaDecoder {

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
        s.typeName.short -> s
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

object DecoderSemi extends MagnoliaDecoder {

  type Typeclass[A] = Decoder[A]

  def derived[A]: Typeclass[A] = macro Magnolia.gen[A]
}

object DecoderAuto extends MagnoliaDecoder {

  type Typeclass[A] = Decoder[A]

  implicit def derivedDecoder[A]: Typeclass[A] = macro Magnolia.gen[A]
}

private[circemagnolia] trait MagnoliaEncoder {

  def join[A](caseClass: CaseClass[Encoder, A]): Encoder[A] =
    (a: A) =>
      Json.obj(caseClass.parameters.map { p =>
        p.label -> p.typeclass(p.dereference(a))
      }: _*)

  def split[A](sealedTrait: SealedTrait[Encoder, A]): Encoder[A] = (a: A) =>
    sealedTrait.split(a) { subtype =>
      Json.obj(subtype.typeName.short -> subtype.typeclass(subtype.cast(a)))
    }
}

object EncoderSemi extends MagnoliaEncoder {

  type Typeclass[A] = Encoder[A]

  def derived[A]: Typeclass[A] = macro Magnolia.gen[A]
}

object EncoderAuto extends MagnoliaEncoder {

  type Typeclass[A] = Encoder[A]

  implicit def derivedEncoder[A]: Typeclass[A] = macro Magnolia.gen[A]
}
