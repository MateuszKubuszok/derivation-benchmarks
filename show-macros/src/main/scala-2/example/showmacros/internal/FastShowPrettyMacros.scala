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

  def newDefCache[F[_]: DirectStyle]: DefCache[F] = new DefCache[F] {

    protected def define1[In1: Type, Out: Type](name: String): Define[Expr[In1], Expr[Out]] = {
      import ExprPromise.platformSpecific.freshTermName

      new Define[Expr[In1], Expr[Out]] {
        private val termName = freshTermName(name)
        private val call: Expr[In1] => Expr[Out] = in1 => c.Expr[Out](q"$termName($in1)")

        def apply(body: Expr[In1] => Expr[Out]): Def = new Def {
          private val defdef = DirectStyle[F].asyncUnsafe {
            val in1 = freshTermName(ShowType.simpleName[In1])
            q"""
            def $termName($in1: ${Type[In1]}): ${Type[Out]} = ${body(c.Expr[In1](q"$in1"))}
            """
          }
          def prependDef[A: Type](expr: Expr[A]): Expr[A] = c.Expr[A](
            q"""
            ${DirectStyle[F].awaitUnsafe(defdef)}
            $expr
            """
          )
          def cast[A]: A = { (in1: Expr[In1]) =>
            DirectStyle[F].awaitUnsafe(defdef) // re-fail
            call(in1)
          }.asInstanceOf[A]
        }
        val pending: PendingDef = new PendingDef {
          def cast[A]: A = call.asInstanceOf[A]
        }
      }
    }
    protected def define2[In1: Type, In2: Type, Out: Type](name: String): Define[(Expr[In1], Expr[In2]), Expr[Out]] = {
      import ExprPromise.platformSpecific.freshTermName

      new Define[(Expr[In1], Expr[In2]), Expr[Out]] {
        private val termName = freshTermName(name)
        private val call: (Expr[In1], Expr[In2]) => Expr[Out] = (in1, in2) => c.Expr[Out](q"$termName($in1, $in2)")

        def apply(body: ((Expr[In1], Expr[In2])) => Expr[Out]): Def = new Def {
          private val defdef = DirectStyle[F].asyncUnsafe {
            val in1 = freshTermName(ShowType.simpleName[In1])
            val in2 = freshTermName(ShowType.simpleName[In2])
            q"""
            def $termName($in1: ${Type[In1]}, $in2: ${Type[In2]}): ${Type[Out]} = ${body(
                (c.Expr[In1](q"$in1"), c.Expr[In2](q"$in2"))
              )}
            """
          }
          def prependDef[A: Type](expr: Expr[A]): Expr[A] = c.Expr[A](
            q"""
            ${DirectStyle[F].awaitUnsafe(defdef)}
            $expr
            """
          )
          def cast[A]: A = { (in1: Expr[In1], in2: Expr[In2]) =>
            DirectStyle[F].awaitUnsafe(defdef) // re-fail
            call(in1, in2)
          }.asInstanceOf[A]
        }
        val pending: PendingDef = new PendingDef {
          def cast[A]: A = call.asInstanceOf[A]
        }
      }
    }
  }

  // Macro's entrypoint
  def deriveFastShowPretty[A: Type]: Expr[FastShowPretty[A]] = ShowExpr.FastShowPretty.instance[A] {
    (value, sb, indent, nesting) =>
      deriveShowingExpression(ShowingContext.create[A](value, sb, indent, nesting))
  }
}
