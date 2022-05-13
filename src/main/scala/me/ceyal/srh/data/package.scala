package me.ceyal.srh

import me.ceyal.srh.data.Attributs.{Attribut, Value, valueToAttribut}
import me.ceyal.srh.data.gear.InventoryItem
import me.ceyal.srh.data.gear.Weapons.DamageType
import me.ceyal.srh.data.skills.Competences.Competence
import me.ceyal.srh.data.skills.{Specialization, SpecializationsSet, competenceFormat}
import play.api.libs.json.{DefaultReads, DefaultWrites, Format, JsString, Json, KeyReads, KeyWrites, Reads, Writes}

package object data {
  object Dimensions extends Enumeration {
    type Dimension = Value

    val Overworld = Value("Monde Réel")
    val Astral = Value("Dimension Astrale")
    val Matrix = Value("Matrice")
    val VRCold = Value("Réalité Virtuelle (froide)")
    val VRHot = Value("Réalité Virtuelle (chaude)")
  }

  import Dimensions._

  implicit val dimensionFormat: Format[Dimension] = Json.formatEnum(Dimensions)
  implicit val dimensionKeyReads: KeyReads[Dimension] = KeyReads(a => dimensionFormat.reads(JsString(a)))
  implicit val dimensionKeyWrites: KeyWrites[Dimension] = KeyWrites(a => dimensionFormat.writes(a).validate[String].get)

  object Attributs extends Enumeration {
    case class Attribut(abbr: String) extends super.Val

    import scala.language.implicitConversions

    implicit def valueToAttribut(x: Value): Attribut = x.asInstanceOf[Attribut]

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

  implicit val attrsFormat: Format[Attributs.Value] = Json.formatEnum(Attributs)
  implicit val attrFormat: Format[Attribut] = attrsFormat.bimap(valueToAttribut, identity)
  implicit val keyReads: KeyReads[Attribut] = KeyReads(a => attrFormat.reads(JsString(a)))
  implicit val keyWrites: KeyWrites[Attribut] = KeyWrites(a => attrFormat.writes(a).validate[String].get)

  case class SkillLevel(skill: skills.Competences.Competence, level: Int, specialization: Option[Specialization] = None, mastery: Option[Specialization] = None)

  implicit val skillLevelFormat: Format[SkillLevel] = Json.format[SkillLevel]

  type AttrBlock = Map[Attribut, Int]
  type AttrGetter = Attribut => Int
}
