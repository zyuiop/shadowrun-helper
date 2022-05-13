package me.ceyal.srh.data

import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.ui.EntityListWindow
import me.ceyal.srh.ui.reactive.ReactiveValue
import play.api.libs.json.{Json, OFormat}

import java.io.File

package object repositories {
  def basepath: File = {
    val home = System.getProperty("user.home")
    val path = Seq(home, ".config", "shadowrun").mkString(System.getProperty("file.separator"))
    new File(path)
  }

  // def statesPath: File = new File(basepath, "states")

  case class RosterEnemy(path: String, quantity: Int)
  implicit val rosterEnemyFormat: OFormat[RosterEnemy] = Json.format[RosterEnemy]
  type Roster = List[RosterEnemy]

  case class Scene(entities: List[GameEntity])

  object Scene {
    def fromRoster(roster: Roster): Scene = {
      val enemies = roster.flatMap(enemy =>
        EnemiesRepository.readFromFile(new File(enemy.path)).asOpt.toList
          .flatMap(entity => (1 to enemy.quantity).map(_ => entity))
      )

      Scene(enemies)
    }
  }
  implicit val sceneFormat: OFormat[Scene] = Json.format[Scene]

  val EnemiesRepository = new BaseRepository[GameEntity]("un ennemi", new File(basepath, "enemies"))
  val RostersRepository = new BaseRepository[Roster]("un roster", new File(basepath, "rosters"))
  val ScenesRepository = new BaseRepository[Scene]("une scène", new File(basepath, "scenes"))
}
