package me.ceyal.srh.data.skills

import me.ceyal.srh.data.components.HasSkills
import me.ceyal.srh.data.entities.{BaseEnemies, GameEntity}
import play.api.libs.json.Json

import scala.util.Try

object PackageTest extends App {

  val Specs = List[Specialization](
    InfluenceSpecs.Intimidation,
    FurtiviteSpecs.Discretion,
    SorcellerieSpecs.LancementSorts
  )

  def serializeSpecs(): Unit = {
    println(Specs.map(Json.toJson[Specialization]))
  }

  def deserializespecs(): Unit = {
    println(Specs.map(Json.toJson[Specialization]).map(Json.fromJson[Specialization]).map(_.get))
    println(Specs.map(Json.toJson[Specialization]).map(Json.fromJson[Specialization]).map(_.get) == Specs)
  }

  def serializeComp() = {
    println(Competences.values.toList.map(Json.toJson[Competences.Value]))
  }
  def deserializeComp() = {
    println(Competences.values.toList.map(Json.toJson[Competences.Value]).map(Json.fromJson[Competences.Value]).map(_.get))
    println(Competences.values.toList.map(Json.toJson[Competences.Value]).map(Json.fromJson[Competences.Value]).map(_.get) == Competences.values.toList)
  }

  def serializeEnemies() = {
    println(BaseEnemies.Enemies.map(Json.toJson[GameEntity]).map(Json.prettyPrint).mkString("\n\n"))
  }


  def deserializeEnemies() = {
    println(BaseEnemies.Enemies.map(Json.toJson[GameEntity]).map(Json.fromJson[GameEntity]).map(_.get))
    println(BaseEnemies.Enemies.map(Json.toJson[GameEntity]).map(Json.fromJson[GameEntity]).map(_.get) == BaseEnemies.Enemies)
  }

  serializeSpecs()
  deserializespecs()

  serializeComp()
  deserializeComp()

  serializeEnemies()
  deserializeEnemies()
}
