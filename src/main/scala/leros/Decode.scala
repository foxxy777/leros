package leros

import chisel3._
import chisel3.util._

import leros.shared.Constants._
import leros.Types._
//解码器输出
class DecodeOut extends Bundle {
  val ena = Bool()
  val op = UInt()
  val isRegOpd = Bool()
  val isStore = Bool()
  val isStoreInd = Bool()
  val isLoadInd = Bool()
  val isLoadAddr = Bool()
  val imm = Bool()
  val enahi = Bool()
  val enah2i = Bool()
  val enah3i = Bool()
  val nosext = Bool()
  val exit = Bool()
}
//用于给输出设置默认值
object DecodeOut {
  def default: DecodeOut = {
    val v = Wire(new DecodeOut)
    v.ena := false.B
    v.op := nop
    v.isRegOpd := false.B
    v.isStore := false.B
    v.isStoreInd := false.B
    v.isLoadInd := false.B
    v.isLoadAddr := false.B
    v.imm := false.B
    v.enahi := false.B
    v.enah2i := false.B
    v.enah3i := false.B
    v.nosext := false.B
    v.exit := false.B
    v
  }
}

class Decode() extends Module {
  val io = IO(new Bundle {
    val din = Input(UInt(8.W))
    val dout = Output(new DecodeOut)
  })

  val d = DecodeOut.default

  // Branch uses only 4 bits for decode
  val isBranch = WireDefault(false.B)
  /* this is broken, why?
  switch (io.din >> 4.U) {
    is (BR.U >> 4.U) { isBranch := true.B }
    is (BRZ.U >> 4.U) { isBranch := true.B }
    is (BRNZ.U >> 4.U) { isBranch := true.B }
    is (BRP.U >> 4.U) { isBranch := true.B }
    is (BRN.U >> 4.U) { isBranch := true.B }
  }
   */
   //0X0f 0000_1111 所以这个mask就是把br指令有效的左4位取出来
   //感觉这个与有点多余，难道这里还会有x/z吗
  def mask(i: Int) = ((i >> 4) & 0x0f).asUInt
/*
  val BR = 0x80   1000_0000
  val BRZ = 0x90  1001_0000
  val BRNZ = 0xa0 1010_0000
  val BRP = 0xb0  1011_0000
  val BRN = 0xc0  1100_0000
*/
// 76543210 
// ————     (7,4)就是头4位 
  val field = io.din(7, 4)
  when (field === mask(BR)) { isBranch := true.B }
  when (field === mask(BRZ)) { isBranch := true.B }
  when (field === mask(BRNZ)) { isBranch := true.B }
  when (field === mask(BRP)) { isBranch := true.B }
  when (field === mask(BRN)) { isBranch := true.B }
//BRANCH_MASK 0xf0 1111_0000
//如果是branch类指令,把din的头4位(也就是branch指令内容)作为instr,如果不是,把整个din作为instr
  val instr = Mux(isBranch, io.din & BRANCH_MASK.U, io.din)

  switch(instr) {
    is(ADD.U) {
      d.op := add
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(ADDI.U) {
      d.op := add
      d.imm := true.B
      d.ena := true.B
    }
    is(SUB.U) {
      d.op := sub
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(SUBI.U) {
      d.op := sub
      d.imm := true.B
      d.ena := true.B
    }
    is(SHR.U) {
      d.op := shr
      d.ena := true.B
    }
    is(LD.U) {
      d.op := ld
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(LDI.U) {
      d.op := ld
      d.imm := true.B
      d.ena := true.B
    }
    is(AND.U) {
      d.op := and
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(ANDI.U) {
      d.op := and
      d.imm := true.B
      d.ena := true.B
      d.nosext := true.B
    }
    is(OR.U) {
      d.op := or
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(ORI.U) {
      d.op := or
      d.imm := true.B
      d.ena := true.B
      d.nosext := true.B
    }
    is(XOR.U) {
      d.op := xor
      d.ena := true.B
      d.isRegOpd := true.B
    }
    is(XORI.U) {
      d.op := xor
      d.imm := true.B
      d.ena := true.B
      d.nosext := true.B
    }
    is(LDHI.U) {
      d.op := ld
      d.imm := true.B
      d.ena := true.B
      d.enahi := true.B
    }
    // Following only useful for 32-bit Leros
    is(LDH2I.U) {
      d.op := ld
      d.imm := true.B
      d.ena := true.B
      d.enah2i := true.B
    }
    is(LDH3I.U) {
      d.op := ld
      d.imm := true.B
      d.ena := true.B
      d.enah3i := true.B
    }
    is (ST.U) {
      d.isStore := true.B
    }
    is (LDADDR.U) {
      d.isLoadAddr := true.B
    }
    is (LDIND.U) {
      d.isLoadInd := true.B
      d.op := ld
      d.ena := true.B
    }
    is (LDINDBU.U) {
      // TODO byte enable
      d.isLoadInd := true.B
      d.op := ld
      d.ena := true.B
    }
    is (STIND.U) {
      d.isStoreInd := true.B
    }
    is (STINDB.U) {
      // TODO byte enable
      d.isStoreInd := true.B
    }
    is(SCALL.U) {
      d.exit := true.B
    }
  }
  io.dout := d
}
