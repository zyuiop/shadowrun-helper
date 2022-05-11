package me.ceyal.tuitui

import org.fusesource.jansi.Ansi

trait Box {
  val upLeft: String
  val upRight: String
  val downLeft: String
  val downRight: String
  val vertical: String
  val horizontal: String

  val verticalSplitLeft: String
  val verticalSplitRight: String
  val horizontalSplitUp: String
  val horizontalSplitDown: String

  val cross: String


  import Box._
}

object Box {
  private def box(str: String): Box = new Box {
    override val upLeft: String = str(0).toString
    override val upRight: String = str(1).toString
    override val downLeft: String = str(2).toString
    override val downRight: String = str(3).toString

    override val vertical: String = str(4).toString
    override val horizontal: String = str(5).toString

    override val verticalSplitLeft: String = str(6).toString
    override val verticalSplitRight: String = str(7).toString
    override val horizontalSplitDown: String = str(8).toString
    override val horizontalSplitUp: String = str(9).toString

    override val cross: String = str(10).toString
  }

  val Simple: Box = box("┌┐└┘│─├┤┬┴┼")
  val Double: Box = box("╔╗╚╝║═╠╣╦╧╬")
}
