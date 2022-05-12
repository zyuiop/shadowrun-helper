package me.ceyal.srh.data

import me.ceyal.srh.data.Attributs.{Attribut, Charisme, Logique, Magie, Volonté}
import me.ceyal.srh.data.gear.InventoryItem
import me.ceyal.srh.data.gear.Weapons.DamageType
import me.ceyal.srh.data.skills.Competences.{Competence, Sorcellerie}
import me.ceyal.srh.data.skills.{SorcellerieSpecs, Specialization}
import me.ceyal.srh.data.spells.{Chamanism, Hermetism, MagicTradition, Spell}

package object components {
  sealed trait Component {
    val tags: Set[Class[_ <: Component]] = Set(getClass)
  }

  trait AttributeModifier extends Component {
    def attributeModifier(attr: Attribut): Int
  }

  case class HasInventory(inventory: List[InventoryItem]) extends Component with AttributeModifier {
    def attributeModifier(attr: Attribut): Int = inventory.map(_.attrModifiers.getOrElse(attr, 0)).sum

    override val tags: Set[Class[_ <: Component]] = Set(getClass, classOf[AttributeModifier])
  }

  implicit val hasInventoryClazz: Class[HasInventory] = classOf[HasInventory]

  case class HasSkills(skills: Map[Competence, SkillLevel]) extends Component {
    def dicesWithSkill(dices: AttrGetter)(attr: Attribut, skill: Competence, spe: Option[Specialization] = None): Int = {
      skills.get(skill) match {
        case Some(level) => level.level + dices(attr) +
          (if (spe.nonEmpty && spe == level.mastery) 3 else if (spe.nonEmpty && spe == level.specialization) 2 else 0)
        case None => if (skill.inexperienced) dices(attr) - 1 else 0
      }
    }

    def dicesForSkill(dices: AttrGetter)(skill: Competence, spe: Option[Specialization] = None): Int = dicesWithSkill(dices)(skill.mainStat, skill, spe)
  }

  implicit val hasSkillsClazz: Class[HasSkills] = classOf[HasSkills]

  case class HasInitiative(dices: Map[Dimension, Int]) extends Component {
    def initiative(attr: AttrGetter)(dimension: Dimension): Int = {
      // TODO - dimension matters here
      attr(Attributs.Réaction) + attr(Attributs.Intuition)
    }

    def initiativeDices(dimension: Dimension): Int = dices.get(dimension).orElse(dices.get(Overworld)).getOrElse(1)
  }

  implicit val hasInitiativeClazz: Class[HasInitiative] = classOf[HasInitiative]

  case class HasEnemyLevel(profLevel: Int) extends Component

  implicit val hasEnemyLevelClazz: Class[HasEnemyLevel] = classOf[HasEnemyLevel]

  case class HasName(name: String) extends Component

  implicit val hasNameClazz: Class[HasName] = classOf[HasName]

  case class HasDamageMonitor(maxValue: Int, currentValue: Int, damageType: Option[DamageType] = None) extends Component

  implicit val hasDamageMonitorClazz: Class[HasDamageMonitor] = classOf[HasDamageMonitor]

  case class HasMagic(tradition: MagicTradition, spells: Seq[Spell]) extends Component {
    val traditionAttribute: Attribut = tradition match {
      case Chamanism => Charisme
      case Hermetism => Logique
    }

    def magicAtkScore(attr: AttrGetter): Int = attr(Magie) + attr(traditionAttribute)

    /**
     * How many dices to resist against drain
     */
    def drainReserve(attr: AttrGetter): Int = attr(Volonté) + attr(traditionAttribute)

    /**
     * How many dices to cast a spell
     */
    def castReserve(attr: AttrGetter, e: HasSkills): Int = e.dicesForSkill(attr)(Sorcellerie, Some(SorcellerieSpecs.LancementSorts))
  }

  implicit val hasMagicClazz: Class[HasMagic] = classOf[HasMagic]
}
