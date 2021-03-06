package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.{BasicWindow, Window, WindowBasedTextGUI, WindowListenerAdapter}
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.dialogs.DialogWindow
import com.googlecode.lanterna.gui2.menu.MenuItem
import com.googlecode.lanterna.input.KeyStroke
import me.ceyal.srh.Main
import me.ceyal.srh.data.entities.BaseEnemies.Enemies
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.repositories.{EnemiesRepository, RostersRepository, Scene, ScenesRepository}
import me.ceyal.srh.ui.components.{MenuBarWithShortcuts, MenuWithShortcuts}
import me.ceyal.srh.ui.reactive.ReactiveValue
import play.api.libs.json.JsSuccess

import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava

class MainWindow(gui: WindowBasedTextGUI) extends BasicWindow {
  setHints(List(Hint.FULL_SCREEN, Hint.NO_DECORATIONS, Hint.CENTERED).asJava)

  private def openScene(scene: ReactiveValue[Scene]) = {
    val entities: ReactiveValue[Seq[GameEntity]] = scene.map(_.entities, (o, nv) => o.copy(entities = nv))
    gui.addWindowAndWait(new EntityListWindow(entities, Some("Vue scène")))
  }

  private def sceneFromRoster(): Unit =
    RostersRepository.load(gui) match {
      case Some((JsSuccess(roster, _), _)) =>
        ScenesRepository.saveAndAutosave(Scene.fromRoster(roster), gui) match {
          case Some(scene) => openScene(scene)
          case None => ()
        }
      case other => println("Error: " + other)
    }

  private def loadOpenScene(): Unit =
    ScenesRepository.loadWithAutosave(gui) match {
      case Some(scene) => openScene(scene)
      case other => println("Error: " + other)
    }

  setMenuBar(
    new MenuBarWithShortcuts {
      add(new MenuWithShortcuts("Enemies") {

      })
      add(new MenuWithShortcuts("Rosters") {
        add(new MenuItem("Create", () => RostersWindows.createRoster()))
        add(new MenuItem("Modify", () => RostersWindows.openAndModifyRoster()))
      })
      add(new MenuWithShortcuts("Scenes") {
        add(new MenuItem("Create from Roster", () => sceneFromRoster()))
        add(new MenuItem("Open", () => loadOpenScene()))
      })
    }
  )

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = if (key.isAltDown) {
      getMenuBar.handleInput(key)
    } else {
      if (key.getCharacter == 'q') {
        close()
      } else if (key.getCharacter == 'r') {
        println(DiceRollTable.promptAndDialog(gui))
      } else if (key.getCharacter == 'o' && key.isCtrlDown) {
        loadOpenScene()
      }else if (key.getCharacter == 'n' && key.isCtrlDown) {
        sceneFromRoster()
      }
    }
  })
}
