package example.showmacros.internal

import example.showmacros.FastShowPretty
import io.scalaland.chimney.internal.compiletime.Definitions
import io.scalaland.chimney.internal.compiletime.datatypes.{ProductTypes, SealedHierarchies}
import io.scalaland.chimney.internal.compiletime.fp.{Applicative, Parallel, Traverse}
import io.scalaland.chimney.internal.compiletime.fp.Implicits.*

import scala.collection.immutable.ListMap
import scala.collection.mutable
import scala.util.chaining.*
import scala.util.control.NoStackTrace

trait Derivation extends Definitions with ProductTypes with SealedHierarchies {

  // Here we are using utilities from chimney-macro-commons instead of Scala 2/Scala 3 macros directly.
  // We'll use "Show" prefix because we are working on Show library, and we don't want to mix these definitions
  // with definitions coming from chimney-macro-commons.

  import Type.Implicits.*

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

    def simpleName[A: Type]: String = {
      val colored = Type.prettyPrint[A]
      val mono = "\u001b\\[([0-9]+)m".r.replaceAllIn(colored, "")
      val start = mono.lastIndexOf(".") + 1
      val end = mono.indexOf("[", start) - 1
      mono.substring(start.max(0), if (end < 0) mono.length else end)
    }

    // When needed, import ShowType.Implicits._
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

      def instance[A: Type](
          body: (Expr[A], Expr[StringBuilder], Expr[String], Expr[Int]) => Expr[StringBuilder]
      ): Expr[FastShowPretty[A]]

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

      def appendCaseClassStart(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit]
      def appendCaseClassEnd(sb: Expr[StringBuilder], indent: Expr[String], nesting: Expr[Int]): Expr[Unit]

      def appendFieldStart(
          sb: Expr[StringBuilder],
          fieldName: Expr[String],
          indent: Expr[String],
          nesting: Expr[Int]
      ): Expr[Unit]
      def appendFieldEnd(sb: Expr[StringBuilder]): Expr[Unit]

      def appendCaseObject(sb: Expr[StringBuilder], className: Expr[String]): Expr[Unit]
    }

    // ...or not

    def incInt(int: Expr[Int]): Expr[Int]

    def arrayForeach[A: Type, B: Type](expr: Expr[Array[A]], f: Expr[A => B]): Expr[Unit]
    def arrayIsEmpty[A: Type](expr: Expr[Array[A]]): Expr[Boolean]
    def listForeach[A: Type, B: Type](expr: Expr[List[A]], f: Expr[A => B]): Expr[Unit]
    def listIsEmpty[A: Type](expr: Expr[List[A]]): Expr[Boolean]
    def vectorForeach[A: Type, B: Type](expr: Expr[Vector[A]], f: Expr[A => B]): Expr[Unit]
    def vectorIsEmpty[A: Type](expr: Expr[Vector[A]]): Expr[Boolean]

    def toString[A: Type](expr: Expr[A]): Expr[String]

    def void[A: Type](expr: Expr[A]): Expr[Unit]

    def unitBlock(statements: Expr[Unit]*): Expr[Unit] =
      Expr.block[Unit](statements.toList.asInstanceOf[List[Expr[Unit]]], Expr.Unit)
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

    def append(value: Expr[String]): Expr[StringBuilder] =
      ShowExpr.StringBuilder.append(sb, value)
    def repeatAppend(value: Expr[String], times: Expr[Int]): Expr[StringBuilder] =
      ShowExpr.StringBuilder.repeatAppend(sb, value, times)

    def appendCaseClassStart(className: Expr[String]): Expr[Unit] =
      ShowExpr.StringBuilder.appendCaseClassStart(sb, className)
    def appendCaseClassEnd(indent: Expr[String], nesting: Expr[Int]): Expr[Unit] =
      ShowExpr.StringBuilder.appendCaseClassEnd(sb, indent, nesting)

    def appendFieldStart(fieldName: Expr[String], indent: Expr[String], nesting: Expr[Int]): Expr[Unit] =
      ShowExpr.StringBuilder.appendFieldStart(sb, fieldName, indent, nesting)
    def appendFieldEnd: Expr[Unit] = ShowExpr.StringBuilder.appendFieldEnd(sb)

    def appendCaseObject(className: Expr[String]): Expr[Unit] = ShowExpr.StringBuilder.appendCaseObject(sb, className)
  }
  implicit class IntIncOps(private val int: Expr[Int]) {
    def inc: Expr[Int] = ShowExpr.incInt(int)
  }
  implicit class ArrayForeachOps[A: Type](private val expr: Expr[Array[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.arrayForeach[A, B](expr, f)
    def isEmpty: Expr[Boolean] = ShowExpr.arrayIsEmpty[A](expr)
  }
  implicit class ListForeachOps[A: Type](private val expr: Expr[List[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.listForeach[A, B](expr, f)
    def isEmpty: Expr[Boolean] = ShowExpr.listIsEmpty[A](expr)
  }
  implicit class VectorForeachOps[A: Type](private val expr: Expr[Vector[A]]) {
    def foreach[B: Type](f: Expr[A => B]): Expr[Unit] = ShowExpr.vectorForeach[A, B](expr, f)
    def isEmpty: Expr[Boolean] = ShowExpr.vectorIsEmpty[A](expr)
  }
  implicit class ToStringOps[A: Type](private val expr: Expr[A]) {
    def toStringExpr: Expr[String] = ShowExpr.toString(expr)
  }
  implicit class VoidOps[A: Type](private val expr: Expr[A]) {
    def void: Expr[Unit] = ShowExpr.void(expr)
  }

  // As the last stop before the actual implementation:
  // - utilities for passing around input arguments,
  // - configs,
  // - logging,
  // - adjusting values for recursive derivation,
  // - chain-of-responsibility,
  // - caching
  // etc

  trait DirectStyle[F[_]] {

    def asyncUnsafe[A](thunk: => A): F[A]
    def awaitUnsafe[A](value: F[A]): A
    def async[A, B](await: (F[A] => A) => B): F[B] = asyncUnsafe(await(awaitUnsafe))
  }
  object DirectStyle {

    def apply[F[_]](implicit F: DirectStyle[F]): DirectStyle[F] = F
  }

  abstract class DefCache[F[_]: DirectStyle] {
    // Soo... neither a =:= b nor Type.isSameAs(a, b) want to work with Type[?] :/
    implicit private class TypeAnyOps[A](private val tpe: Type[A]) {
      def asAny: Type[Any] = tpe.asInstanceOf[Type[Any]]
    }

    private case class Signature(name: String, input: List[Type[Any]], output: Type[Any]) {

      override def hashCode(): Int = name.hashCode

      override def equals(obj: Any): Boolean = obj match {
        case Signature(name2, input2, output2) =>
          name == name2 &&
          input.length == input2.length &&
          input.zip(input2).forall(p => p._1 =:= p._2) &&
          output =:= output2
        case _ => false
      }

      override def toString: String =
        s"def $name(${input.map(Type.prettyPrint(_)).mkString(", ")}): ${Type.prettyPrint(output)}"
    }
    private object Signature {
      def apply[In1: Type, Out: Type](name: String) =
        new Signature(name, List(Type[In1].asAny), Type[Out].asAny)
      def apply[In1: Type, In2: Type, Out: Type](name: String) =
        new Signature(name, List(Type[In1].asAny, Type[In2].asAny), Type[Out].asAny)
    }

    // Allows accessing def inside its body
    protected trait PendingDef {
      def cast[A]: A
    }

    // Allows accessing def once it's defined
    protected trait Def {
      def prependDef[A: Type](expr: Expr[A]): Expr[A]
      def cast[A]: A
    }

    // Platform-specific way of creating def out of its body
    protected trait Define[In, Out] {
      def apply(body: In => Out): Def
      def pending: PendingDef
    }

    private var pending = ListMap.empty[Signature, PendingDef]
    private var defined = ListMap.empty[Signature, Def]

    private def unsafeGet[A](signature: Signature): Option[A] =
      defined.get(signature).map(_.cast[A]).orElse(pending.get(signature).map(_.cast[A]))

    final class Builder[In, Out, A] private (
        private val signature: Signature,
        private val body: In => A,
        private val define: Define[In, Out]
    ) {
      def map[B](f: A => B): Builder[In, Out, B] =
        new Builder[In, Out, B](signature, body andThen f, define)
      def emap[B](f: A => F[B]): Builder[In, Out, B] =
        new Builder[In, Out, B](signature, body andThen f andThen DirectStyle[F].awaitUnsafe, define)
      def build(implicit ev: (In => A) =:= (In => Out)): Unit = {
        pending = pending.removed(signature)
        defined = defined + (signature -> define(ev(body)))
      }
    }
    private object Builder {
      def apply[In, Out](signature: Signature, define: Define[In, Out]): Builder[In, Out, In] = {
        pending = pending + (signature -> define.pending)
        new Builder[In, Out, In](signature, identity[In](_), define)
      }
    }

    final def build1[In1: Type, Out: Type](name: String): Builder[Expr[In1], Expr[Out], Expr[In1]] =
      Builder(Signature[In1, Out](name), define1[In1, Out](name))
    final def build2[In1: Type, In2: Type, Out: Type](
        name: String
    ): Builder[(Expr[In1], Expr[In2]), Expr[Out], (Expr[In1], Expr[In2])] =
      Builder(Signature[In1, In2, Out](name), define2[In1, In2, Out](name))

    final def of1[In1: Type, Out: Type](name: String): Option[Expr[In1] => F[Expr[Out]]] =
      unsafeGet[Expr[In1] => Expr[Out]](Signature[In1, Out](name)).map(fn => in1 => DirectStyle[F].asyncUnsafe(fn(in1)))
    final def of2[In1: Type, In2: Type, Out: Type](name: String): Option[(Expr[In1], Expr[In2]) => F[Expr[Out]]] =
      unsafeGet[(Expr[In1], Expr[In2]) => Expr[Out]](Signature[In1, In2, Out](name)).map(fn =>
        (in1, in2) => DirectStyle[F].asyncUnsafe(fn(in1, in2))
      )

    final def prependDefs[A: Type](expr: Expr[A]): Expr[A] = {
      var prepended = Set.empty[Signature]
      var toPrepend = defined.removedAll(prepended)
      var result = expr
      // Values to prepend might be lazy so, we have to prepend them until nothing new was computed.
      while (toPrepend.nonEmpty) {
        result = toPrepend.values.foldRight(result)((def0, e) => def0.prependDef(e))
        prepended = prepended ++ toPrepend.keys
        toPrepend = defined.removedAll(prepended)
      }
      result
    }

    // Platform-specific implementations
    protected def define1[In1: Type, Out: Type](name: String): Define[Expr[In1], Expr[Out]]
    protected def define2[In1: Type, In2: Type, Out: Type](name: String): Define[(Expr[In1], Expr[In2]), Expr[Out]]

    override def toString: String =
      s"DefCache(pending = Seq(${pending.keys.mkString(", ")}), defined = Seq(${defined.keys.mkString(", ")}))"
  }

  def newDefCache[F[_]: DirectStyle]: DefCache[F]

  case class ShowingContext[A](
      // Input data required for rendering the currently handled value/type
      shown: Type[A],
      value: Expr[A],
      sb: Expr[StringBuilder],
      indent: Expr[String],
      nesting: Expr[Int],
      // Metadata/configs
      avoidImplicit: Boolean,
      recurNesting: Int,
      defCache: DefCache[DerivationResult],
      // Logging
      log: mutable.ListBuffer[String]
  ) {

    def nest[B: Type](value: Expr[B], nesting: Expr[Int]): ShowingContext[B] = copy[B](
      shown = Type[B],
      value = value,
      nesting = nesting,
      avoidImplicit = false,
      recurNesting = recurNesting + 1
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
      recurNesting = 0,
      log = mutable.ListBuffer.empty[String],
      defCache = newDefCache[DerivationResult]
    )
  }

  // Makes Type[A] available when ShowingContext[A] is available
  implicit def showType[A](implicit ctx: ShowingContext[A]): Type[A] = ctx.shown
  // Allow obtaining these utilities as if they were stored in globals
  def value[A](implicit ctx: ShowingContext[A]): Expr[A] = ctx.value
  def sb(implicit ctx: ShowingContext[?]): Expr[StringBuilder] = ctx.sb
  def indent(implicit ctx: ShowingContext[?]): Expr[String] = ctx.indent
  def nesting(implicit ctx: ShowingContext[?]): Expr[Int] = ctx.nesting
  def shouldAvoidImplicit(implicit ctx: ShowingContext[?]): Boolean = ctx.avoidImplicit
  def shouldLog: Boolean = XMacroSettings.contains("fastshowpretty.logging=true")
  def defCache(implicit ctx: ShowingContext[?]): DefCache[DerivationResult] = ctx.defCache
  def log(msg: String)(implicit ctx: ShowingContext[?]): Unit = if (shouldLog) {
    ctx.log.addOne(("  " * ctx.recurNesting) + " - " + msg)
  }
  def nestedCtx[B: Type](value: Expr[B], nesting: Expr[Int])(implicit ctx: ShowingContext[?]): ShowingContext[B] =
    ctx.nest(value, nesting)

  sealed trait DerivationError
  object DerivationError {
    case class TypeNotSupported(typeName: String) extends DerivationError
    case class DefNotFound(typeName: String) extends DerivationError
    case class AssertionFailed(msg: String) extends DerivationError
  }

  type DerivationResult[A] = Either[List[DerivationError], A]
  implicit val applicativeResult: Applicative[DerivationResult] = new Applicative[DerivationResult] {
    def map2[A, B, C](fa: DerivationResult[A], fb: DerivationResult[B])(f: (A, B) => C): DerivationResult[C] =
      for { a <- fa; b <- fb } yield f(a, b)
    def pure[A](a: A): DerivationResult[A] = Right(a)
  }
  implicit val directStyleResult: DirectStyle[DerivationResult] = new DirectStyle[DerivationResult] {
    private case class PassErrors(errors: List[DerivationError]) extends NoStackTrace
    def asyncUnsafe[A](thunk: => A): DerivationResult[A] = try
      Right(thunk)
    catch {
      case PassErrors(errors) => Left(errors)
    }
    def awaitUnsafe[A](value: DerivationResult[A]): A = value match {
      case Left(errors) => throw PassErrors(errors)
      case Right(value) => value
    }
  }

  // Provides slightly better description of the intent than nested Options and Eithers
  type FinalResult = DerivationResult[Expr[Unit]]
  def derivationFailed(error: DerivationError*): FinalResult = Left(List(error*))
  def ruleNotMatched: Option[FinalResult] = None
  def ruleMatched(fr: FinalResult): Option[FinalResult] = Some(fr)
  def ruleSucceeded(sb: Expr[Unit]): Option[FinalResult] = Some(Right(sb))

  // To decrease the size of the code and speed up both compilation we'll cache some rule bodies as defs.

  // Here, we're making assumption that the input type is enough to distinct these defs in our macro.
  def cacheName[A: ShowingContext]: String = "show_" + ShowType.simpleName[A]

  // Caches the rule implementation as a def the first time it is required. Fails if def couldn't be created.
  def cacheRuleAsDef[A: ShowingContext](
      applyUpdatedCtx: ShowingContext[A] => FinalResult
  ): FinalResult = {
    val defName = cacheName[A]
    log(s"Checking if def for ${Type.prettyPrint[A]} exists")
    defCache.of2[A, Int, Unit](defName).orElse {
      defCache
        .build2[A, Int, Unit](defName)
        .emap { case (newValue: Expr[A], newNesting: Expr[Int]) =>
          applyUpdatedCtx(implicitly[ShowingContext[A]].copy[A](value = newValue, nesting = newNesting))
        }
        .build
      defCache.of2[A, Int, Unit](defName)
    } match {
      case Some(defBody) => defBody(value[A], nesting) // calls def with actual value
      case None          => derivationFailed(DerivationError.DefNotFound(Type.prettyPrint[A]))
    }
  }

  // Use cache if available, pass if not.
  def useCache[A: ShowingContext]: Option[FinalResult] =
    defCache.of2[A, Int, Unit](cacheName[A]).map(fn => fn(value[A], nesting))

  trait DerivationRule {

    // Whether this rule applies, and if it does what is the result
    def attempt[A: ShowingContext]: Option[FinalResult]
  }

  // With all of these we can define our derivation rules:

  import Type.Implicits.*
  import ShowType.Implicits.*

  case object ImplicitRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] =
      if (shouldAvoidImplicit) {
        log(s"Skipped summoning ${Type.prettyPrint[FastShowPretty[A]]}")
        ruleNotMatched
      } else
        (Expr.summonImplicit[FastShowPretty[A]] match {
          case Some(instance) => ruleSucceeded(handleInstance(instance))
          case None           => ruleNotMatched
        })

    private def handleInstance[A: ShowingContext](instance: Expr[FastShowPretty[A]]): Expr[Unit] =
      instance.showPretty(value[A], sb, indent, nesting).void
  }

  case object CachedDefRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = useCache[A]
  }

  case object BuildInRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = Type[A] match {
      case tpe if tpe =:= Type[Nothing] =>
        Some(derivationFailed(DerivationError.TypeNotSupported(Type.prettyPrint[Nothing])))
      case tpe if tpe =:= Type[String]     => ruleSucceeded(handleString[A])
      case tpe if tpe =:= Type[Char]       => ruleSucceeded(handleChar[A])
      case tpe if tpe =:= Type[Boolean]    => ruleSucceeded(handleBoolean[A])
      case tpe if tpe =:= Type[Byte]       => ruleSucceeded(handleByte[A])
      case tpe if tpe =:= Type[Short]      => ruleSucceeded(handleShort[A])
      case tpe if tpe =:= Type[Int]        => ruleSucceeded(handleInt[A])
      case tpe if tpe =:= Type[Long]       => ruleSucceeded(handleLong[A])
      case tpe if tpe =:= Type[Float]      => ruleSucceeded(handleFloat[A])
      case tpe if tpe =:= Type[Double]     => ruleSucceeded(handleDouble[A])
      case tpe if tpe =:= Type[BigInt]     => ruleSucceeded(handleBigInt[A])
      case tpe if tpe =:= Type[BigDecimal] => ruleSucceeded(handleBigDecimal)
      case tpe if tpe =:= Type[Unit]       => ruleSucceeded(handleUnit[A])
      case Type.Array(inner) =>
        import inner.Underlying as Inner
        val coll = value[A].upcastToExprOf[Array[Inner]]
        ruleMatched(handleCollectionOrFail[Inner]("Array", coll.isEmpty, coll.foreach(_)))
      case ShowType.List(inner) =>
        import inner.Underlying as Inner
        val coll = value[A].upcastToExprOf[List[Inner]]
        ruleMatched(handleCollectionOrFail[Inner]("List", coll.isEmpty, coll.foreach(_)))
      case ShowType.Vector(inner) =>
        import inner.Underlying as Inner
        val coll = value[A].upcastToExprOf[Vector[Inner]]
        ruleMatched(handleCollectionOrFail[Inner]("Vector", coll.isEmpty, coll.foreach(_)))
      case _ => ruleNotMatched
    }

    private def handleString[A: ShowingContext]: Expr[Unit] =
      sb.append(Expr.String("\"")).append(value[A].upcastToExprOf[String]).append(Expr.String("\"")).void
    private def handleChar[A: ShowingContext]: Expr[Unit] =
      sb.append(Expr.String("'")).append(value[A].toStringExpr).append(Expr.String("'")).void

    private def handleBoolean[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).void
    private def handleByte[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).append(Expr.String(".toByte")).void

    private def handleShort[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).append(Expr.String(".toShort")).void
    private def handleInt[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).void
    private def handleLong[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).append(Expr.String("L")).void
    private def handleFloat[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).append(Expr.String("f")).void
    private def handleDouble[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).void

    private def handleBigInt[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).void
    private def handleBigDecimal[A: ShowingContext]: Expr[Unit] =
      sb.append(value[A].toStringExpr).void

    private def handleUnit[A: ShowingContext]: Expr[Unit] =
      sb.append(Expr.String("()")).void

    private def handleCollectionOrFail[Inner: Type](
        className: String,
        isEmpty: Expr[Boolean],
        foreach: Expr[Inner => Unit] => Expr[Unit]
    )(implicit ctx: ShowingContext[?]): FinalResult =
      // We'll need a lambda to pass it into .foreach.
      // But we're getting Either[List[DerivationError], Expr[StringBuilder]] instead of Expr[StringBuilder], so simple:
      //   '{ arg => ${ recur('arg) } }
      // would not work.
      // ExprPromise allows creating and passing recursively an Expr[Inner] without creating a lambda upfront.
      ExprPromise
        .promise[Inner](ExprPromise.NameGenerationStrategy.FromType)
        .map { (innerExpr: Expr[Inner]) =>
          recur(innerExpr)
        }
        .sequence // turns ExprPromise[Inner, DerivationResult[...]] into DerivationResult[ExprPromise[Inner, ...]]
        .map { promise =>
          // Reused collections methods
          Expr.ifElse[Unit](isEmpty) {
            sb.appendCaseObject(Expr.String(className))
          } {
            ShowExpr.unitBlock(
              sb.appendCaseClassStart(Expr.String(className)),
              foreach(promise.map { expandedShowPretty =>
                ShowExpr.unitBlock(
                  sb.repeatAppend(indent, nesting.inc).void,
                  expandedShowPretty,
                  sb.append(Expr.String(",\n")).void
                )
              }.fulfilAsLambda),
              sb.appendCaseClassEnd(indent, nesting)
            )
          }
        }
  }

  case object ProductRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = Type[A] match {
      // Uses utilities from ProductTypes mixin.
      case ProductType(Product(Product.Extraction(extraction), Product.Constructor(parameters, _))) =>
        if (Type[A].isCaseClass || Type[A].isCaseObject) {
          ruleMatched(cacheRuleAsDef[A](ctx => handleCaseClassOrFail(extraction, parameters)(ctx)))
        } else {
          log(s"We could access ${Type.prettyPrint[A]} fields and constructor, but it's not a case class")
          ruleNotMatched
        }
      case _ =>
        ruleNotMatched
    }

    private def handleCaseClassOrFail[A: ShowingContext](
        extraction: Product.Getters[A],
        parameters: Product.Parameters
    ): FinalResult = {
      // Type hints for a silly compiler
      val caseClassArgs: List[(String, ExistentialType)] = parameters.toList.collect {
        case (fieldName: String, param: Existential[Product.Parameter])
            if param.value.targetType == Product.Parameter.TargetType.ConstructorParameter =>
          fieldName -> param.Underlying.as_??
      }
      caseClassArgs
        .flatMap { case (fieldName: String, param: ExistentialType) =>
          import param.Underlying as FieldType
          extraction.collectFirst {
            case (`fieldName`, someGetter)
                if someGetter.value.sourceType == Product.Getter.SourceType.ConstructorVal && someGetter.Underlying =:= FieldType =>
              import someGetter.{Underlying as GetterType, value as getter}
              val fieldExpr = getter.get(value[A]).upcastToExprOf[FieldType]
              recur(fieldExpr).map { expandedShowPretty =>
                ShowExpr.unitBlock(
                  sb.appendFieldStart(Expr.String(fieldName), indent, nesting.inc),
                  expandedShowPretty,
                  sb.appendFieldEnd
                )
              }
          }.toList
        }
        .sequence
        .map { expandedShowPrettyForFields =>
          val className = ShowType.simpleName[A]
          if (expandedShowPrettyForFields.isEmpty) {
            sb.appendCaseObject(Expr.String(className))
          } else {
            ShowExpr.unitBlock(
              (List(sb.appendCaseClassStart(Expr.String(className))) ++ expandedShowPrettyForFields ++ List(
                sb.appendCaseClassEnd(indent, nesting)
              ))*
            )
          }
        }
    }
  }

  case object SumTypeRule extends DerivationRule {

    def attempt[A: ShowingContext]: Option[FinalResult] = Type[A] match {
      // Uses utilities from SealedHierarchies mixin.
      case SealedHierarchy(subtypes) =>
        ruleMatched(cacheRuleAsDef[A](ctx => handleSealedTraitOrFail(subtypes.elements)(ctx)))
      case _ => ruleNotMatched
    }

    private def handleSealedTraitOrFail[A: ShowingContext](
        elements: List[Existential.UpperBounded[A, Enum.Element[A, *]]]
    ): FinalResult = {
      // Type hints for a silly compiler
      type ElementOfA[B] = Enum.Element[A, B]
      elements
        .traverse[DerivationResult, PatternMatchCase[Unit]] { (subtype: Existential.UpperBounded[A, ElementOfA]) =>
          import subtype.Underlying as Subtype
          // Here ExprPromise is used for creating: case subtypeExpr: Subtype => expandedShowPretty
          ExprPromise
            .promise[Subtype](ExprPromise.NameGenerationStrategy.FromType)
            .traverse { subtypeExpr =>
              recur(subtypeExpr)
            }
            .map(_.fulfillAsPatternMatchCase)
        }
        .map(matchCases => matchCases.matchOn(value[A])) // Creates: expr match { ... list of cases defined above }
    }
  }

  val rules = List(ImplicitRule, CachedDefRule, BuildInRule, ProductRule, SumTypeRule)

  def deriveShowing[A: ShowingContext]: FinalResult = rules
    .foldLeft {
      log(s"Started derivation for ${value.prettyPrint} : ${Type.prettyPrint[A]}")
      ruleNotMatched
    } { (result, rule) =>
      result orElse {
        log(s"Attempting rule $rule")
        try
          rule.attempt[A]
        catch {
          case e: Throwable =>
            Some(derivationFailed(DerivationError.AssertionFailed(s"Exception in macro: ${e.getMessage}")))
        }
      }
    }
    .map { result =>
      result match {
        case Right(expr) => log(s"Successfully shown ${Type.prettyPrint[A]}: ${expr.prettyPrint}")
        case _           =>
      }
      result
    }
    .getOrElse {
      log(s"Failed to resolve how to show ${value.prettyPrint} : ${Type.prettyPrint[A]}")
      derivationFailed(DerivationError.TypeNotSupported(Type.prettyPrint[A]))
    }

  // Intended for recursive use: when going for a field in a case class/a subtype in sealed, we'll call it.
  def recur[A: Type](newValue: Expr[A])(implicit showingContext: ShowingContext[?]): FinalResult = {
    val cacheNewNesting =
      PrependDefinitionsTo.prependVal(nesting.inc, ExprPromise.NameGenerationStrategy.FromPrefix("nesting"))
    val cacheNewValue =
      PrependDefinitionsTo.prependVal(newValue, ExprPromise.NameGenerationStrategy.FromType)
    (cacheNewNesting)
      .map2(cacheNewValue)(_ -> _)
      .traverse { case (cachedNewNesting, cachedNewValue) =>
        deriveShowing[A](nestedCtx(cachedNewValue, cachedNewNesting))
      }
      .map(_.closeBlockAsExprOf[Unit])
  }

  // Intended for top-level call as it reads the configs and prepares possible diagnostics/error message.
  def deriveShowingExpression[A: ShowingContext]: Expr[StringBuilder] =
    deriveShowing[A]
      .map { expr =>
        // Makes sure that StringBuilder is returned, as expected by API and prepend all the generated defs.
        Expr
          .block[StringBuilder](List(defCache.prependDefs(expr)), sb)
          .tap(e => log(s"The final expression is:\n${e.prettyPrint}"))
      }
      .tap { _ =>
        // Show the log in the compilation output/IDE/Scastie if -Xmacro-settings:fastshowpretty.logging=true was passed
        if (shouldLog) {
          reportInfo(s"Logs:\n${implicitly[ShowingContext[A]].log.mkString("\n")}")
        }
      } match {
      case Left(errors) =>
        // Adjust error ADT for the reader
        val humanReadableErrors = errors
          .map {
            case DerivationError.TypeNotSupported(typeName) => s"No build-in support nor implicit for type $typeName"

            case DerivationError.DefNotFound(typeName) => s"Attempt to cache showing $typeName as def failed"

            case DerivationError.AssertionFailed(msg) => s"Assertion failed during derivation: $msg"

          }
          .mkString("\n")
        reportError(
          s"Failed to derive showing for ${value.prettyPrint} : ${Type.prettyPrint[A]}:\n$humanReadableErrors"
        )
      case Right(value) => value
    }
}
