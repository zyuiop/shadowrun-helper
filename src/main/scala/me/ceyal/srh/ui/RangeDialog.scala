package me.ceyal.srh.ui

import com.googlecode.lanterna.gui2.{TextGUIGraphics, WindowBasedTextGUI}
import com.googlecode.lanterna.gui2.dialogs.ListSelectDialogBuilder
import me.ceyal.srh.data.gear.Weapons.Ranges

object RangeDialog {
  def apply(gui: WindowBasedTextGUI, title: String = "Choisir la portée"): Option[Ranges.Range] = {
    val rng = new ListSelectDialogBuilder[Ranges.Range]
      .addListItems(Ranges.values.toSeq :_*)
      .setTitle(title)
      .setCanCancel(true)
      .build()
      .showDialog(gui)

    Option(rng)
  }
}
