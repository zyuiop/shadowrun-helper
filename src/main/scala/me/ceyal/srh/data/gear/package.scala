package me.ceyal.srh.data

import me.ceyal.srh.util.{EnumValueBase, enumOfEnumFormat}
import play.api.libs.json.Format

package object gear {

  trait InventoryItem extends EnumValueBase {
    // TODO
    val attrModifiers: AttrBlock = Map()

    val armorIndex: Int = 0
  }

  implicit val format: Format[InventoryItem] = enumOfEnumFormat[InventoryItem]

  object MiscItems extends Enumeration {
    case class MiscItem(name: String) extends this.Val(name) with InventoryItem

    val Commlink: Map[Int, MiscItem] = (1 to 10).map(level => level -> MiscItem(s"CommLink (IA $level)")).toMap
  }
}
