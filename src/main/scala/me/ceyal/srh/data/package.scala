package me.ceyal.srh

import me.ceyal.srh.data.Attributs.Attribut
import me.ceyal.srh.data.gear.InventoryItem
import me.ceyal.srh.data.gear.Weapons.DamageType
import me.ceyal.srh.data.skills.Competences.Competence
import me.ceyal.srh.data.skills.{Specialization, SpecializationsSet}

package object data {
  type AttrBlock = Map[Attribut, Int]
  type AttrGetter = Attribut => Int



  sealed trait Dimension
  case object Overworld extends Dimension
  case object Astral extends Dimension
  case object Matrix extends Dimension
  case object VRCold extends Dimension
  case object VRHot extends Dimension

  object Attributs extends Enumeration {
    case class Attribut(abbr: String) extends super.Val

    val Constitution: Attribut = Attribut("CON")
    val Agilité: Attribut = Attribut("AGI")
    val Réaction: Attribut = Attribut("REA")
    val Force: Attribut = Attribut("FOR")
    val Volonté: Attribut = Attribut("VOL")
    val Logique: Attribut = Attribut("LOG")
    val Intuition: Attribut = Attribut("INT")
    val Charisme: Attribut = Attribut("CHA")
    val Atout: Attribut = Attribut("ATO")
    /**
     * BEWARE: Essence is computed * 1000
     */
    val Essence: Attribut = Attribut("ESS")

    val Magie: Attribut = Attribut("MAG")
    val Résonance: Attribut = Attribut("RES")

    // Un peu artificiel
    val DésInitiative: Attribut = Attribut("DI")
  }

  case class SkillLevel(skill: skills.Competences.Competence, level: Int, specialization: Option[Specialization] = None, mastery: Option[Specialization] = None)




}
