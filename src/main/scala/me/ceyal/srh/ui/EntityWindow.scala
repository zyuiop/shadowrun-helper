package me.ceyal.srh.ui

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.gui2.Interactable.Result
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.{HasMagic, HasSkills}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.spells.Spell
import me.ceyal.srh.data.{AttrBlock, Attributs, SkillLevel}
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.util.{NamedGameEntity, foldText}

import java.util.concurrent.atomic.AtomicBoolean
import scala.collection.mutable
import scala.jdk.CollectionConverters.SeqHasAsJava

object EntityWindow {
  def attributeTable(attrs: AttrBlock) = {
    val mapped = attrs.filterNot(_._1 == Attributs.DésInitiative).map {
      case (k, v) if k == Attributs.Essence => k -> (v.toDouble / 1000).toString
      case (k, v) => k -> v.toString
    }.toSeq.sortBy(_._1.id)

    val names = mapped.map(_._1.abbr)
    new Table[String](names: _*) {
      getTableModel.addRow(mapped.map(_._2).asJava)
    }
  }

  def skillsTable(entity: GameEntity, skills: HasSkills) = {
    val launchDices = (skillLevel: SkillLevel) => {
      DiceRollTable.dialogForSkills(Main.gui, entity, skills, skillLevel)
      ()
    }

    new TableWithDetails[SkillLevel](skills.skills.values.toSeq, Seq("Nom", "Niveau", "Attribut", "Dés")) {
      override def onSelect(selected: SkillLevel): Unit = launchDices(selected)

      override def keyHandler(key: KeyStroke, selected: SkillLevel): Result = {
        if (key.getCharacter == 'r') {
          launchDices(selected)
          Result.HANDLED
        } else Result.UNHANDLED
      }

      override def createRow(skill: SkillLevel): Seq[String] =
        Seq(skill.skill.name, skill.level.toString, skill.skill.mainStat.abbr, skills.dicesForSkill(entity)(skill.skill).toString)

      override def createDetailsBlock(skill: SkillLevel): Container = {
        val altStats: Seq[String] = {
          val s: Seq[String] = skill.skill.alternativeStat.toSeq.map { case (usecase, attr) => s"${attr.abbr}: $usecase" }

          if (s.nonEmpty) s.prepended("Attributs alternatifs:")
          else s
        }
        val lines: Iterable[String] = skill.specialization.map(spe => s"Spécialisation (+2d): $spe") ++ skill.mastery.map(mas => s"Maîtrise (+3d): $mas") ++ altStats
        val labels = lines.map(s => new Label(foldText(s, table.getPreferredSize.getColumns))).toSeq
        Panels.vertical(labels: _*)
      }
    }
  }


  def magicTable(entity: GameEntity, magic: HasMagic) = {
    new TableWithDetails[Spell](magic.spells, Seq("Nom", "Type", "Portée", "Durée", "Drain")) {
      // override def onSelect(selected: SkillLevel): Unit = launchDices(selected)

      def drainDamage(spell: Spell) = {
        val dices = magic.drainReserve(entity)
        val successes = DiceRollTable.dialog(Main.gui, dices).count(_ > 4)
        val reducedDrain = Math.max(0, spell.drain - successes)

        val dmgType = if (reducedDrain > entity.attr(Attributs.Magie)) "physiques" else "étourdissants"

        // TODO: directly apply damages?
        new MessageDialogBuilder().setTitle("Drain").setText(s"Drain [${spell.drain}] - Dés [$successes] = $reducedDrain dégats $dmgType").build().showDialog(Main.gui)
      }

      override def keyHandler(key: KeyStroke, selected: Spell): Result = {
        if (key.getCharacter == 'r') {
          // launchDices(selected)
          Result.HANDLED
        } else if (key.getCharacter == 'd') {
          drainDamage(selected)
          Result.HANDLED
        } else Result.UNHANDLED
      }

      override def createRow(spell: Spell): Seq[String] =
        Seq(spell.name, spell.spellType.toString, spell.range.toString, spell.duration.toString, spell.drain.toString)

      override def createDetailsBlock(spell: Spell): Container = {
        Panels.vertical(
          new Label("Détails du sort:").addStyle(SGR.BOLD).addStyle(SGR.UNDERLINE),
          new Label(foldText(spell.description, table.getPreferredSize.getColumns))

          // TODO: add kind specific spell details
        )
      }
    }
  }

  def entityPanel(entity: GameEntity, withFrame: Boolean = false): Container = {
    val shortcuts = mutable.Map[Char, Container]()

    def withShortcut(char: Char, container: Container) = {
      shortcuts.put(char, container)
      container
    }

    val panel = new Panel(new LinearLayout(Direction.VERTICAL).setSpacing(1)) {
      override def handleInput(key: KeyStroke): Boolean = {
        if (!super.handleInput(key)) {
          if (shortcuts.contains(key.getCharacter.charValue())) {
            val focus = shortcuts(key.getCharacter.charValue()).nextFocus(null)

            if (focus != null) {
              focus.takeFocus()
              true
            } else false
          } else false
        } else true
      }
    }

    panel addComponent withShortcut('a', attributeTable(entity.attributes).withBorder(Borders.singleLine("Attributs")))
      .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))

    entity.components[HasSkills].foreach(skillComponent => {
      panel addComponent withShortcut('c',  skillsTable(entity, skillComponent).withBorder(Borders.singleLine("Compétences")))
        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
    })

    entity.components[HasMagic].foreach(magicComponent => {
      panel addComponent withShortcut('m', magicTable(entity, magicComponent).withBorder(Borders.singleLine(s"Magie (${magicComponent.tradition})")))
        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
    })

    if (withFrame) panel.withBorder(Borders.singleLine(entity.name))
    else panel
  }
}

class EntityWindow(entity: GameEntity) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  // Determine the title
  setTitle(entity.name)

  setComponent(entityPanel(entity))

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
      if (key.getCharacter == 'q' || key.getKeyType == KeyType.Escape) {
        close()
      }
    }
  })


}
