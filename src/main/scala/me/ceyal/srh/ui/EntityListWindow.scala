package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.{BasicWindow, Direction, Panels, Window, WindowListenerAdapter}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.{HasEnemyLevel, HasName}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.util.NamedGameEntity

import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava


class EntityListWindow(entities: Seq[GameEntity]) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  val tbl = new TableWithDetails(
    rows = entities,
    headers = Seq("Nom"), // TODO: more stuff!
    rowMapperLambda = (elem: GameEntity) => Seq(elem.name),
    detailsMapperLambda = (elem: GameEntity) => EntityWindow.entityPanel(elem, withFrame = true),
    detailsPosition = Direction.HORIZONTAL
  ) {
    override def onSelect(selected: GameEntity): Unit =
      Main.gui.addWindowAndWait(new EntityWindow(selected))
  }

  setComponent(Panels.vertical(tbl))

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
      if (key.getCharacter == 'q' || key.getKeyType == KeyType.Escape) {
        close()
      }
    }
  })


}
