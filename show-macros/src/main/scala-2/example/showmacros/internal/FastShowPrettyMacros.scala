package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.DefinitionsPlatform

import scala.reflect.macros.blackbox

class FastShowPrettyMacros(val c: blackbox.Context) extends DefinitionsPlatform with Derivation {

  // Here we have to implement the platform-specific part...

  import c.universe.*, Type.platformSpecific.TypeCtorOps

  object ShowType extends ShowTypeModule {

    object FastShowPretty extends FastShowPrettyModule {
      def apply[A: Type]: Type[FastShowPretty[A]] = weakTypeTag[FastShowPretty[A]]
    }

    val StringBuilder: Type[StringBuilder] = weakTypeTag[StringBuilder]
    val BigInt: Type[BigInt] = weakTypeTag[BigInt]
    val BigDecimal: Type[BigDecimal] = weakTypeTag[BigDecimal]

    object List extends Type.Ctor1[List] {
      def apply[A: Type]: Type[List[A]] = weakTypeTag[List[A]]
      def unapply[A](A: Type[A]): Option[ExistentialType] = A.asCtor[List[?]].map(A0 => A0.param(0))
    }
    object Vector extends Type.Ctor1[Vector] {
      def apply[A: Type]: Type[Vector[A]] = weakTypeTag[Vector[A]]
      def unapply[A](A: Type[A]): Option[ExistentialType] = A.asCtor[Vector[?]].map(A0 => A0.param(0))
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
      ): Expr[StringBuilder] = c.Expr[StringBuilder](q"$instance.showPretty($value, $sb, $indent, $nesting)")
    }
    object StringBuilder extends StringBuilderModule {

      def append(sb: Expr[StringBuilder], value: Expr[String]): Expr[StringBuilder] =
        c.Expr[StringBuilder](q"$sb.append($value)")

      def repeatAppend(sb: Expr[StringBuilder], value: Expr[String], times: Expr[Int]): Expr[StringBuilder] =
        c.Expr[StringBuilder](
          q"""
          var i = 0
          val j = $times 
          while (i < j) {
            $sb.append($value)
            i += 1
          }
          $sb
          """
        )
    }

    def incInt(int: Expr[Int]): Expr[Int] = c.Expr(q"$int + 1")
    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def toString[A: Type](expr: Expr[A]): Expr[String] = c.Expr[String](q"$expr.toString")
  }

  // ...so that here we could use platform-agnostic code to do the heavy lifting :)

  def deriveFastShowPretty[A: c.WeakTypeTag]: c.Expr[FastShowPretty[A]] = {
    val value = ExprPromise.platformSpecific.freshTermName("value")
    val sb = ExprPromise.platformSpecific.freshTermName("sb")
    val indent = ExprPromise.platformSpecific.freshTermName("indent")
    val nesting = ExprPromise.platformSpecific.freshTermName("nesting")
    c.Expr[FastShowPretty[A]](
      q"""
      new ${Type[FastShowPretty[A]]} {

        def showPretty(
          $value: ${Type[A]},
          $sb: ${Type[StringBuilder]},
          $indent: ${Type[String]},
          $nesting: ${Type[Int]}
        ): ${Type[StringBuilder]} = ${deriveShowingExpression(
          ShowingContext.create[A](
            c.Expr[A](q"$value"),
            c.Expr[StringBuilder](q"$sb"),
            c.Expr[String](q"$indent"),
            c.Expr[Int](q"$nesting")
          )
        )}
      }
      """
    )
  }
}
