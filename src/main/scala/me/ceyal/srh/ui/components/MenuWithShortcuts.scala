package me.ceyal.srh.ui.components

import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.menu.{Menu, MenuItem}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

import scala.collection.mutable

class MenuWithShortcuts(label: String) extends Menu(label) with WithShortcuts {
  override def add(menuItem: MenuItem): Menu = {
    super[WithShortcuts].add(menuItem.getLabel.head, () => menuItem.takeFocus())
    super.add(menuItem)
  }

  override def handleKeyStroke(keyStroke: KeyStroke): Interactable.Result =
    super[WithShortcuts].handleKeyStroke(keyStroke, super.handleKeyStroke(keyStroke))
}
