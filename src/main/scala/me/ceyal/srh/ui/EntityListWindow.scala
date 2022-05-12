package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.{BasicWindow, Direction, Interactable, Panels, Window, WindowListenerAdapter}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.{EntityWithDamageMonitor, HasEnemyLevel, HasName}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.gear.Weapons.{Physical, Stunning}
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.ui.reactive.ReactiveValue
import me.ceyal.srh.util.NamedGameEntity

import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava


class EntityListWindow(entities: Seq[ReactiveValue[GameEntity]]) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  val tbl = new TableWithDetails(
    rows = entities,
    headers = Seq("Nom"), // TODO: more stuff!
    rowMapperLambda = (elem: ReactiveValue[GameEntity]) => Seq(elem.get.name),
    detailsMapperLambda = (elem: ReactiveValue[GameEntity]) => EntityWindow.entityPanel(elem, withFrame = true),
    detailsPosition = Direction.HORIZONTAL
  ) {
    override def onSelect(selected: ReactiveValue[GameEntity]): Unit =
      Main.gui.addWindowAndWait(new EntityWindow(selected))

    override def keyHandler(key: KeyStroke, selected: ReactiveValue[GameEntity]): Interactable.Result = {
      if (key.getCharacter == 'd') {
        selected.update(_.damage(1, if (key.isAltDown) Stunning else Physical))
        Interactable.Result.HANDLED
      } else if (key.getCharacter == 'h') {
        selected.update(_.heal(1, if (key.isAltDown) Stunning else Physical))
        Interactable.Result.HANDLED
      } else Interactable.Result.UNHANDLED

    }
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
