package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.dialogs.{DialogWindow, ListSelectDialogBuilder, TextInputDialogBuilder, WaitingDialog}
import com.googlecode.lanterna.gui2.table.{DefaultTableCellRenderer, Table}
import com.googlecode.lanterna.gui2.{Button, Direction, EmptySpace, GridLayout, Interactable, LinearLayout, LocalizedString, Panel, TextGUIGraphics, WindowBasedTextGUI}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import com.googlecode.lanterna.{SGR, TextColor}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.HasSkills
import me.ceyal.srh.data.{AttrGetter, Attributs, SkillLevel}
import me.ceyal.srh.random.Dices

import java.util.regex.Pattern
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava}
import scala.util.Random

/**
 * A table that displays a dice-roll result and enables modifying it
 */
object DiceRollTable {
  trait DiceRollTable {
    /**
     * Returns the current number of successes
     *
     * @return
     */
    def countSuccesses: Int

    /**
     * Returns the current values for the dices
     *
     * @return
     */
    def values: Seq[Int]
  }

  def apply(dices: Int): Table[Int] with DiceRollTable = {
    val labels = ((1 to dices) map (d => s"d$d")).appended("Succ")

    new DiceRollTableImpl(dices, labels)
  }

  def dialogForSkills(gui: WindowBasedTextGUI, entity: AttrGetter, skills: HasSkills, skill: SkillLevel) = {
    val attr = if (skill.skill.alternativeStat.nonEmpty) {
      case class AttrStrPair(attr: Attributs.Attribut, desc: String) {
        override def toString: String = attr.abbr + ": " + desc
      }

      new ListSelectDialogBuilder[AttrStrPair]
        .addListItem(AttrStrPair(skill.skill.mainStat, "(par défaut)"))
        .addListItems(skill.skill.alternativeStat.map(pair => AttrStrPair(pair._2, pair._1)).toSeq:_*)
        .setTitle("Choix attribut")
        .setCanCancel(false)
        .build().showDialog(Main.gui).attr
    } else skill.skill.mainStat

    val bonus = if (skill.level == 0) -1 else if (skill.mastery.nonEmpty || skill.specialization.nonEmpty) {
      case class BonusAndDesc(desc: String, bonus: Int) {
        override def toString: String = s"$desc (+ ${bonus}d)"
      }

      var builder = new ListSelectDialogBuilder[BonusAndDesc]
        .addListItem(BonusAndDesc("Aucun", 0))

      if (skill.mastery.nonEmpty) builder = builder.addListItem(BonusAndDesc(skill.mastery.get + " (Maitrise)", 3))
      if (skill.specialization.nonEmpty) builder = builder.addListItem(BonusAndDesc(skill.specialization.get + " (Spécialisation)", 2))

      builder.setTitle("Choix spécialisation")
        .setCanCancel(false)
        .build().showDialog(gui).bonus
    } else 0

    DiceRollTable.dialog(gui, skills.dicesWithSkill(entity)(attr, skill.skill) + bonus)
  }

  def dialog(gui: WindowBasedTextGUI, dices: Int): Seq[Int] = new DialogWindow("Jets de dés") {
    setHints(Seq(Hint.CENTERED).asJava)

    val dtr = DiceRollTable(dices)
    val panel = new Panel(new LinearLayout(Direction.VERTICAL))
    panel.addComponent(new EmptySpace)
    panel.addComponent(dtr)
    panel.addComponent(new EmptySpace)
    panel.addComponent(new Button("Fermer", () => close()), LinearLayout.createLayoutData(LinearLayout.Alignment.End))

    setComponent(panel)

    override def handleInput(key: KeyStroke): Boolean = {
      if (key.getCharacter == 'q') {
        close()
        true
      } else super.handleInput(key)
    }

    override def showDialog(textGUI: WindowBasedTextGUI): Seq[Int] = {
      super.showDialog(textGUI)
      dtr.values
    }
  }.showDialog(gui)

  def promptAndDialog(gui: WindowBasedTextGUI): Seq[Int] = {
    val nd = new TextInputDialogBuilder()
      .setTitle("Combien de dés ?")
      .setValidationPattern(Pattern.compile("\\d{1,2}"), "Il me faut un nombre")
      .build()
      .showDialog(gui)

    if (nd != null && nd.nonEmpty) dialog(gui, nd.toInt)
    else Seq()
  }

  private class DiceRollTableImpl(dices: Int, labels: Seq[String]) extends Table[Int](labels: _*) with DiceRollTable {
    private val result: Seq[Int] = Dices.launchDices(dices)

    setCellSelection(true)

    def countSuccesses: Int = {
      val data = values

      if (data.count(_ < 0) > 0) -1 else data.count(_ > 4)
    }

    def values: Seq[Int] = getTableModel.getRow(0).asScala.dropRight(1).toSeq

    getTableModel.addRow(result.appended(0): _*)
    getTableModel.setCell(dices, 0, countSuccesses)

    setTableCellRenderer(new DefaultTableCellRenderer[Int]() {
      override def applyStyle(table: Table[Int], cellValue: Int, columnIndex: Int, rowIndex: Int, isSelected: Boolean, textGUIGraphics: TextGUIGraphics): Unit = {
        super.applyStyle(table, cellValue, columnIndex, rowIndex, isSelected, textGUIGraphics)

        if (cellValue < 0) {
          textGUIGraphics.setForegroundColor(TextColor.ANSI.MAGENTA)
        } else if (columnIndex < dices) {
          if (cellValue == 1 && !isSelected) {
            textGUIGraphics.setBackgroundColor(TextColor.ANSI.RED)
            textGUIGraphics.setForegroundColor(TextColor.ANSI.WHITE)
          }
          else if (cellValue < 5)
            textGUIGraphics.setForegroundColor(TextColor.ANSI.RED)
          else
            textGUIGraphics.setForegroundColor(TextColor.ANSI.GREEN)
        } else {
          textGUIGraphics.enableModifiers(SGR.BOLD)
        }
      }

      override def getContent(cell: Int): Array[String] = {
        if (cell < 0) Array("??")
        else super.getContent(cell)
      }
    })


    override def handleKeyStroke(keyStroke: KeyStroke): Interactable.Result = {
      if (getSelectedColumn < dices) {
        val cell = getSelectedColumn
        val model = getTableModel
        val value = model.getCell(cell, 0)

        def setValue(newValue: Int) = {
          if (newValue == 0 || newValue > 6) ()
          else {
            model.setCell(cell, 0, newValue)
            model.setCell(dices, 0, countSuccesses)
          }
        }

        keyStroke match {
          case ks if ks.getCharacter == 'd' || ks.getKeyType == KeyType.PageDown =>
            setValue(value - 1)
            Interactable.Result.HANDLED
          case ks if ks.getCharacter == 'i' || ks.getKeyType == KeyType.PageUp =>
            setValue(value + 1)
            Interactable.Result.HANDLED
          case ks if ks.getCharacter == 'r' =>
            setValue(-1)
            new Thread() {
              override def run(): Unit = {
                Thread.sleep(500)
                setValue(Dices.launchDices(1).head)
              }
            }.start()

            Interactable.Result.HANDLED
          case ks if ks.getCharacter != null && ks.getCharacter.charValue.isDigit && ks.getCharacter.charValue().asDigit < 7 && ks.getCharacter.charValue().asDigit > 0 =>
            setValue(ks.getCharacter.charValue().asDigit)
            Interactable.Result.HANDLED
          case _ => super.handleKeyStroke(keyStroke)
        }
      } else super.handleKeyStroke(keyStroke)
    }

  }
}
