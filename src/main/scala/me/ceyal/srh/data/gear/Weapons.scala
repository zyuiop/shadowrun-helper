package me.ceyal.srh.data.gear

import me.ceyal.srh.data.{AttrBlock, AttrGetter, Attributs}
import me.ceyal.srh.data.Attributs.Attribut
import me.ceyal.srh.data.gear.Weapons.Ranges.Courte
import me.ceyal.srh.data.skills.AthletismeSpecs.Archerie
import me.ceyal.srh.data.skills.CloseCombatWeaponKind._
import me.ceyal.srh.data.skills.Competences.{Athletisme, CombatRapproche, Competence}
import me.ceyal.srh.data.skills.FirearmKinds._
import me.ceyal.srh.data.skills.ExoticWeaponsKinds._
import me.ceyal.srh.data.skills.{AthletismeSpecs, CloseCombatWeaponKind, Competences, ExoticWeaponsKinds, SpecializationsSet}

object Weapons {
  trait WeaponKind

  sealed trait DamageType
  case object Physical extends DamageType
  case object Stunning extends DamageType

  sealed trait Effect {
    // TODO: for electrocution and stuff
  }
  case object Electrocute extends Effect
  case object Chemical extends Effect

  object Ranges extends Enumeration {
    case class Range(description: String) extends super.Val

    val Proche = Range("0 à 3 mètres")
    val Courte = Range("4 à 50 mètres")
    val Moyenne = Range("51 à 250 mètres")
    val Longue = Range("251 à 500 mètres")
    val Extreme = Range("500+ mètres")
  }

  trait Weapon extends InventoryItem {
    def atkScore(range: Ranges.Range, holderAttr: AttrGetter): Int
    def damageDices(holderAttr: AttrGetter): Int

    val damageType: DamageType
    val hitEffects: Set[Effect]

    val usageSpecialization: Option[WeaponKind] = None
    val baseSkill: Competence
  }

  object MeleeWeapons extends Enumeration {
    case class MeleeWeapon(name: String, spec: WeaponKind, dices: Int, atkScore: Int, damageType: DamageType = Physical, hitEffects: Set[Effect] = Set(), moreAtkScores: Map[Ranges.Range, Int] = Map(), baseSkill: Competence = Competences.CombatRapproche) extends super.Val(name) with Weapon {
      override def atkScore(range: Ranges.Range, holderAttr: AttrGetter): Int = {
        if (range == Ranges.Proche) atkScore + holderAttr(if (baseSkill == CombatRapproche) Attributs.Force else Attributs.Réaction)
        else moreAtkScores.getOrElse(range, 0)
      }

      override val usageSpecialization: Option[WeaponKind] = Some(spec)

      override def damageDices(holderAttr: AttrGetter): Int = {
        val force = holderAttr(Attributs.Force)
        dices + (if (force >= 10) 2 else if (force >= 7) 1 else 0)
      }
    }

    val ArmesDhast = MeleeWeapon("Armes d'hast", Tranchantes, 4, 8)
    val Couteau = MeleeWeapon("Couteau", Tranchantes, 2, 6, moreAtkScores = Map(Courte -> 1))
    val CouteauCombat = MeleeWeapon("Couteau de combat/survie", Tranchantes, 3, 8, moreAtkScores = Map(Courte -> 2))
    val Epee = MeleeWeapon("Épée", Tranchantes, 3, 9)
    val HacheCombat = MeleeWeapon("Hache de combat", Tranchantes, 5, 9)
    val Katana = MeleeWeapon("Katana", Tranchantes, 4, 10)
    val LamesAvBras = MeleeWeapon("Lames d'avant bras", Tranchantes, 3, 6)

    val Baton = MeleeWeapon("Bâton", Contondantes, 4, 8, damageType = Stunning)
    val BatonTelescopique = MeleeWeapon("Bâton téléscopique", Contondantes, 4, 8, Stunning)
    val Electromatraque = MeleeWeapon("Électromatraque", Contondantes, 5, 6, Stunning, hitEffects = Set(Electrocute))
    val Massue = MeleeWeapon("Massue", Contondantes, 3, 6, damageType = Stunning)
    val MatraqueTelescopique = MeleeWeapon("Matraque téléscopique", Contondantes, 2, 5, Stunning)
    val NerfDeBoeuf = MeleeWeapon("Nerf de boeuf", Contondantes, 2, 6, Stunning)

    val ChaineMoto = MeleeWeapon("Chaîne de moto", MainsNues, 2, 5, Stunning)
    val CoupPoingAmericain = MeleeWeapon("Coup de poing américain", MainsNues, 3, 6)
    val ElectroGants = MeleeWeapon("Chaîne ", MainsNues, 4, 5, Stunning, hitEffects = Set(Electrocute))
    /*val AMainsNues = new MeleeWeapon("Mains nues ", MainsNues, 2, 0, Stunning) {
      override def atkScore(range: Ranges.Range, holderAttr: AttrGetter): Int = {
        if (range == Ranges.Proche) holderAttr(Attributs.Force) + holderAttr(Attributs.Réaction)
        else 0
      }
    }*/

    val Fouet = MeleeWeapon("Fouet", ExoticWeaponsKinds.Fouet, 1, 6, baseSkill = Competences.ArmesExotiques)
    val FouetMonofil = MeleeWeapon("Fouet monofilament", ExoticWeaponsKinds.FouetMonofilament, 4, 14, baseSkill = Competences.ArmesExotiques)
  }

  object ThrowableWeapons {
    case class ThrowableWeapon(name: String, spec: WeaponKind, dices: Int, atkScores: (Int, Int, Int, Int), damageType: DamageType = Physical, hitEffects: Set[Effect] = Set(), moreAtkScores: Map[Ranges.Range, Int] = Map(), baseSkill: Competence = Competences.CombatRapproche) extends Weapon {
      override def atkScore(range: Ranges.Range, holderAttr: AttrGetter): Int = {
        if (range == Ranges.Proche) atkScores._1
        else if (range == Ranges.Courte) atkScores._2
        else if (range == Ranges.Moyenne) atkScores._3
        else if (range == Ranges.Longue) atkScores._4
        else 0
      }

      override val usageSpecialization: Option[WeaponKind] = Some(spec)

      override def damageDices(holderAttr: AttrGetter): Int = dices
    }

    def bow(level: Int) = {
      require(level > 0 && level < 15)
      def divUp(fact: Int) = Math.ceil(level / fact.toDouble).toInt
      val atkScore = (if (level < 7) 2 else if (level < 10) 3 else 4)
      ThrowableWeapon(s"Arc (indice $level)", Archerie, atkScore, (divUp(2), divUp(3), divUp(4), 0))
    }

    val ArbaleteLegere = ThrowableWeapon("Arbalète légère", Archerie, 2, (2, 6, 8, 0))
    val ArbaleteStandard = ThrowableWeapon("Arbalète standard", Archerie, 3, (2, 10, 4, 2))
    val ArbaleteLourde = ThrowableWeapon("Arbalète lourde", Archerie, 4, (2, 8, 6, 4))
  }
}