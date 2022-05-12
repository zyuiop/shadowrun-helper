package me.ceyal.srh

import me.ceyal.srh.data.components.{HasEnemyLevel, HasName}
import me.ceyal.srh.data.entities.GameEntity

package object util {
  implicit class NamedGameEntity(entity: GameEntity) {
    lazy val name: String = {
      val base = entity.componentOpt[HasName].map(_.name).getOrElse("???")
      entity.componentOpt[HasEnemyLevel].map(level => s"$base [Professionalisme ${level.profLevel}]").getOrElse(base)
    }
  }

  def foldText(text: String, maxLen: Int) = {
    text.split(" ").foldLeft(List[String]()) {
      case (head :: tail, word) =>
        if ((head + " " + word).length > maxLen) word :: head :: tail
        else (head + " " + word) :: tail
      case (Nil, word) => word :: Nil
    }.reverse.mkString("\n")
  }
}
