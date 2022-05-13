
package me.ceyal.srh.ui.components

import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.menu.{Menu, MenuItem}
import com.googlecode.lanterna.input.KeyStroke

import scala.collection.mutable

trait WithShortcuts {
  private val map = mutable.Map[Char, () => Unit]()

  def add(cut: Char, interact: () => Unit) = {
    map.put(cut.toLower, interact)
  }

  def handleKeyStroke(keyStroke: KeyStroke, or: => Interactable.Result = Interactable.Result.UNHANDLED): Interactable.Result = {
    if (keyStroke.isAltDown && keyStroke.getCharacter != null) {
      map.get(keyStroke.getCharacter.charValue()) match {
        case Some(value) =>
          value.apply()
          Interactable.Result.HANDLED
        case None => or
      }
    } else or
  }
}
