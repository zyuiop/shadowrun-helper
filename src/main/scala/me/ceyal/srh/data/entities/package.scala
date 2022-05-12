package me.ceyal.srh.data

import me.ceyal.srh.data.Attributs._
import me.ceyal.srh.data.components._
import me.ceyal.srh.data.gear.Weapons.MeleeWeapons
import me.ceyal.srh.data.gear.{Commlink, InventoryItem}
import me.ceyal.srh.data.skills.Competences.{Competence, _}
import me.ceyal.srh.data.skills.InfluenceSpecs
import me.ceyal.srh.data.spells.{Chamanism, CombatSpells, HealingSpells}
import me.ceyal.srh.data.{Astral => DimAstral}

package object entities {
  case class GameEntity(attributes: AttrBlock, components: List[Component] = List()) extends AttrGetter {
    private def modifiers: Seq[AttributeModifier] = components(classOf[AttributeModifier])

    def attr(attr: Attribut): Int = attributes.getOrElse(attr, 0) + modifiers.map(_.attributeModifier(attr)).sum

    override def apply(v1: Attribut): Int = attr(v1)

    def withComponents(components: Component*): GameEntity = copy(components = this.components ++ components)

    def components[T <: Component](implicit clazz: Class[T]): Seq[T] = components.filter(_.tags(clazz)).filter(_.isInstanceOf[T]).map(_.asInstanceOf[T])

    def component[T <: Component](implicit clazz: Class[T]): T = components(clazz).head

    def componentOpt[T <: Component](implicit clazz: Class[T]): Option[T] = components(clazz).headOption

    def setComponent[T <: Component](newV: T)(implicit clazz: Class[T]) = ???

    def setComponents[T <: Component](newV: Seq[T])(implicit clazz: Class[T]) = ???

    def map[T <: Component](mapping: T => T)(implicit clazz: Class[T]) = ???

    def mapComponent[T <: Component](newV: T)(implicit clazz: Class[T]) = ???

    /* Scores */

    def defScore: Int = attr(Attributs.Constitution) //  + modifiers.map(_.armorIndex).max // TODO - armor index impl as an attr modifier?

    def baseAtkScore: Int = attr(Attributs.Réaction) + attr(Attributs.Force) // TODO: "peut être modifié par certains traits et équipements"
  }

  /*case class Character(name: String,
                       attributes: AttrBlock,
                       skills: Map[Competence, SkillLevel],
                       karma: Int,
                       nuyens: Int,
                       knowledge: Set[String] = Set(),
                       inventory: List[InventoryItem] = List()) extends GameEntity
  */

  def baseEnemy(typeName: String, profLevel: Int,
                attributes: AttrBlock,
                skillsList: List[SkillLevel],
                inventory: List[InventoryItem] = List(),
                initiativeDices: Map[Dimension, Int] = Map()
               ): GameEntity = {
    val skills: Map[Competence, SkillLevel] = skillsList.map(a => a.skill -> a).toMap
    val dmgMonitor = Math.ceil(attributes(Constitution) / 2).toInt + 8

    GameEntity(attributes, List(
      HasName(typeName),
      HasEnemyLevel(profLevel),
      HasSkills(skills),
      HasInventory(inventory),
      HasDamageMonitor(dmgMonitor, dmgMonitor),
      HasInitiative(initiativeDices)
    ))
  }

  object BaseEnemies {
    val Enemies: Seq[GameEntity] = List(
      baseEnemy("Brute d'Humanis", 0, Map(
        Constitution -> 2, Agilité -> 2, Réaction -> 2, Force -> 2, Volonté -> 2, Logique -> 2, Intuition -> 2, Charisme -> 1, Essence -> 6000, DésInitiative -> 1
      ), List(
        SkillLevel(Athletisme, 1), SkillLevel(CombatRapproche, 3), SkillLevel(Influence, 1, Some(InfluenceSpecs.Intimidation))
      ), List(MeleeWeapons.Massue, Commlink(1))),

      baseEnemy("Chaman TerraFirst !", 0, Map(
        Constitution -> 1, Agilité -> 2, Réaction -> 3, Force -> 2, Volonté -> 3, Logique -> 2, Intuition -> 2, Charisme -> 2, Magie -> 2, Essence -> 6000
      ), List(
        SkillLevel(Astral, 2), SkillLevel(Conjuration, 2), SkillLevel(Sorcellerie, 2)
      ), List(Commlink(1)), Map(DimAstral -> 2)).withComponents(HasMagic(Chamanism, List(
        HealingSpells.Antidote, HealingSpells.SoinsPurificateurs, CombatSpells.FlotAcide, CombatSpells.Deflagration
      )))
    )

    val EnemiesByProLevel: Map[Int, Seq[GameEntity]] = Enemies.groupBy(_.component[HasEnemyLevel].profLevel)
  }


}