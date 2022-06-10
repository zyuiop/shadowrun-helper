package me.ceyal.srh.ui

import com.googlecode.lanterna.SGR
import com.googlecode.lanterna.gui2.Interactable.Result
import com.googlecode.lanterna.gui2.Window.Hint
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.gui2.dialogs.{ListSelectDialogBuilder, MessageDialogBuilder, MessageDialogButton}
import com.googlecode.lanterna.gui2.table.Table
import com.googlecode.lanterna.input.{KeyStroke, KeyType}
import me.ceyal.srh.Main
import me.ceyal.srh.data.components.{EntityWithDamageMonitor, HasDamageMonitor, HasInventory, HasMagic, HasSkills}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.gear.Weapons.{Physical, Ranges, Stunning, Weapon}
import me.ceyal.srh.data.spells.Spell
import me.ceyal.srh.data.{AttrBlock, Attributs, SkillLevel}
import me.ceyal.srh.ui.EntityWindow.entityPanel
import me.ceyal.srh.ui.components.TableWithDetails
import me.ceyal.srh.ui.reactive.ReactiveValue
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

    new TableWithDetails(ReactiveValue.immutable(skills.skills.values.toSeq), Seq("Nom", "Niveau", "Attribut", "Dés")) {
      override def onSelect(selected: ReactiveValue[SkillLevel]): Unit = launchDices(selected.get)

      override def keyHandler(key: KeyStroke, selected: ReactiveValue[SkillLevel]): Result = {
        if (key.getCharacter == 'r') {
          launchDices(selected.get)
          Result.HANDLED
        } else Result.UNHANDLED
      }

      override def createRow(skill: SkillLevel): Seq[String] =
        Seq(skill.skill.name, skill.level.toString, skill.skill.mainStat.abbr, skills.dicesForSkill(entity)(skill.skill).toString)

      override def createDetailsBlock(skillRV: ReactiveValue[SkillLevel]): Container = {
        val skill = skillRV.get

        val altStats: Seq[String] = {
          val s: Seq[String] = skill.skill.alternativeStat.toSeq.map { case (usecase, attr) => s"${attr.abbr}: $usecase" }

          if (s.nonEmpty) s.prepended("Attributs alternatifs:")
          else s
        }
        val lines: Iterable[String] = skill.specialization.map(spe => s"Spécialisation (+2d): $spe") ++ skill.mastery.map(mas => s"Maîtrise (+3d): $mas") ++ altStats
        val labels: Seq[Component] = lines.map(s => new Label(foldText(s, table.getPreferredSize.getColumns))).toSeq
        Panels.vertical(labels prepended new EmptySpace: _*)
      }
    }
  }


  def magicTable(reactiveEntity: ReactiveValue[GameEntity], magic: HasMagic) = {
    new TableWithDetails[Spell](ReactiveValue immutable magic.spells, Seq("Nom", "Type", "Portée", "Durée", "Drain")) {
      // override def onSelect(selected: SkillLevel): Unit = launchDices(selected)

      def drainDamage(spell: Spell) = {
        val dices = magic.drainReserve(reactiveEntity.get)
        val successes = DiceRollTable.dialog(Main.gui, dices).count(_ > 4)
        val reducedDrain = Math.max(0, spell.drain - successes)

        val dmgType = if (reducedDrain > reactiveEntity.get.attr(Attributs.Magie)) Physical else Stunning

        val applDmg = "Encaisser les dégats"
        // TODO: directly apply damages?
        val result = new ListSelectDialogBuilder[String].setTitle("Drain")
          .setDescription(s"Drain [${spell.drain}] - Dés [$successes] = $reducedDrain dégats $dmgType")
          .addListItem(applDmg)
          .addListItem("Fermer")
          .build()
          .showDialog(Main.gui)

        if (result == applDmg && reducedDrain > 0) {
          reactiveEntity.update(_.damage(reducedDrain, dmgType))
        }
      }

      override def keyHandler(key: KeyStroke, selected: ReactiveValue[Spell]): Result = {
        if (key.getCharacter == 'r') {
          // launchDices(selected)
          Result.HANDLED
        } else if (key.getCharacter == 'd') {
          drainDamage(selected.get)
          Result.HANDLED
        } else Result.UNHANDLED
      }

      override def onSelect(selected: ReactiveValue[Spell]): Unit = drainDamage(selected.get)

      override def createRow(spell: Spell): Seq[String] =
        Seq(spell.name, spell.spellType.toString, spell.range.toString, spell.duration.toString, spell.drain.toString)

      override def createDetailsBlock(spell: ReactiveValue[Spell]): Container = {
        Panels.vertical(
          new EmptySpace(),
          new Label("Détails du sort:").addStyle(SGR.BOLD).addStyle(SGR.UNDERLINE),
          new Label(foldText(spell.get.description, table.getPreferredSize.getColumns))

          // TODO: add kind specific spell details
        )
      }
    }
  }

  def weaponsTable(reactiveEntity: ReactiveValue[GameEntity]) = {
    val weapons: ReactiveValue[Seq[Weapon]] = reactiveEntity.map(_.component[HasInventory].weapons.toSeq, (e, w) => e.mapAll[HasInventory](i => i.updateWeapons(w.toList)))

    new TableWithDetails[Weapon](weapons, Seq("Nom", "Compétence", "SO", "Dégats")) {
      // override def onSelect(selected: SkillLevel): Unit = launchDices(selected)

      def attack(w: Weapon) = {
        val range = RangeDialog(Main.gui, "Choisir la portée de l'attaque")

        if (range.isDefined) {
          val so = w.atkScore(range.get, reactiveEntity.get)
          new MessageDialogBuilder().setTitle("Attribution de l'atout")
            .setText("Atout à l'attaquant si SD <= " + (so - 4) + "\nAtout au défenseur si SD >= " + (so + 4))
            .build().showDialog(Main.gui)

          val atkDices = reactiveEntity.get.component[HasSkills].dicesForSkill(reactiveEntity.get)(w.baseSkill, w.usageSpecialization)

          DiceRollTable.dialog(Main.gui, atkDices, "Attaque (dégats " + w.damageValue(reactiveEntity.get) + w.damageType.abridged + w.hitEffects.mkString + ")")
        }
      }

      override def keyHandler(key: KeyStroke, selected: ReactiveValue[Weapon]): Result = {
        // TODO
        Result.UNHANDLED
      }

      override def onSelect(selected: ReactiveValue[Weapon]): Unit = attack(selected.get)

      override def createRow(weapon: Weapon): Seq[String] = {
        Seq(weapon.name,
          weapon.baseSkill + weapon.usageSpecialization.map(sp => s" (${sp.toString})").getOrElse(""),
          Ranges.values.toList.sortBy(_.id).map(v => weapon.atkScore(v, reactiveEntity.get)).map(v => if (v == 0) "-" else v.toString).mkString("/"),
          weapon.damageValue(reactiveEntity.get) + weapon.damageType.abridged
        )
      }

      override def createDetailsBlock(spell: ReactiveValue[Weapon]): Container = {
        Panels.vertical() // TODO
      }
    }
  }

  def damageGauges(dmg: ReactiveValue[Seq[HasDamageMonitor]]): Container = dmg ==> { dmgSeq =>
    def damageGauge(mon: HasDamageMonitor) = Seq(
      new Label(mon.damageType.map(_.toString).getOrElse("Tous")),
      new Label(s"[${mon.currentValue} / ${mon.maxValue}]"),
      new ProgressBar(0, mon.maxValue, 20).setValue(mon.currentValue)
    )

    Panels.grid(3, dmgSeq.flatMap(damageGauge): _*)
  }

  def entityPanel(reactiveEntity: ReactiveValue[GameEntity], withFrame: Boolean = false): Container = {
    val shortcuts = mutable.Map[Char, Container]()
    val entity = reactiveEntity.get // todo

    def withShortcut(char: Char, container: Container) = {
      shortcuts.put(char, container)
      container
    }

    val panel = new Panel(new LinearLayout(Direction.VERTICAL).setSpacing(1)) {
      override def handleInput(key: KeyStroke): Boolean = {
        if (!super.handleInput(key) && key.getCharacter != null) {
          if (shortcuts.contains(key.getCharacter.charValue())) {
            val focus = shortcuts(key.getCharacter.charValue()).nextFocus(null)

            if (focus != null) {
              focus.takeFocus()
              true
            } else false
          }
          else if (key.getCharacter == 'd') {
            reactiveEntity.update(_.damage(1, if (key.isAltDown) Stunning else Physical))
            true
          }
          else if (key.getCharacter == 'h') {
            reactiveEntity.update(_.heal(1, if (key.isAltDown) Stunning else Physical))
            true
          }
          else false
        } else true
      }
    }

    panel addComponent withShortcut('a', attributeTable(entity.attributes).withBorder(Borders.singleLine("Attributs")))
      .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))

    entity.components[HasSkills].foreach(skillComponent => {
      panel addComponent withShortcut('c', skillsTable(entity, skillComponent).withBorder(Borders.singleLine("Compétences")))
        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
    })

    if (entity.components[HasInventory].exists(_.weapons.nonEmpty)) {
      panel.addComponent(weaponsTable(reactiveEntity).withBorder(Borders.singleLine("Armes")), LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
      )
    }

    entity.components[HasMagic].foreach(magicComponent => {
      panel addComponent withShortcut('m', magicTable(reactiveEntity, magicComponent).withBorder(Borders.singleLine(s"Magie (${magicComponent.tradition})")))
        .setLayoutData(LinearLayout.createLayoutData(LinearLayout.Alignment.Center))
    })

    if (entity.components[HasDamageMonitor].nonEmpty) {
      val reactiveDmg = reactiveEntity.map[Seq[HasDamageMonitor]](_.components[HasDamageMonitor], (entity, dmgMonitors) => entity.setComponents(dmgMonitors))
      panel.addComponent(damageGauges(reactiveDmg).withBorder(Borders.singleLine("Moniteurs d'état")), LinearLayout.createLayoutData(LinearLayout.Alignment.Center)
      )
    }


    if (withFrame) panel.withBorder(Borders.singleLine(entity.name))
    else panel
  }
}

class EntityWindow(reactiveEntity: ReactiveValue[GameEntity]) extends BasicWindow {
  setHints(List(Hint.CENTERED).asJava)

  // Determine the title
  setTitle(reactiveEntity.get.name)

  setComponent(entityPanel(reactiveEntity))

  addWindowListener(new WindowListenerAdapter {
    override def onUnhandledInput(basePane: Window, key: KeyStroke, hasBeenHandled: AtomicBoolean): Unit = {
      if (key.getCharacter == 'q' || key.getKeyType == KeyType.Escape) {
        close()
      }
    }
  })


}
