package me.ceyal.srh.data

import me.ceyal.srh.data.Attributs.Attribut
import me.ceyal.srh.data.gear.Weapons.WeaponKind
import me.ceyal.srh.data.skills.Competences.{Competence, valueToCompetence}
import me.ceyal.srh.data.skills.IngenierieSpecs.IngenierieSpec
import me.ceyal.srh.data.skills.PilotageSpecs.PilotageSpec
import me.ceyal.srh.util.{EnumValueBase, enumOfEnumFormat}
import play.api.libs.json._

import scala.util.Try

package object skills {
  sealed trait SpecializationsSet {
    def withName(str: String): Any
    def apply(i: Int): Any
  }

  sealed trait Specialization extends EnumValueBase

  implicit val specializationSetFormat: Format[SpecializationsSet] = new Format[SpecializationsSet] {
    override def reads(json: JsValue): JsResult[SpecializationsSet] = json match {
      case JsString(s) =>
        val ss = Class.forName(s).getField("MODULE$").get(null).asInstanceOf[SpecializationsSet]
        JsSuccess(ss)
      case _ => JsError("Object expected")
    }

    override def writes(o: SpecializationsSet): JsValue = {
      val enumClass = o.getClass.getSimpleName
      JsString(enumClass)
    }
  }

  implicit val specializationFormat: Format[Specialization] = enumOfEnumFormat[Specialization]

  object Competences extends Enumeration {
    /**
     * @param name name of the skill
     * @param mainStat main stat used for this skill
     * @param specialization enum of available specializations for this skill
     * @param inexperienced if true, this can be used even with a level of 0 (at the cost of -1 dice)
     * @param alternativeStat
     */
    case class Competence(name: String, mainStat: Attribut, specialization: SpecializationsSet, inexperienced: Boolean = true, alternativeStat: Map[String, Attribut] = Map.empty) extends super.Val(name) {
      def canUse(stats: AttrBlock) = true
    }

    import scala.language.implicitConversions
    implicit def valueToCompetence(x: Value): Competence = x.asInstanceOf[Competence]

    val ArmesAFeu = Competence("Armes à feu", Attributs.Agilité, FirearmKinds)
    val ArmesExotiques = Competence("Armes exotiques", Attributs.Agilité, ExoticWeaponsKinds, inexperienced = false) // TODO - integrate list of exotic weapons somehow
    val Astral = Competence("Astral", Attributs.Intuition, AstralSpecs, inexperienced = false, alternativeStat =
      Map("Combat astral" -> Attributs.Volonté)
    )
    val Athletisme = Competence("Athlétisme", Attributs.Agilité, AthletismeSpecs, alternativeStat =
      Map("Tests où on peut se heurter à une résistance (ex: courir dans la boue)" -> Attributs.Force)
    )
    val Biotech = Competence("Biotech", Attributs.Logique, BiotechSpecs, inexperienced = false, alternativeStat =
      Map("Lorsque la tentative ne respecte pas vraiment les règles traditionnelles de la biotech" -> Attributs.Intuition)
    )
    val CombatRapproche = Competence("Combat rapproché", Attributs.Agilité, CloseCombatWeaponKind)
    val Conjuration = Competence("Conjuration", Attributs.Magie, ConjurationSpecs, inexperienced = false)
    val Electronique = Competence("Électronique", Attributs.Logique, ElectroniqueSpecs, alternativeStat =
      Map("Bidouillage" -> Attributs.Intuition)
    )
    val Enchantement = Competence("Enchantement", Attributs.Magie, EnchantementSpecs, inexperienced = false)
    val Escroquerie = Competence("Escroquerie", Attributs.Charisme, EscroquerieSpecs)
    val Furtivite = Competence("Furtivité", Attributs.Agilité, FurtiviteSpecs)
    val Influence = Competence("Influence", Attributs.Charisme, InfluenceSpecs, alternativeStat = Map(
      "Pour présenter un argument clair et évident, principalement lors de négociations" -> Attributs.Logique
    ))
    val Ingenierie = Competence("Ingénierie", Attributs.Logique, IngenierieSpecs, alternativeStat = Map(
      "Bricolage" -> Attributs.Intuition,
      "Crochetage" -> Attributs.Agilité
    ))
    val Perception = Competence("Perception", Attributs.Intuition, PerceptionSpecs, alternativeStat = Map(
      "Pour identifier des schémas et des formes" -> Attributs.Logique
    ))
    val Pilotage = Competence("Pilotage", Attributs.Réaction, PilotageSpecs)

    val Piratage = Competence("Piratage", Attributs.Logique, PiratageSpecs, inexperienced = false)
    val PleinAir = Competence("Plein air", Attributs.Intuition, PleinAirSpecs)
    val Sorcellerie = Competence("Sorcellerie", Attributs.Magie, SorcellerieSpecs, inexperienced = false)
    val Technomancie = Competence("Technomancie", Attributs.Résonance, TechnomancieSpecs, inexperienced = false)
  }

  implicit val competencesFormat: Format[Competences.Value] = Json.formatEnum(Competences)
  implicit val competenceFormat: Format[Competence] = competencesFormat.bimap(valueToCompetence, identity)
  implicit val keyReads: KeyReads[Competence] = KeyReads(a => competenceFormat.reads(JsString(a)))
  implicit val keyWrites: KeyWrites[Competence] = KeyWrites(a => competenceFormat.writes(a).validate[String].get)

  // Todo - move?
  object FirearmKinds extends Enumeration with SpecializationsSet {
    case class FirearmKind(name: String) extends super.Val(name) with WeaponKind with Specialization

    val CanonsAssaut = FirearmKind("Canons d'assaut")
    val Fusils = FirearmKind("Fusils")
    val Mitraillettes = FirearmKind("Mitraillettes")
    val Mitrailleuses = FirearmKind("Mitrailleuses")
    val PistoletsDePoche = FirearmKind("Pistolets de poche")
    val PistoletsLegers = FirearmKind("Pistolets légers")
    val PistoletsLourds = FirearmKind("Pistolets lourds")
    val PistoletsAutomatiques = FirearmKind("Pistolets automatiques")
    val Shotguns = FirearmKind("Shotguns")
    val Tasers = FirearmKind("Tasers")
  }

  object ExoticWeaponsKinds extends Enumeration with SpecializationsSet {
    case class ExoticWeaponsKind(name: String) extends super.Val(name) with WeaponKind with Specialization

    val Fouet = ExoticWeaponsKind("Fouet")
    val FouetMonofilament = ExoticWeaponsKind("Fouet Monofilament")
  }

  object AstralSpecs extends Enumeration with SpecializationsSet {
    case class AstralSpec(name: String) extends super.Val(name) with Specialization

    val CombatAstral = AstralSpec("Combat astral")
    val EtatsEmotionnels = AstralSpec("États émotionnels")
    val SignaturesAstrales = AstralSpec("Signatures astrales")
    val TypesDesprit = AstralSpec("Types d'esprit")
  }

  object AthletismeSpecs extends Enumeration with SpecializationsSet {
    case class AthletismeSpec(name: String) extends super.Val(name) with Specialization

    val Archerie = new AthletismeSpec("Archerie") with WeaponKind
    val Escalade = AthletismeSpec("Escalade")
    val Gymnastique = AthletismeSpec("Gymnastique")
    val Lancer = new AthletismeSpec("Lancer") with WeaponKind
    val Natation = AthletismeSpec("Natation")
    val Saut = AthletismeSpec("Saut")
    val Sprint = AthletismeSpec("Sprint")
    val Vol = AthletismeSpec("Vol")
  }

  object BiotechSpecs extends Enumeration with SpecializationsSet {
    case class BiotechSpec(name: String) extends super.Val(name) with Specialization

    val Biotechnologie = BiotechSpec("Biotechnologie")
    val Cybertechnologie = BiotechSpec("Cybertechnologie")
    val Medecine = BiotechSpec("Médecine")
    val PremiersSoins = BiotechSpec("Premiers soins")
  }

  object CloseCombatWeaponKind extends Enumeration with SpecializationsSet {
    case class CloseCombatSpec(name: String) extends super.Val(name) with WeaponKind with Specialization

    val Contondantes = CloseCombatSpec("Armes contondantes")
    val Tranchantes = CloseCombatSpec("Armes tranchantes")
    val MainsNues = CloseCombatSpec("Combat à mains nues")
  }

  object ConjurationSpecs extends Enumeration with SpecializationsSet {
    case class ConjurationSpec(name: String) extends super.Val(name) with Specialization

    val Invocation = ConjurationSpec("Invocation")
    val Bannissement = ConjurationSpec("Bannissement")
  }

  object ElectroniqueSpecs extends Enumeration with SpecializationsSet {
    case class ElectroniqueSpec(name: String) extends super.Val(name) with Specialization

    val Informatique = ElectroniqueSpec("Informatique")
    val Logiciels = ElectroniqueSpec("Logiciels")
    val MaterielElectro = ElectroniqueSpec("Matériel électronique")
  }

  object EnchantementSpecs extends Enumeration with SpecializationsSet {
    case class EnchantementSpec(name: String) extends super.Val(name) with Specialization

    val Alchimie = EnchantementSpec("Alchimie")
    val CreationArtefact = EnchantementSpec("Création d'artefact")
    val Desenchanement = EnchantementSpec("Désenchantement")
  }

  object EscroquerieSpecs extends Enumeration with SpecializationsSet {
    case class EscroquerieSpec(name: String) extends super.Val(name) with Specialization

    val Comedie = EscroquerieSpec("Comédie")
    val Deguisement = EscroquerieSpec("Déguisement")
    val Imposture = EscroquerieSpec("Imposture")
    val Representation = EscroquerieSpec("Représentation")
  }

  object FurtiviteSpecs extends Enumeration with SpecializationsSet {
    case class FurtiviteSpec(name: String) extends super.Val(name) with Specialization

    val Camouflage = FurtiviteSpec("Camouflage")
    val Discretion = FurtiviteSpec("Discrétion")
    val Escamotage = FurtiviteSpec("Escamotage")
  }

  object InfluenceSpecs extends Enumeration with SpecializationsSet {
    case class InfluenceSpec(name: String) extends super.Val(name) with Specialization

    val Enseignement = InfluenceSpec("Enseignement")
    val Etiquette = InfluenceSpec("Étiquette")
    val Intimidation = InfluenceSpec("Intimidation")
    val Leadership = InfluenceSpec("Leadership")
    val Negociation = InfluenceSpec("Négociation")
  }

  object IngenierieSpecs extends Enumeration with SpecializationsSet {
    case class IngenierieSpec(name: String) extends super.Val(name) with Specialization

    val ArmesVehicule = IngenierieSpec("Armes de véhicule")
    val Armurerie = IngenierieSpec("Armurerie")
    val Crochetage = IngenierieSpec("Crochetage")
    val Demolition = IngenierieSpec("Démolition")

    val MecaAeronotique = IngenierieSpec("Mécanique aéronotique")
    val MecaAutomobile = IngenierieSpec("Mécanique automobile")
    val MecaNautique = IngenierieSpec("Mécanique nautique")

    val MecaIndustrielle = IngenierieSpec("Mécanique industrielle")
  }

  object PerceptionSpecs extends Enumeration with SpecializationsSet {
    case class PerceptionSpec(name: String) extends super.Val(name) with Specialization

    val Auditive = PerceptionSpec("Auditive")
    val Tactile = PerceptionSpec("Tactile")
    val Visuelle = PerceptionSpec("Visuelle")

    val EnvDeserts = PerceptionSpec("Déserts")
    val EnvForets = PerceptionSpec("Forêts")
    val EnvMilieuxUrbains = PerceptionSpec("Milieux urbains")
    // TODO: autres environements
  }

  object PilotageSpecs extends Enumeration with SpecializationsSet {
    case class PilotageSpec(name: String) extends super.Val(name) with Specialization

    val VehicAerien = PilotageSpec("Véhicules aériens")
    val VehicAquatique = PilotageSpec("Véhicules aquatiques")
    val VehicTerrestre = PilotageSpec("Véhicules terrestres")
  }

  // TODO Move
  object VehicleTypes extends Enumeration {
    case class VehicleType(name: String, pilotingSkill: PilotageSpec, repairingSkill: IngenierieSpec) extends super.Val(name)

    val Aerien = VehicleType("Aérien", PilotageSpecs.VehicAerien, IngenierieSpecs.MecaAeronotique)
    val Aquatique = VehicleType("Aquatique", PilotageSpecs.VehicAquatique, IngenierieSpecs.MecaNautique)
    val Terrestre = VehicleType("Terrestre", PilotageSpecs.VehicTerrestre, IngenierieSpecs.MecaAutomobile)
  }

  object PiratageSpecs extends Enumeration with SpecializationsSet {
    case class PiratageSpec(name: String) extends super.Val(name) with Specialization

    val Cybercombat = PilotageSpec("Cybercombat")
    val GuerreElectronique = PilotageSpec("Guerre électronique")
    val Hacking = PilotageSpec("Hacking")
  }

  object PleinAirSpecs extends Enumeration with SpecializationsSet {
    case class PleinAirSpec(name: String) extends super.Val(name) with Specialization

    val Orientation = PleinAirSpec("Orientation")
    val Pistage = PleinAirSpec("Pistage")
    val Survie = PleinAirSpec("Survie")

    val EnvDeserts = PleinAirSpec("Déserts")
    val EnvForets = PleinAirSpec("Forêts")
    val EnvMilieuxUrbains = PleinAirSpec("Milieux urbains")
    // TODO: autres environements
  }

  object SorcellerieSpecs extends Enumeration with SpecializationsSet {
    case class SorcellerieSpec(name: String) extends super.Val(name) with Specialization

    val Contresort = SorcellerieSpec("Contresort")
    val LancementSorts = SorcellerieSpec("Lancement de sorts")
    val MagieRituelle = SorcellerieSpec("Magie rituelle")
  }

  object TechnomancieSpecs extends Enumeration with SpecializationsSet {
    case class TechnomancieSpec(name: String) extends super.Val(name) with Specialization

    val Compilation = TechnomancieSpec("Compilation")
    val Decompilation = TechnomancieSpec("Décompilation")
    val Inscription = TechnomancieSpec("Inscription")
  }
}
