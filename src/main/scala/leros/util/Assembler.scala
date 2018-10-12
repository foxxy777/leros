package leros.util

object Assembler {

  val prog = Array[Int](
    0x0903, // addi 0x3
    0x09ff, // -1
    0x0d02, // subi 2
    0x21ab, // ldi 0xab
    0x230f, // and 0x0f
    0x25c3, // or 0xc3
    0x0000
  )

  // collect destination addresses in first pass
  val symbols = collection.mutable.Map[String, Int]()

  def getProgramFix() = prog

  def getProgram() = {
    if (true)
      getProgramFix()
    else {
      // FERTL does not like large vectors
      val prog = new Array[Int](200)
      for (i <- 0 until prog.length) {
        prog(i) = ((i << 8) + i+1) & 0xffff
      }
      prog
    }
  }
}
