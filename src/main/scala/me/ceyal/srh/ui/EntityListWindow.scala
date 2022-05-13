package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder
import com.googlecode.lanterna.gui2.{BasicWindow, BorderLayout, Button, Container, Direction, GridLayout, Interactable, LinearLayout, Panels, Window, WindowListenerAdapter}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.Dimensions
import me.ceyal.srh.data.Dimensions.Dimension
import me.ceyal.srh.data.components.{EntityWithDamageMonitor, HasEnemyLevel, HasInitiative, HasName}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.gear.Weapons.{Physical, Stunning}
import me.ceyal.srh.random.Dices
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.ui.components.TableWithDetails
import me.ceyal.srh.ui.reactive.ReactiveValue
import me.ceyal.srh.util.NamedGameEntity

import java.util.concurrent.atomic.AtomicBoolean
import scala.jdk.CollectionConverters.SeqHasAsJava


class EntityListWindow(entities: Seq[ReactiveValue[GameEntity]], title: Option[String] = None) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  val onChange = ReactiveValue.of(0)

  title.foreach(setTitle)

  // TODO make this table reactive somehow...
  val tbl = onChange ==> (_ => new TableWithDetails(
    rows = entities,
    headers = Seq("Nom", "Initiative"), // TODO: more stuff!
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

    override def createRow(elem: ReactiveValue[GameEntity]): Seq[String] =
      Seq(elem.get.name, elem.get.componentOpt[HasInitiative].flatMap(_.rolledValue).map(_.toString).getOrElse("?"))

    override def createDetailsBlock(value: ReactiveValue[GameEntity]): Container =
      EntityWindow.entityPanel(value, withFrame = true)
  })

  def rollInit(): Unit = {
    val dim = new ListSelectDialogBuilder[Dimension]()
      .addListItems(Dimensions.values.toSeq:_*)
      .setTitle("Initiative")
      .setDescription("Dans quelle dimension lancer l'initiative ?")
      .build().showDialog(getTextGUI)

    if (dim != null) {
      entities.foreach(enr => enr.update(en => en.mapAll[HasInitiative](init => {
        val roll = Dices.launchDices(init.getDices(dim)).sum + init.initiative(en)
        init.copy(rolledValue = Some(roll))
      })))
      onChange.update(_ + 1)
    }
  }

  val btnBar = Panels.horizontal(
    new Button("Initiative", () => rollInit())
  ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End))

  setComponent(
    Panels.vertical(
      tbl, btnBar
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
