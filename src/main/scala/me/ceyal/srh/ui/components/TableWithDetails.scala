package me.ceyal.srh.ui.components

import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.gui2._
import com.googlecode.lanterna.gui2.table.{DefaultTableCellRenderer, Table}
import com.googlecode.lanterna.input.KeyStroke

import scala.jdk.CollectionConverters.SeqHasAsJava

class TableWithDetails[T](rows: Seq[T], headers: Seq[String], rowMapperLambda: T => Seq[String] = (_: T) => ???, detailsMapperLambda: T => Container = (_: T) => ???,
                               detailsPosition: Direction = Direction.VERTICAL) extends Panel(new LinearLayout(detailsPosition).setSpacing(1)) {

  private var detailsPanel: Option[Container] = None
  private var childFocused: Boolean = false

  private def setDetailsPanel(e: T): Unit = synchronized {
    clearDetailsPanel()
    val pane = createDetailsBlock(e)
    detailsPanel = Some(pane)
    addComponent(pane)
  }

  private def clearDetailsPanel(): Unit = synchronized {
    if (detailsPanel.nonEmpty) {
      removeComponent(detailsPanel.get)
      detailsPanel = None
    }
  }

  val table: Table[String] = new Table[String](headers: _*) {
    rows.foreach(r => getTableModel.addRow(createRow(r).asJava))

    override def afterEnterFocus(direction: Interactable.FocusChangeDirection, previouslyInFocus: Interactable): Unit = {
      super.afterEnterFocus(direction, previouslyInFocus)
      childFocused = false
      setDetailsPanel(rows(getSelectedRow))
    }

    override def afterLeaveFocus(direction: Interactable.FocusChangeDirection, nextInFocus: Interactable): Unit = {
      super.afterLeaveFocus(direction, nextInFocus)
      childFocused = false
      if (nextInFocus == null || !nextInFocus.hasParent(TableWithDetails.this)) clearDetailsPanel()
      else childFocused = true
    }

    def selected: T = rows(getSelectedRow)

    setSelectAction(() => onSelect(selected))

    setTableCellRenderer(new DefaultTableCellRenderer[String]() {
      override def applyStyle(table: Table[String], cellValue: String, columnIndex: Int, rowIndex: Int, isSelected: Boolean, textGUIGraphics: TextGUIGraphics): Unit = {
        super.applyStyle(table, cellValue, columnIndex, rowIndex, isSelected, textGUIGraphics)

        if (isSelected && !isFocused && childFocused) {
          textGUIGraphics.setBackgroundColor(TextColor.ANSI.BLACK_BRIGHT)
          textGUIGraphics.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT)
        }
      }
    })

    override def handleKeyStroke(keyStroke: KeyStroke): Interactable.Result = {
      val parent = super.handleKeyStroke(keyStroke)

      if (parent != Interactable.Result.UNHANDLED) {
        if (parent == Interactable.Result.HANDLED)
          setDetailsPanel(rows(getSelectedRow))
        parent
      } else {
        keyHandler(keyStroke, selected)
      }

    }
  }

  /**
   * Called when a value is selected in the table
   * @param selected
   */
  def onSelect(selected: T): Unit = ()

  /**
   * Called when a key is pressed while a value is selected in the table
   * @param ks
   * @return
   */
  def keyHandler(ks: KeyStroke, selected: T): Interactable.Result = Interactable.Result.UNHANDLED

  /**
   * Called to produce the details block given a selected value t
   */
  def createDetailsBlock(value: T): Container = detailsMapperLambda(value)

  /**
   * Called to produce a row given a value t
   */
  def createRow(value: T): Seq[String] = rowMapperLambda(value)


  addComponent(table, LinearLayout.createLayoutData(LinearLayout.Alignment.Center))

  override def nextFocus(fromThis: Interactable): Interactable = super.nextFocus(fromThis)
}
