package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.{BasicWindow, Window, WindowBasedTextGUI, WindowListenerAdapter}
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.dialogs.DialogWindow
import com.googlecode.lanterna.input.KeyStroke
import me.ceyal.srh.Main
import me.ceyal.srh.data.entities.BaseEnemies.Enemies

import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava

class MainWindow(gui: WindowBasedTextGUI) extends BasicWindow {
  setHints(List(Hint.FULL_SCREEN, Hint.NO_DECORATIONS, Hint.CENTERED).asJava)

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
      if (key.getCharacter == 'q') {
        close()
      } else if (key.getCharacter == 'r') {
        println(DiceRollTable.promptAndDialog(gui))
      } else if (key.getCharacter == 't') {
        // for "test"
        // gui.addWindowAndWait(new EntityWindow(Enemies.head))
        gui.addWindowAndWait(new EntityListWindow(Enemies))
      }
    }
  })
}
