package me.ceyal.srh.data

import me.ceyal.srh.data.Attributs.Attribut
import me.ceyal.srh.data.gear.Weapons.DamageEffects.{Chemical, DamageEffect}
import me.ceyal.srh.data.gear.Weapons.DamageTypes.DamageType
import me.ceyal.srh.data.gear.Weapons.{Stunning}
import me.ceyal.srh.data.gear.Weapons.{Physical => DmgPhysical}
import me.ceyal.srh.data.spells.MagicTraditions.MagicTradition
import me.ceyal.srh.util.{EnumValueBase, enumOfEnumFormat}
import play.api.libs.json.{Format, Json, OFormat}

package object spells {
  object MagicTraditions extends Enumeration {
    type MagicTradition = Value
    val Chamanism = Value("Chamanisme")
    val Hermetism = Value("Hermétisme")
  }

  implicit val magicTraditionFormat: Format[MagicTradition] = Json.formatEnum(MagicTraditions)

  sealed class SpellType(override val toString: String)
  case object Physical extends SpellType("Physique")
  case object Mana extends SpellType("Mana")

  sealed class SpellDuration(override val toString: String)
  case object Instant extends SpellDuration("Instantané")
  case object Maintained extends SpellDuration("Maintenu")
  case class Limited(how: String) extends SpellDuration(s"Limité [$how]")
  case object Permanent extends SpellDuration("Permanent")

  sealed class SpellRange(override val toString: String)
  case object Contact extends SpellRange("Contact")
  case object LineOfSight extends SpellRange("Ligne de vue")
  case object Zone extends SpellRange("Zone (ligne de vue)")

  sealed class CombatType(val resistanceTypes: Set[Attribut], override val toString: String)
  case object DirectCombat extends CombatType(Set(Attributs.Volonté, Attributs.Intuition), "Direct")
  case object IndirectCombat extends CombatType(Set(Attributs.Réaction, Attributs.Volonté), "Indirect")

  sealed trait Spell extends EnumValueBase {
    val name: String
    val range: SpellRange
    val spellType: SpellType
    val drain: Int
    val duration: SpellDuration
    val description: String
  }

  implicit val spellFormat = enumOfEnumFormat[Spell]

  object HealingSpells extends Enumeration {
    case class HealingSpell(name: String, range: SpellRange, spellType: SpellType, duration: SpellDuration, drain: Int, description: String) extends super.Val(name) with Spell

    val Antidote = HealingSpell("Antidote", Contact, Physical, Permanent, 5, "Réduit la Virulence d'une toxine [Sorcellerie + Magie (Virulence, 1 round)]. Chaque succès réduit la Virulence de 1. Drain uniquement lors du lancer du sort (pas pour les jets suivants)")
    val SoinsPurificateurs = HealingSpell("Soins purificateurs", Contact, Physical, Permanent, 5, "[Sorcelerie + Magie (5 - Essence)] Soigne 1 dommage étourdissant par succès net ; élimine l'état Corrodé")
  }

  object CombatSpells extends Enumeration {
    case class CombatSpell(name: String, combatType: CombatType, range: SpellRange, spellType: SpellType, duration: SpellDuration, drain: Int, damageType: DamageType, description: String, damageAdditional: Set[DamageEffect] = Set()) extends super.Val(name) with Spell

    val FlotAcide = CombatSpell("Flot acide", IndirectCombat, LineOfSight, Physical, Instant, 5, DmgPhysical, "Inflige dmg Chimiques et l'état Corrodé (indice égal aux succès nets du test)", Set(Chemical))
    val VagueToxique = CombatSpell("Vague toxique", IndirectCombat, Zone, Physical, Instant, 6, DmgPhysical, "Inflige dmg Chimiques et l'état Corrodé (indice égal aux succès nets du test)", Set(Chemical))

    val FrappeADistance = CombatSpell("Frappe à distance", IndirectCombat, LineOfSight, Physical, Instant, 3, Stunning, "Sort indirect (dmg = succès net + amplification choisie)")
    val Deflagration = CombatSpell("Déflagration", IndirectCombat, Zone, Physical, Instant, 4, Stunning, "Crée une explosion (dmg = succès net + amplification choisie)")
  }

}
