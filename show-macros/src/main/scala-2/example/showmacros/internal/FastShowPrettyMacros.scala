package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.DefinitionsPlatform
import io.scalaland.chimney.internal.compiletime.datatypes.{ProductTypesPlatform, SealedHierarchiesPlatform}

import scala.reflect.macros.blackbox

class FastShowPrettyMacros(val c: blackbox.Context)
    extends DefinitionsPlatform
    with ProductTypesPlatform
    with SealedHierarchiesPlatform
    with Derivation {

  // Here we have to implement the platform-specific part

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

      def instance[A: Type](
          body: (Expr[A], Expr[StringBuilder], Expr[String], Expr[Int]) => Expr[StringBuilder]
      ): Expr[FastShowPretty[A]] = {
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
            ): ${Type[StringBuilder]} = ${body(
              c.Expr[A](q"$value"),
              c.Expr[StringBuilder](q"$sb"),
              c.Expr[String](q"$indent"),
              c.Expr[Int](q"$nesting")
            )}
          }
          """
        )
      }

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
        c.Expr[StringBuilder](q"_root_.example.showmacros.internal.Runtime.repeatAppend($sb, $value, $times)")

      def appendCaseClassStart(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit] =
        c.Expr[Unit](q"_root_.example.showmacros.internal.Runtime.appendCaseClassStart($sb, $className)")
      def appendCaseClassEnd(sb: Expr[StringBuilder], indent: Expr[String], nesting: Expr[Int]): Expr[Unit] =
        c.Expr[Unit](q"_root_.example.showmacros.internal.Runtime.appendCaseClassEnd($sb, $indent, $nesting)")

      def appendFieldStart(
          sb: Expr[StringBuilder],
          fieldName: Expr[String],
          indent: Expr[String],
          nesting: Expr[Int]
      ): Expr[Unit] =
        c.Expr[Unit](q"_root_.example.showmacros.internal.Runtime.appendFieldStart($sb, $fieldName, $indent, $nesting)")
      def appendFieldEnd(sb: Expr[StringBuilder]): Expr[Unit] =
        c.Expr[Unit](q"_root_.example.showmacros.internal.Runtime.appendFieldEnd($sb)")

      def appendCaseObject(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit] =
        c.Expr[Unit](q"_root_.example.showmacros.internal.Runtime.appendCaseObject($sb, $className)")
    }

    def incInt(int: Expr[Int]): Expr[Int] = c.Expr(q"$int + 1")

    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def arrayIsEmpty[A: Type](expr: Expr[Array[A]]): Expr[Boolean] =
      c.Expr[Boolean](q"$expr.isEmpty")
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def listIsEmpty[A: Type](expr: Expr[List[A]]): Expr[Boolean] =
      c.Expr[Boolean](q"$expr.isEmpty")
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit] =
      c.Expr[Unit](q"$expr.foreach($f)")
    def vectorIsEmpty[A: Type](expr: Expr[Vector[A]]): Expr[Boolean] =
      c.Expr[Boolean](q"$expr.isEmpty")

    def toString[A: Type](expr: Expr[A]): Expr[String] = c.Expr[String](q"$expr.toString")
    def void[A: Type](expr: Expr[A]): Expr[Unit] = c.Expr[Unit](q"$expr")
  }

  // Macro's entrypoint
  def deriveFastShowPretty[A: Type]: Expr[FastShowPretty[A]] = ShowExpr.FastShowPretty.instance[A] {
    (value, sb, indent, nesting) =>
      deriveShowingExpression(ShowingContext.create[A](value, sb, indent, nesting))
  }
}
