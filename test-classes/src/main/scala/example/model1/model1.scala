package example.model1

final case class In1(int: Int)
final case class In2(i11: In1, i12: In1)
final case class In3(i21: In2, i22: In2, i23: In2)
final case class In4(i31: In3, i32: In3, i33: In3, i34: In3)
final case class In5(i41: In4, i42: In4, i43: In4, i44: In4, i45: In4)

final case class Out(
    i1: In5,
    i2: In5,
    i3: In5,
    i4: In5,
    i5: In5,
    i6: In5
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

  val example: Out =
    Out(in5, in5, in5, in5, in5, in5)

  def main(args: Array[String]): Unit = println(example)
}
