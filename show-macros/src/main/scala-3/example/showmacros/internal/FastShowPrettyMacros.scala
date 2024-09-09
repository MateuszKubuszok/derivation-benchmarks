package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.DefinitionsPlatform

import scala.quoted
import scala.quoted.*

class FastShowPrettyMacros(val q: Quotes) extends DefinitionsPlatform(using q), Derivation {

  // Here we have to implement the platform-specific part...

  import q.*, q.reflect.*

  object ShowType extends ShowTypeModule {

    object FastShowPretty extends FastShowPrettyModule {
      def apply[A: Type]: Type[FastShowPretty[A]] = quoted.Type.of[FastShowPretty[A]]
    }

    val StringBuilder: Type[StringBuilder] = quoted.Type.of[StringBuilder]
    val BigInt: Type[BigInt] = quoted.Type.of[BigInt]
    val BigDecimal: Type[BigDecimal] = quoted.Type.of[BigDecimal]

    object List extends Type.Ctor1[List] {
      def apply[A: Type]: Type[List[A]] = quoted.Type.of[List[A]]
      def unapply[A](A: Type[A]): Option[ExistentialType] = A match {
        case '[List[inner]] => scala.Some(Type[inner].as_??)
        case _              => scala.None
      }
    }
    object Vector extends Type.Ctor1[Vector] {
      def apply[A: Type]: Type[Vector[A]] = quoted.Type.of[Vector[A]]
      def unapply[A](A: Type[A]): Option[ExistentialType] = A match {
        case '[Vector[inner]] => scala.Some(Type[inner].as_??)
        case _                => scala.None
      }
    }
  }

  object ShowExpr extends ShowExprModule {

    object FastShowPretty extends FastShowPrettyModule {

      def showPretty[A: Type](
          instance: Expr[FastShowPretty[A]],
          value: Expr[A],
          sb: Expr[StringBuilder],
          indent: Expr[String],
          nesting: Expr[Int]
      ): Expr[StringBuilder] = '{ ${ instance }.showPretty(${ value }, ${ sb }, ${ indent }, ${ nesting }) }
    }
    object StringBuilder extends StringBuilderModule {

      def append(sb: Expr[StringBuilder], value: Expr[String]): Expr[StringBuilder] =
        '{ ${ sb }.append(${ value }) }

      def repeatAppend(sb: Expr[StringBuilder], value: Expr[String], times: Expr[Int]): Expr[StringBuilder] =
        '{
          var i = 0
          val j = ${ times }
          while i < j do {
            ${ sb }.append(${ value })
            i += 1
          }
          ${ sb }
        }
    }

    def incInt(int: Expr[Int]): Expr[Int] = '{ ${ int } + 1 }
    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def toString[A: Type](expr: Expr[A]): Expr[String] = '{ ${ expr }.toString }
  }

  // ...so that here we could use platform-agnostic code to do the heavy lifting :)

  def deriveFastShowPretty[A: Type]: Expr[FastShowPretty[A]] = '{
    new FastShowPretty[A] {
      def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
        ${ deriveShowingExpression[A](ShowingContext.create[A]('{ value }, '{ sb }, '{ indent }, '{ nesting })) }
    }
  }
}
object FastShowPrettyMacros {

  // Scala 3's inline def can only unquote method of an object
  def deriveFastShowPretty[A: Type](using q: Quotes): Expr[FastShowPretty[A]] =
    new FastShowPrettyMacros(q).deriveFastShowPretty[A]
}
