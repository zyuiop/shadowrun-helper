package me.ceyal.srh.ui.components

import com.googlecode.lanterna.gui2.Interactable
import com.googlecode.lanterna.gui2.menu.{Menu, MenuBar, MenuItem}
import com.googlecode.lanterna.input.{KeyStroke, KeyType}

import scala.collection.mutable

class MenuBarWithShortcuts extends MenuBar with WithShortcuts {
  override def add(menuItem: Menu): MenuBar = {
    super[WithShortcuts].add(menuItem.getLabel.head, () => {
      menuItem.takeFocus()
      menuItem.handleInput(new KeyStroke(KeyType.Enter))
    })
    super.add(menuItem)
  }

  override def handleInput(keyStroke: KeyStroke): Boolean =
    super[WithShortcuts].handleKeyStroke(keyStroke) == Interactable.Result.HANDLED || super.handleInput(keyStroke)
}
