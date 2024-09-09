package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.Definitions
import io.scalaland.chimney.internal.compiletime.fp.{Applicative, Parallel, Traverse}
import io.scalaland.chimney.internal.compiletime.fp.Implicits.*

import scala.collection.mutable
import scala.util.chaining.*

trait Derivation extends Definitions {

  // Here we are using utilities from chimney-macro-commons instead of Scala 2/Scala 3 macros directly.
  // We'll use "Show" prefix because we are working on Show library, and we don't want to mix these definitions
  // with definitions coming from chimney-macro-commons.

  // Let's define the types used specifically by this library:

  val ShowType: ShowTypeModule
  trait ShowTypeModule { this: ShowType.type =>

    // Nice, extensible way working with some parametric type
    val FastShowPretty: FastShowPrettyModule
    trait FastShowPrettyModule { this: FastShowPretty.type =>
      def apply[A: Type]: Type[FastShowPretty[A]]
    }

    // Proper types need only a val(ue)
    val StringBuilder: Type[StringBuilder]
    val BigInt: Type[BigInt]
    val BigDecimal: Type[BigDecimal]

    // Similar to but FastShowPrettyModule, but with unapply
    val List: Type.Ctor1[List]
    val Vector: Type.Ctor1[Vector]

    // When needed impoty ShowType.Implicits._
    // Not defined in Derivation directly because it would be easy to get circular dependencies in the implementation.
    object Implicits {

      implicit def fastShowPrettyType[A: Type]: Type[FastShowPretty[A]] = FastShowPretty[A]

      implicit def stringBuilderType: Type[StringBuilder] = StringBuilder

      implicit def bigIntType: Type[BigInt] = BigInt
      implicit def bugDecimalType: Type[BigDecimal] = BigDecimal

      implicit def listType[A: Type]: Type[List[A]] = List[A]
      implicit def vectorType[A: Type]: Type[Vector[A]] = Vector[A]
    }
  }

  val ShowExpr: ShowExprModule
  trait ShowExprModule { this: ShowExpr.type =>

    // We can group Expr utilities together by type...

    val FastShowPretty: FastShowPrettyModule
    trait FastShowPrettyModule { this: FastShowPretty.type =>

      def showPretty[A: Type](
          instance: Expr[FastShowPretty[A]],
          value: Expr[A],
          sb: Expr[StringBuilder],
          indent: Expr[String],
          nesting: Expr[Int]
      ): Expr[StringBuilder]
    }

    val StringBuilder: StringBuilderModule
    trait StringBuilderModule { this: StringBuilder.type =>

      def append(sb: Expr[StringBuilder], value: Expr[String]): Expr[StringBuilder]

      def repeatAppend(sb: Expr[StringBuilder], value: Expr[String], times: Expr[Int]): Expr[StringBuilder]
    }

    // ...or not

    def incInt(int: Expr[Int]): Expr[Int]

    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit]
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit]
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit]

    def toString[A: Type](expr: Expr[A]): Expr[String]
  }

  // As the next step, let's define some extension methods to make the work Expr-essions easier:

  implicit class FastShowPrettyOps[A: Type](private val instance: Expr[FastShowPretty[A]]) {

    def showPretty(
        value: Expr[A],
        sb: Expr[StringBuilder],
        indent: Expr[String],
        nesting: Expr[Int]
    ): Expr[StringBuilder] = ShowExpr.FastShowPretty.showPretty(instance, value, sb, indent, nesting)
  }
  implicit class StringBuilderOps(private val sb: Expr[StringBuilder]) {
    def append(value: Expr[String]): Expr[StringBuilder] = ShowExpr.StringBuilder.append(sb, value)
    def repeatAppend(value: Expr[String], times: Expr[Int]) = ShowExpr.StringBuilder.repeatAppend(sb, value, times)
  }
  implicit class IntIncOps(private val int: Expr[Int]) {
    def inc: Expr[Int] = ShowExpr.incInt(int)
  }
  implicit class ArrayForeachOps[A: Type](private val expr: Expr[Array[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.arrayForeach[A, B](expr, f)
  }
  implicit class ListForeachOps[A: Type](private val expr: Expr[List[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.listForeach[A, B](expr, f)
  }
  implicit class VectorForeachOps[A: Type](private val expr: Expr[Vector[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.vectorForeach[A, B](expr, f)
  }
  implicit class ToStringOps[A: Type](private val expr: Expr[A]) {
    def toStringExpr: Expr[String] = ShowExpr.toString(expr)
  }

  // As the last stop before the actual implementation:
  // - utilities for passing around input arguments,
  // - configs,
  // - logging,
  // - adjusting values for recursive derivation,
  // - chain-of-responsibility,
  // etc

  case class ShowingContext[A](
      shown: Type[A],
      value: Expr[A],
      sb: Expr[StringBuilder],
      indent: Expr[String],
      nesting: Expr[Int],
      avoidImplicit: Boolean,
      logNesting: Int,
      log: mutable.ListBuffer[String]
  ) {

    def nest[B: Type](value: Expr[B]): ShowingContext[B] = copy[B](
      shown = Type[B],
      value = value,
      nesting = nesting.inc,
      avoidImplicit = false,
      logNesting = logNesting + 1
    )
  }
  object ShowingContext {

    def create[A: Type](
        value: Expr[A],
        sb: Expr[StringBuilder],
        indent: Expr[String],
        nesting: Expr[Int]
    ): ShowingContext[A] = ShowingContext(
      shown = Type[A],
      value = value,
      sb = sb,
      indent = indent,
      nesting = nesting,
      avoidImplicit = true,
      logNesting = 0,
      log = mutable.ListBuffer.empty[String]
    )
  }

  implicit def showType[A](implicit ctx: ShowingContext[A]): Type[A] = ctx.shown
  def value[A](implicit ctx: ShowingContext[A]): Expr[A] = ctx.value
  def sb(implicit ctx: ShowingContext[?]): Expr[StringBuilder] = ctx.sb
  def indent(implicit ctx: ShowingContext[?]): Expr[String] = ctx.indent
  def nesting(implicit ctx: ShowingContext[?]): Expr[Int] = ctx.nesting
  def shouldAvoidImplicit(implicit ctx: ShowingContext[?]): Boolean = ctx.avoidImplicit
  def log(msg: String)(implicit ctx: ShowingContext[?]): Unit = ctx.log.addOne(" - " + ("  " * ctx.logNesting) + msg)
  def nestedCtx[B: Type](value: Expr[B])(implicit ctx: ShowingContext[?]): ShowingContext[B] = ctx.nest(value)

  sealed trait DerivationError
  object DerivationError {
    case class TypeNotSupported(typeName: String) extends DerivationError
  }

  type DerivationResult[A] = Either[List[DerivationError], A]
  implicit val applicativeResult: Applicative[DerivationResult] = new Applicative[DerivationResult] {
    def map2[A, B, C](fa: DerivationResult[A], fb: DerivationResult[B])(f: (A, B) => C): DerivationResult[C] =
      for { a <- fa; b <- fb } yield f(a, b)
    def pure[A](a: A): DerivationResult[A] = Right(a)
  }

  type FinalResult = DerivationResult[Expr[StringBuilder]]
  def ruleNotMatched: Option[FinalResult] = None
  def ruleSucceeded(sb: Expr[StringBuilder]): Option[FinalResult] = Some(Right(sb))
  def ruleFailed(error: DerivationError*): Option[FinalResult] = Some(Left(List(error*)))

  trait DerivationRule {

    // Whether this rule applies, and if it does what is the result
    def attempt[A: ShowingContext]: Option[FinalResult]
  }

  // With all of these we can define our derivation rules:

  import Type.Implicits.*
  import ShowType.Implicits.*

  object ImplicitRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] =
      if (shouldAvoidImplicit) {
        log(s"Skipped summoning ${Type.prettyPrint[FastShowPretty[A]]}")
        ruleNotMatched
      } else
        (Expr.summonImplicit[FastShowPretty[A]] match {
          case Some(instance) => ruleSucceeded(instance.showPretty(value[A], sb, indent, nesting))
          case None           => ruleNotMatched
        })
  }

  object BuildInRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = Type[A] match {
      case tpe if tpe =:= Type[String] =>
        ruleSucceeded(sb.append(Expr.String("\"")).append(value[A].upcastToExprOf[String]).append(Expr.String("\"")))
      case tpe if tpe =:= Type[Char] =>
        ruleSucceeded(sb.append(Expr.String("'")).append(value[A].toStringExpr).append(Expr.String("'")))
      case tpe if tpe =:= Type[Boolean] =>
        ruleSucceeded(sb.append(value[A].toStringExpr))
      case tpe if tpe =:= Type[Byte] =>
        ruleSucceeded(sb.append(value[A].toStringExpr).append(Expr.String(".toByte")))
      case tpe if tpe =:= Type[Short] =>
        ruleSucceeded(sb.append(value[A].toStringExpr).append(Expr.String(".toShort")))
      case tpe if tpe =:= Type[Int] =>
        ruleSucceeded(sb.append(value[A].toStringExpr))
      case tpe if tpe =:= Type[Long] =>
        ruleSucceeded(sb.append(value[A].toStringExpr).append(Expr.String("L")))
      case tpe if tpe =:= Type[Float] =>
        ruleSucceeded(sb.append(value[A].toStringExpr))
      case tpe if tpe =:= Type[Double] =>
        ruleSucceeded(sb.append(value[A].toStringExpr).append(Expr.String("f")))
      case tpe if tpe =:= Type[BigInt] =>
        ruleSucceeded(sb.append(value[A].toStringExpr))
      case tpe if tpe =:= Type[BigDecimal] =>
        ruleSucceeded(sb.append(value[A].toStringExpr))
      case tpe if tpe =:= Type[Unit] =>
        ruleSucceeded(sb.append(Expr.String("()")))
      case Type.Array(inner) =>
        import inner.Underlying as Inner
        handleCollection[Inner](value[A].upcastToExprOf[Array[Inner]].foreach(_))
      case ShowType.List(inner) =>
        import inner.Underlying as Inner
        handleCollection[Inner](value[A].upcastToExprOf[List[Inner]].foreach(_))
      case ShowType.Vector(inner) =>
        import inner.Underlying as Inner
        handleCollection[Inner](value[A].upcastToExprOf[Vector[Inner]].foreach(_))
      case _ => ruleNotMatched
    }

    private def handleCollection[Inner: Type](
        foreach: Expr[Inner => StringBuilder] => Expr[Unit]
    )(implicit ctx: ShowingContext[_]): Option[FinalResult] = {
      // We'll need a lambda to pass it into .foreach.
      // But we're getting Either[List[DerivationError], Expr[StringBuilder]] instead of Expr[StringBuilder], so simple:
      //   '{ arg => ${ deriveShowing(nestedCtx[Inner]('arg)) } }
      // would not work.
      // ExprPromise allows creating and passing recursively an Expr[Inner] without creating a lambda upfront.
      ExprPromise
        .promise[Inner](ExprPromise.NameGenerationStrategy.FromType)
        .map { (innerExpr: Expr[Inner]) =>
          deriveShowing(nestedCtx[Inner](innerExpr))
        }
        .sequence // turns ExprPromise[Inner, DerivationResult[...]] into DerivationResult[ExprPromise[Inner, ...]]
        .map { promise =>
          // TODO: append name
          // TODO: then (
          // TODO: then all that crap
          // TODO: then )

          // Creates:
          // {
          //   arr.foreach { a => ... }
          //   sb
          // }
          Expr.block(List(foreach(promise.fulfilAsLambda)), sb)
        }
        .pipe(Option(_))
      }
  }

  object ProductRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = ruleNotMatched // TODO
  }

  object SumTypeRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = ruleNotMatched // TODO
  }

  val rules = List(ImplicitRule, BuildInRule, ProductRule, SumTypeRule)

  def deriveShowing[A: ShowingContext]: FinalResult = rules
    .foldLeft {
      log(s"Started derivation for ${value.prettyPrint} : ${Type.prettyPrint[A]}")
      ruleNotMatched
    } { (result, rule) =>
      result orElse {
        log(s"Attempting rule $rule")
        rule.attempt[A]
      }
    }
    .map { result =>
      if (result.isRight) { log(s"Successfully shown ${Type.prettyPrint[A]}") }
      result
    }
    .getOrElse {
      log(s"Failed to resolve how to show ${value.prettyPrint} : ${Type.prettyPrint[A]}")
      Left(List(DerivationError.TypeNotSupported(Type.prettyPrint[A])))
    }

  def deriveShowingExpression[A: ShowingContext]: Expr[StringBuilder] =
    deriveShowing[A].tap { _ =>
      if (XMacroSettings.contains("fastshowpretty.logging=true")) {
        reportInfo(s"Logs:\n${implicitly[ShowingContext[A]].log.mkString("\n")}")
      }
    } match {
      case Left(errors) =>
        val humanReadableErrors = errors
          .map { case DerivationError.TypeNotSupported(typeName) =>
            s"No build-in support nor implicit for type $typeName"
          }
          .mkString("\n")
        reportError(s"Failed to derive showing for ${value.prettyPrint} : ${Type.prettyPrint[A]}:$humanReadableErrors")
      case Right(value) => value
    }
}
