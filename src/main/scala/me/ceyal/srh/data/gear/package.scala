package me.ceyal.srh.data

package object gear {

  trait InventoryItem {
    // TODO
    val attrModifiers: AttrBlock = Map()

    val armorIndex: Int = 0
  }

  case class Commlink(ia: Int) extends InventoryItem
}
