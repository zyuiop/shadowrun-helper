package me.ceyal.srh

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, Terminal}

import scala.util.Try

object Main extends App {
  private def app(implicit terminal: Terminal): Unit = {
    terminal.setCursorPosition(10, 5)
    terminal.enableSGR(SGR.BORDERED)
    terminal.putString("Hello, Lanterna!")
    terminal.disableSGR(SGR.BORDERED)

    Thread.sleep(2000)
  }


  implicit val terminal: Terminal = new DefaultTerminalFactory().createTerminal()

  terminal.enterPrivateMode()
  Try { app }
  terminal.exitPrivateMode()

}
