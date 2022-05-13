package me.ceyal.srh

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.{DefaultTerminalFactory, MouseCaptureMode}
import me.ceyal.srh.data.components.{HasEnemyLevel, HasName}
import me.ceyal.srh.data.entities.BaseEnemies
import me.ceyal.srh.data.repositories.basepath
import me.ceyal.srh.ui.MainWindow
import play.api.libs.json.Json

import java.io.{File, FileOutputStream}
import scala.languageFeature.implicitConversions
import scala.util.Try

object Main extends App {

  private def app(): Unit = {
    gui.addWindowAndWait(new MainWindow(gui))
  }

  // Save default enemies
  def copyDefaultEnemies() = {
    val f = new File(new File(basepath, "enemies"), "default")


    BaseEnemies.Enemies
      .filter(e => e.componentOpt[HasName].isDefined && e.componentOpt[HasEnemyLevel].isDefined)
      .map(e => {
        (e.component[HasEnemyLevel].profLevel + "-" + e.component[HasName].name).replaceAll("[^a-zA-Z0-9]+", "-") + ".json" -> Json.prettyPrint(Json.toJson(e)).getBytes("utf-8")
      })
      .foreach { case (name, content) =>
        val os = new FileOutputStream(new File(f, name))
        try {
          os.write(content)
        } finally os.close()
      }
  }

  val _terminal = new DefaultTerminalFactory().setMouseCaptureMode(MouseCaptureMode.CLICK).createTerminal
  val screen = new TerminalScreen(_terminal)
  val gui = new MultiWindowTextGUI(screen)
  screen.startScreen()
  Try {
    copyDefaultEnemies()
    app()
  }
  screen.stopScreen()

}
