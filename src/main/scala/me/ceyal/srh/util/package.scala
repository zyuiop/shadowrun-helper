package me.ceyal.srh

import me.ceyal.srh.data.components.{HasEnemyLevel, HasName}
import me.ceyal.srh.data.entities.GameEntity
import me.ceyal.srh.data.skills.{Specialization, SpecializationsSet}
import play.api.libs.json.{Format, JsError, JsObject, JsResult, JsSuccess, JsValue, Json}

import scala.util.Try

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

  trait EnumValueBase {
    def name: String
    def id: Int
  }

  def enumOfEnumFormat[T <: EnumValueBase]: Format[T] =
    new Format[T] {
      override def reads(json: JsValue): JsResult[T] = json match {
        case obj: JsObject =>
          val enumClazz = (obj \ "_type").validate[String].get
          val name = (obj \ "name").validateOpt[String].get
          val index = (obj \ "index").validateOpt[Int].get

          val enum = Class.forName(enumClazz).getField("MODULE$").get(null).asInstanceOf[Enumeration]

          name.flatMap(n => Try { enum.withName(n).asInstanceOf[T] }.toOption)
            .orElse(index.flatMap(i => Try { enum(i).asInstanceOf[T] }.toOption)) match {
            case Some(spe) => JsSuccess(spe)
            case None => JsError("Bad block")
          }
        case _ => JsError("Object expected")
      }

      override def writes(o: T): JsValue = {
        val enumClass = o.getClass.getEnclosingClass

        Json.obj("_type" -> enumClass.getName, "index" -> o.id, "name" -> o.name)
      }
    }
}
