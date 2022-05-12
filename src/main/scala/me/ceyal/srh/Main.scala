package me.ceyal.srh

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import me.ceyal.srh.ui.MainWindow

import scala.languageFeature.implicitConversions
import scala.util.Try

object Main extends App {

  private def app(): Unit = {
    gui.addWindowAndWait(new MainWindow(gui))
  }

  val _terminal = new DefaultTerminalFactory().createTerminal
  val screen = new TerminalScreen(_terminal)
  val gui = new MultiWindowTextGUI(screen)
  screen.startScreen()
  Try {
    app()
  }
  screen.stopScreen()

}
