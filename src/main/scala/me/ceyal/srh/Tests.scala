package me.ceyal.srh

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.gui2.dialogs.{ActionListDialogBuilder, MessageDialogBuilder}
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import me.ceyal.srh.ui.DiceRollTable

import java.util
import java.util.concurrent.atomic.AtomicBoolean
import scala.languageFeature.implicitConversions
import scala.util.Try

object Tests extends App {

  implicit def pairToTermPosition(pair: (Int, Int)): TerminalPosition =
    new TerminalPosition(pair._1, pair._2)

  private def guiApp(): Unit = {
    gui.addWindowAndWait(new BasicWindow("Lorem Ipsum") {
      setHints(util.Arrays.asList(Window.Hint.CENTERED))

      import com.googlecode.lanterna.TextColor
      import com.googlecode.lanterna.gui2.{Direction, LinearLayout}

      val contentPane = new Panel(new LinearLayout(Direction.VERTICAL))
      contentPane.addComponent(new Label("This is the first label"))
      contentPane.addComponent(new Label("This is the second label, red").setForegroundColor(TextColor.ANSI.RED))
      contentPane.addComponent(new Label("This is the last label\nSpanning\nMultiple\nRows"))
      contentPane.addComponent(new EmptySpace())
      contentPane.addComponent(DiceRollTable(13))
      contentPane.addComponent(new EmptySpace())
      contentPane.addComponent(new Table[Int]("FOR", "CON", "AGI", "REF", "HEALTH") {
        setSelectAction(() => {
          new MessageDialogBuilder()
            .setText("Selected row " + getSelectedRow)
            .setTitle("Selection").build().showDialog(gui)
        })

        getTableModel.addRow(5, 3, 4, 1, 12)
        getTableModel.addRow(5, 3, 4, 1, 10)

        override def handleKeyStroke(keyStroke: KeyStroke): Interactable.Result = {
          val parent = super.handleKeyStroke(keyStroke)

          if (parent == Interactable.Result.UNHANDLED) {
            keyStroke match {
              case ks if ks.getCharacter == 'd' =>
                getTableModel.setCell(4, getSelectedRow, getTableModel.getCell(4, getSelectedRow) - 1)
                Interactable.Result.HANDLED
              case ks if ks.getCharacter == 'h' =>
                getTableModel.setCell(4, getSelectedRow, getTableModel.getCell(4, getSelectedRow) + 1)
                Interactable.Result.HANDLED
              case _ => parent
            }
          } else parent
        }
      })
      contentPane.addComponent(new EmptySpace())


      val btnsPane = new Panel(new LinearLayout(Direction.HORIZONTAL))
      btnsPane.addComponent(new Button("Exit", () => close()))
      btnsPane.addComponent(new Button("Actions", () => {
        val result = new ActionListDialogBuilder()
          .addAction("Test", () => println("do stuff"))
          .addAction("Other test", () => println("do other stuff"))
          .setCanCancel(true)
          .build().showDialog(gui)

      }))

      contentPane.addComponent(btnsPane, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
      setComponent(contentPane)

      addWindowListener(new WindowListenerAdapter {
        override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
          if (key.getCharacter == 'q') {
            close()
          }
        }
      })
    })
  }

  private def app(): Unit = {
    val textGraphics = screen.newTextGraphics
    textGraphics.putString(10, 5, "Hello, Lanterna!")
    textGraphics.drawLine(0, 7, 50, 7, '┄')
    screen.refresh()

    var ks: KeyStroke = null
    do {
      ks = screen.readInput()
      textGraphics.putString(10, 15, ks.toString + " " * 20)
      screen.refresh()
    } while (ks.getCharacter != 'q')
  }

  val _terminal = new DefaultTerminalFactory().createTerminal
  val screen = new TerminalScreen(_terminal)
  val gui = new MultiWindowTextGUI(screen)
  screen.startScreen()
  Try {
    guiApp()
  }
  screen.stopScreen()

}
