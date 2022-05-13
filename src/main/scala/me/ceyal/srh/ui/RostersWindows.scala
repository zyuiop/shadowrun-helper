package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder
import com.googlecode.lanterna.gui2.{BasicWindow, Button, Container, Interactable, LinearLayout, Panels, Window, WindowListener, WindowListenerAdapter}
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.HasName
import me.ceyal.srh.data.repositories
import me.ceyal.srh.data.repositories.{Roster, RosterEnemy}
import me.ceyal.srh.ui.reactive.{ReactiveComponentTool, ReactiveValue}
import play.api.libs.json.JsSuccess

import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

object RostersWindows {
  private val repo = repositories.RostersRepository

  case class ModifyRosterWindow(roster: ReactiveValue[Roster]) extends BasicWindow {
    val tbl = roster ==> (r => new Table[String]("Fichier", "Qté") {
      r.foreach(enemy => getTableModel.addRow(enemy.path.split("/").last, enemy.quantity.toString))

      override def handleKeyStroke(keyStroke: KeyStroke): Interactable.Result = {
        val parent = super.handleKeyStroke(keyStroke)
        if (parent == Interactable.Result.UNHANDLED) {
          val row = getSelectedRow
          if (keyStroke.getCharacter == 'r' || keyStroke.getKeyType == KeyType.Delete) {
            roster.update(lst => lst.take(row) ::: lst.drop(row + 1))
            Interactable.Result.HANDLED
          } else if (keyStroke.getCharacter == 'd' || keyStroke.getCharacter == '-') {
            val current = r(getSelectedRow)
            val next = current.copy(quantity = current.quantity - 1)

            if (next.quantity < 1)
              roster.update(lst => lst.take(row) ::: lst.drop(row + 1))
            else
              roster.update(lst => lst.take(row) ::: next :: lst.drop(row + 1))
            Interactable.Result.HANDLED
          } else if (keyStroke.getCharacter == 'i' || keyStroke.getCharacter == '+') {
            val current = r(getSelectedRow)
            val next = current.copy(quantity = current.quantity + 1)
            roster.update(lst => lst.take(row) ::: next :: lst.drop(row + 1))
            Interactable.Result.HANDLED
          } else {
            Interactable.Result.UNHANDLED
          }
        } else parent
      }
    })

    addWindowListener(new WindowListenerAdapter {
      override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
        if (key.getCharacter == 'q' || key.getKeyType == KeyType.Escape) {
          close()
        } else if (key.getCharacter == 'a') {
          add()
        }
      }
    })

    def add(): Unit = {
      repositories.EnemiesRepository.load(Main.gui) match {
        case Some((JsSuccess(e, _), f)) =>
          val path = f.getAbsolutePath
          val amt = new TextInputDialogBuilder()
            .setTitle("Combien ?")
            .setDescription(s"Combien de ${e.componentOpt[HasName].map(_.name).getOrElse("Ennemi Sans Nom")} voulez vous ajouter à ce roster?")
            .setValidationPattern(Pattern.compile("\\d{1,2}"), "Il me faut un nombre")
            .build()
            .showDialog(Main.gui)

          if (amt != null && amt.nonEmpty) {
            roster.update(current => {
              // If exists, update quantity
              if (current.exists(_.path == path))
                current.map(e => { if (e.path == path) e.copy(quantity = e.quantity + amt.toInt) else e })
              else
                current.appended(RosterEnemy(path, amt.toInt))
            })
          }

        case _ => ()

      }
    }

    val buttons = Panels.horizontal(
      new Button("Ajouter ennemi", () => add()),
      new Button("Fermer", () => close())
    ).setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.End))

    setTitle("Modifier un roster")
    setComponent(Panels.vertical(
      tbl, buttons
    ))
  }

  def createRoster(): Unit =
    repo.saveAndAutosave(List(), Main.gui) match {
      case Some(roster) => Main.gui.addWindowAndWait(ModifyRosterWindow(roster))
      case None => ()
    }

  def openRoster: Option[ReactiveValue[Roster]] = repo.loadWithAutosave(Main.gui)

  def openAndModifyRoster(): Unit = openRoster match {
    case Some(roster) => Main.gui.addWindowAndWait(ModifyRosterWindow(roster))
    case None => ()
  }
}