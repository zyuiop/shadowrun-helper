package me.ceyal.srh.data

import me.ceyal.srh.data.Attributs.{Attribut, Charisme, Logique, Magie, Volonté}
import me.ceyal.srh.data.Dimensions.Dimension
import me.ceyal.srh.data.components.HasDamageMonitor.forDamageType
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.gear.InventoryItem
import me.ceyal.srh.data.gear.Weapons.DamageTypes.DamageType
import me.ceyal.srh.data.gear.Weapons.{Physical, Stunning, Weapon}
import me.ceyal.srh.data.skills.Competences.{Competence, Sorcellerie}
import me.ceyal.srh.data.skills.{SorcellerieSpecs, Specialization}
import me.ceyal.srh.data.spells.MagicTraditions.{Chamanism, Hermetism, MagicTradition}
import me.ceyal.srh.data.spells.Spell
import play.api.libs.json.Format
import play.api.libs.json.Json.format

import scala.languageFeature.implicitConversions

package object components {
  sealed trait ComponentTag[_ <: Component]

  case class ClassComponentTag[T <: Component](clazz: Class[_ <: T]) extends ComponentTag[T]

  case class MixedComponentTag[T <: Component, U](clazz: Class[_ <: T], other: U) extends ComponentTag[T]

  object AttributeModifierTag extends ComponentTag[Component with AttributeModifier]

  implicit def componentTagFromClass[T <: Component](clazz: Class[T]): ComponentTag[T] = ClassComponentTag(clazz)

  sealed trait Component {
    val baseTag: ComponentTag[_ <: Component] = ClassComponentTag(getClass)
    val tags: Set[ComponentTag[_ <: Component]] = Set(baseTag)
  }

  trait AttributeModifier {
    def attributeModifier(attr: Attribut): Int
  }

  case class HasInventory(inventory: List[InventoryItem]) extends Component with AttributeModifier {
    def attributeModifier(attr: Attribut): Int = inventory.map(_.attrModifiers.getOrElse(attr, 0)).sum

    val (weapons, nonWeapons): (List[Weapon], List[InventoryItem]) = {
      val (w, n) = inventory.partition(_.isInstanceOf[Weapon])
      w.map(_.asInstanceOf[Weapon]) -> n
    }

    def updateWeapons(newWeapons: List[Weapon]) = copy(inventory = newWeapons ::: nonWeapons)
    def updateNonWeapons(newEquipment: List[InventoryItem]) = copy(inventory = newEquipment ::: weapons)

    override val tags: Set[ComponentTag[_ <: Component]] = Set(getClass, AttributeModifierTag)
  }

  implicit val hasInventoryClazz: ComponentTag[HasInventory] = classOf[HasInventory]
  implicit val hasInventoryFormat: Format[HasInventory] = format[HasInventory]

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

  implicit val hasSkillsClazz: ComponentTag[HasSkills] = classOf[HasSkills]
  implicit val hasSkillsFormat: Format[HasSkills] = format[HasSkills]


  case class HasInitiative(dices: Map[Dimension, Int], rolledValue: Option[Int] = None) extends Component {
    def initiative(attr: AttrGetter): Int = {
      // TODO - dimension matters here
      attr(Attributs.Réaction) + attr(Attributs.Intuition)
    }

    def getDices(dim: Dimension): Int = dices.getOrElse(dim, 1)

    def withRolledInitiative(rolledValue: Option[Int]): HasInitiative = copy(rolledValue = rolledValue)
  }

  implicit val hasInitiativeClazz: ComponentTag[HasInitiative] = classOf[HasInitiative]
  implicit val hasInitiativeFormat: Format[HasInitiative] = format[HasInitiative]

  case class HasEnemyLevel(profLevel: Int) extends Component

  implicit val hasEnemyLevelClazz: ComponentTag[HasEnemyLevel] = classOf[HasEnemyLevel]
  implicit val hasEnemyLevelFormat: Format[HasEnemyLevel] = format[HasEnemyLevel]

  /** Used to identify ennemies uniquely */
  case class HasEnemyId(id: String) extends Component

  implicit val hasEnemyIdClazz: ComponentTag[HasEnemyId] = classOf[HasEnemyId]
  implicit val hasEnemyIdFormat: Format[HasEnemyId] = format[HasEnemyId]

  case class HasName(name: String) extends Component

  implicit val hasNameClazz: ComponentTag[HasName] = classOf[HasName]
  implicit val hasNameFormat: Format[HasName] = format[HasName]

  case class HasDamageMonitor(maxValue: Int, currentValue: Int = 0, damageType: Option[DamageType] = None) extends Component {
    def suffer(dmg: Int) = copy(currentValue = Math.min(maxValue, currentValue + dmg))

    def heal(dmg: Int) = copy(currentValue = Math.max(0, currentValue - dmg))

    override val tags: Set[ComponentTag[_ <: Component]] = Set(baseTag) ++ (damageType match {
      case Some(tpe) => Set(forDamageType(tpe))
      case None => Set(forDamageType(Stunning), forDamageType(Physical))
    })
  }

  implicit class EntityWithDamageMonitor(entity: GameEntity) {
    def damage(amt: Int, kind: DamageType): GameEntity = {
      entity.mapAll[HasDamageMonitor](dmg => dmg.suffer(amt))(HasDamageMonitor.forDamageType(kind))
    }

    def heal(amt: Int, kind: DamageType): GameEntity = {
      entity.mapAll[HasDamageMonitor](dmg => dmg.heal(amt))(HasDamageMonitor.forDamageType(kind))
    }
  }

  object HasDamageMonitor {
    def forDamageType(damageType: DamageType): ComponentTag[HasDamageMonitor] = MixedComponentTag(classOf[HasDamageMonitor], damageType)
  }

  implicit val hasDamageMonitorClazz: ComponentTag[HasDamageMonitor] = classOf[HasDamageMonitor]
  implicit val hasDamageMonitorFormat: Format[HasDamageMonitor] = format[HasDamageMonitor]

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

  implicit val hasMagicClazz: ComponentTag[HasMagic] = classOf[HasMagic]
  implicit val hasMagicFormat: Format[HasMagic] = format[HasMagic]

  implicit val componentFormat: Format[Component] = format[Component]
}
