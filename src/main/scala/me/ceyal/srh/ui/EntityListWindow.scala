package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder
import com.googlecode.lanterna.gui2.table.{DefaultTableCellRenderer, Table}
import com.googlecode.lanterna.gui2.{BasicWindow, BorderLayout, Button, Container, Direction, GridLayout, Interactable, LinearLayout, Panels, SplitPanel, TextGUIGraphics, Window, WindowListenerAdapter}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.Dimensions
import me.ceyal.srh.data.Dimensions.Dimension
import me.ceyal.srh.data.components.{EntityWithDamageMonitor, HasDamageMonitor, HasEnemyId, HasEnemyLevel, HasInitiative, HasName}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.gear.Weapons.{DamageTypes, Physical, Stunning}
import me.ceyal.srh.random.Dices
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.ui.components.TableWithDetails
import me.ceyal.srh.ui.reactive.{ReactiveComponentTool, ReactiveValue}
import me.ceyal.srh.util.NamedGameEntity

import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava


class EntityListWindow(entitiesReactive: ReactiveValue[Seq[GameEntity]], title: Option[String] = None) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  val onChange = ReactiveValue.of(0)
  title.foreach(setTitle)

  // TODO make this table reactive somehow...
  val tbl = new TableWithDetails(
    reactiveRows = entitiesReactive,
    headers = Seq("#", "Nom", "Init", "Vie"), // TODO: more stuff!
    detailsPosition = Direction.HORIZONTAL,

    addDetailsPanel = false
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

    override def createRow(elem: GameEntity): Seq[String] = {
      val damages = elem.components[HasDamageMonitor].map { mon =>
        val damageMon = if (mon.maxValue == mon.currentValue) "xxx" else (mon.maxValue - mon.currentValue).toString
        mon.damageType match {
          case Some(DamageTypes.Stunning) => s"${damageMon}E"
          case Some(DamageTypes.Physical) => s"${damageMon}P"
          case None => damageMon
        }}.mkString(" / ")

      Seq(
        elem.componentOpt[HasEnemyId].map(_.id).getOrElse("?"),
        elem.name,
        elem.componentOpt[HasInitiative].flatMap(_.rolledValue).map(_.toString).getOrElse("?"),
        damages
      )
    }

    override def createDetailsBlock(value: ReactiveValue[GameEntity]): Container =
      EntityWindow.entityPanel(value, withFrame = true)
  }

  def rollInit(): Unit = {
    val dim = new ListSelectDialogBuilder[Dimension]()
      .addListItems(Dimensions.values.toSeq:_*)
      .setTitle("Initiative")
      .setDescription("Dans quelle dimension lancer l'initiative ?")
      .build().showDialog(getTextGUI)

    if (dim != null) {
      entitiesReactive.update(_.map(enr => enr.mapAll[HasInitiative](init => {
        val roll = Dices.launchDices(init.getDices(dim)).sum + init.initiative(enr)
        init.copy(rolledValue = Some(roll))
      })).sortBy(_.componentOpt[HasInitiative].flatMap(_.rolledValue).getOrElse(0)).reverse)
      onChange.update(_ + 1)
    }
  }

  val btnBar = Panels.horizontal(
    new Button("Initiative", () => rollInit())
  ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End))

  setComponent(
    Panels.vertical(
      Panels.horizontal(
        tbl.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow)),
        tbl.detailsPanel.setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow))
      ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Fill, LinearLayout.GrowPolicy.CanGrow)),
      btnBar
    )
  )

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
      if (key.getCharacter == 'q' || key.getKeyType == KeyType.Escape) {
        close()
      }
    }
  })


}
