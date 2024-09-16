package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.DefinitionsPlatform
import io.scalaland.chimney.internal.compiletime.datatypes.{ProductTypesPlatform, SealedHierarchiesPlatform}

import scala.quoted
import scala.quoted.*

class FastShowPrettyMacros(val q: Quotes)
    extends DefinitionsPlatform(using q),
      ProductTypesPlatform,
      SealedHierarchiesPlatform,
      Derivation {

  // Here we have to implement the platform-specific part

  import quotes.*, quotes.reflect.*

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

      def instance[A: Type](
          body: (Expr[A], Expr[StringBuilder], Expr[String], Expr[Int]) => Expr[StringBuilder]
      ): Expr[FastShowPretty[A]] = '{
        new FastShowPretty[A] {
          def showPretty(value: A, sb: StringBuilder, indent: String, nesting: Int): StringBuilder =
            ${ body('{ value }, '{ sb }, '{ indent }, '{ nesting }) }
        }
      }

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
        '{ Runtime.repeatAppend($sb, ${ value }, ${ times }) }

      def appendCaseClassStart(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit] =
        '{ Runtime.appendCaseClassStart(${ sb }, ${ className }) }
      def appendCaseClassEnd(sb: Expr[StringBuilder], indent: Expr[String], nesting: Expr[Int]): Expr[Unit] =
        '{ Runtime.appendCaseClassEnd(${ sb }, ${ indent }, ${ nesting }) }

      def appendFieldStart(
          sb: Expr[StringBuilder],
          fieldName: Expr[String],
          indent: Expr[String],
          nesting: Expr[Int]
      ): Expr[Unit] =
        '{ Runtime.appendFieldStart(${ sb }, ${ fieldName }, ${ indent }, ${ nesting }) }
      def appendFieldEnd(sb: Expr[StringBuilder]): Expr[Unit] =
        '{ Runtime.appendFieldEnd(${ sb }) }

      def appendCaseObject(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit] =
        '{ Runtime.appendCaseObject(${ sb }, ${ className }) }
    }

    def incInt(int: Expr[Int]): Expr[Int] = '{ ${ int } + 1 }

    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def arrayIsEmpty[A: Type](expr: Expr[Array[A]]): Expr[Boolean] =
      '{ ${ expr }.isEmpty }
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def listIsEmpty[A: Type](expr: Expr[List[A]]): Expr[Boolean] =
      '{ ${ expr }.isEmpty }
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit] =
      '{ ${ expr }.foreach(${ f }) }
    def vectorIsEmpty[A: Type](expr: Expr[Vector[A]]): Expr[Boolean] =
      '{ ${ expr }.isEmpty }

    def toString[A: Type](expr: Expr[A]): Expr[String] = '{ ${ expr }.toString }
    def void[A: Type](expr: Expr[A]): Expr[Unit] = '{ ${ expr }; () }
  }

  def newDefCache[F[_]: DirectStyle]: DefCache[F] = new DefCache[F] {
    import ExprPromise.platformSpecific.freshTerm

    protected def define1[In1: Type, Out: Type](name: String): Define[Expr[In1], Expr[Out]] =
      new Define[Expr[In1], Expr[Out]] {
        private val symbol = Symbol.newMethod(
          Symbol.spliceOwner,
          freshTerm(name),
          MethodType(List(freshTerm("in1")))(_ => List(TypeRepr.of[In1]), _ => TypeRepr.of[Out])
        )
        private val call: Expr[In1] => Expr[Out] = in1 =>
          Ref(symbol).appliedToArgss(List(List(in1.asTerm))).asExprOf[Out]

        def apply(body: Expr[In1] => Expr[Out]): Def = new Def {
          def prependDef[A: Type](expr: Expr[A]): Expr[A] = Block(
            List(
              DefDef(
                symbol,
                {
                  case List(List(in1: Term)) =>
                    Some(body(in1.asExprOf[In1]).asTerm.changeOwner(symbol))
                  case _ => None
                }
              )
            ),
            expr.asTerm
          ).changeOwner(Symbol.spliceOwner).asExprOf[A]
          def cast[A]: A = call.asInstanceOf[A]
        }
        val pending: PendingDef = new PendingDef {
          var isRecursive: Boolean = false
          def cast[A]: A = call.asInstanceOf[A]
        }
      }
    protected def define2[In1: Type, In2: Type, Out: Type](name: String): Define[(Expr[In1], Expr[In2]), Expr[Out]] =
      new Define[(Expr[In1], Expr[In2]), Expr[Out]] {
        private val symbol = Symbol.newMethod(
          Symbol.spliceOwner,
          freshTerm(name),
          MethodType(List(freshTerm("in1"), freshTerm("in2")))(
            _ => List(TypeRepr.of[In1], TypeRepr.of[In2]),
            _ => TypeRepr.of[Out]
          )
        )
        private val call: (Expr[In1], Expr[In2]) => Expr[Out] = (in1, in2) =>
          Ref(symbol).appliedToArgss(List(List(in1.asTerm, in2.asTerm))).asExprOf[Out]

        def apply(body: ((Expr[In1], Expr[In2])) => Expr[Out]): Def = new Def {
          def prependDef[A: Type](expr: Expr[A]): Expr[A] = Block(
            List(
              DefDef(
                symbol,
                {
                  case List(List(in1: Term, in2: Term)) =>
                    Some(body((in1.asExprOf[In1], in2.asExprOf[In2])).asTerm.changeOwner(symbol))
                  case _ => None
                }
              )
            ),
            expr.asTerm
          ).changeOwner(Symbol.spliceOwner).asExprOf[A]
          def cast[A]: A = call.asInstanceOf[A]
        }
        val pending: PendingDef = new PendingDef {
          def cast[A]: A = call.asInstanceOf[A]
        }
      }
  }

  // Macro's entrypoint
  def deriveFastShowPretty[A: Type]: Expr[FastShowPretty[A]] = ShowExpr.FastShowPretty.instance[A] {
    (value, sb, indent, nesting) =>
      deriveShowingExpression(ShowingContext.create[A](value, sb, indent, nesting))
  }
}
object FastShowPrettyMacros {

  // Scala 3's inline def can only unquote method of an object
  def deriveFastShowPretty[A: Type](using q: Quotes): Expr[FastShowPretty[A]] =
    new FastShowPrettyMacros(q).deriveFastShowPretty[A]
}
