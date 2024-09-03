package example.model1

final case class In1(int: Int)
final case class In2(i11: In1, i12: In1)
final case class In3(i21: In2, i22: In2, i23: In2)
final case class In4(i31: In3, i32: In3, i33: In3, i34: In3)
final case class In5(i41: In4, i42: In4, i43: In4, i44: In4, i45: In4)
final case class In6(i51: In5, i52: In5, i53: In5, i54: In5, i55: In5, i56: In5)
final case class In7(i61: In6, i62: In6, i63: In6, i64: In6, i65: In6, i66: In6, i67: In6)
final case class In8(i71: In7, i72: In7, i73: In7, i74: In7, i75: In7, i76: In7, i77: In7, i78: In7)
final case class In9(i81: In8, i82: In8, i83: In8, i84: In8, i85: In8, i86: In8, i87: In8, i88: In8, i89: In8)

final case class Out(
    i1: Out.MaxNesting,
    i2: Out.MaxNesting,
    i3: Out.MaxNesting,
    i4: Out.MaxNesting,
    i5: Out.MaxNesting,
    i6: Out.MaxNesting
)
object Out {

  private var currentInt = 0
  private def int = {
    currentInt = currentInt + 1
    currentInt
  }
  private def in1 = In1(int)
  private def in2 = In2(in1, in1)
  private def in3 = In3(in2, in2, in2)
  private def in4 = In4(in3, in3, in3, in3)
  private def in5 = In5(in4, in4, in4, in4, in4)
  private def in6 = In6(in5, in5, in5, in5, in5, in5)
  private def in7 = In7(in6, in6, in6, in6, in6, in6, in6)
  private def in8 = In8(in7, in7, in7, in7, in7, in7, in7, in7)
  private def in9 = In9(in8, in8, in8, in8, in8, in8, in8, in8, in8)

  type MaxNesting = In5
  private def maxNesting = in5

  val example: Out =
    Out(maxNesting, maxNesting, maxNesting, maxNesting, maxNesting, maxNesting)

  def main(args: Array[String]): Unit = println(example)
}
