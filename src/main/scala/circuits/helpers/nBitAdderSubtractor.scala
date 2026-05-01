package sysarch.circuits.helpers

import sysarch.chisel._
import sysarch.gates._
import sysarch.circuits.helpers._

class nBitAdderSubtractor(width: Int) extends Module {
  val a          = IO(Input(Vec(width, Bool())))
  val b          = IO(Input(Vec(width, Bool())))
  val enable_sub = IO(Input(Bool()))
  val sum        = IO(Output(Vec(width, Bool())))
  val cout       = IO(Output(Bool()))

  val coutXor = Module(new XORGate)
  val nAdders = Seq.fill(width)(Module(new FullAdder)) // 8bit adders togeyher
  val MynNot  = Module(new nBitNOT(width))
  val MyMux   = Module(new Mux(width))                 // one complete mux bux

  MynNot.a := b // inverted b

  MyMux.a   := b
  MyMux.b   := MynNot.out
  MyMux.sel := enable_sub // select on basis on enable sub

  for (i <- 0 until width) {

    if (i == 0) {
      nAdders(i).cin := enable_sub
    } else {
      nAdders(i).cin := nAdders(i - 1).cout
    } // to choose for i=0 becuae for that only cin is from input and for next it will cin =cout from previous

    nAdders(i).a := a(i)
    nAdders(i).b := MyMux.out(i)

    sum(i) := nAdders(i).sum

  }
  coutXor.a := nAdders(
    width - 1
  ).cout // becuase for subtraction in mathematich notaion we borrow from front bit so if there is carry that means we have borrowed becz in 2's complement we get get opposite of our bit  so we have to make carry 0
  // carry 1 sub 1 - cout 0 ,carry1 sub 0 foor addition cout  =1
  coutXor.b := enable_sub
  cout      := coutXor.out

}
