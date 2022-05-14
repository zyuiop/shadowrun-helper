package me.ceyal.srh.ui.components

import com.googlecode.lanterna.gui2.table.Table
import me.ceyal.srh.ui.reactive.{ReactiveComponentTool, ReactiveValue}

class ReactiveTable[T, ST <: Seq[T]](reactiveRows: ReactiveValue[ST],
                       headers: Seq[String],
                       rowMapperLambda: T => Seq[String] = (_: T) => ???
                      ) extends Table[String](headers: _*) with ReactiveComponentTool {

  /**
   * Called to produce a row given a value t
   */
  def createRow (value: T): Seq[String] = rowMapperLambda (value)

  val rows: () => Seq[T] = use(reactiveRows)

  listen[ST](reactiveRows, (nv, _) => {
    val model = getTableModel

    // Prepare rows
    while (model.getRowCount > nv.size) {
      model.removeRow(model.getRowCount - 1)
    }
    while (model.getRowCount < nv.size) {
      model.addRow(headers :_*)
    }

    // Set rows
    nv.map(createRow).zipWithIndex.foreach {
      case (rowContent, rowIndex) =>
        rowContent.zipWithIndex.foreach {
          case (cell, column) => model.setCell(column, rowIndex, cell)
        }
    }
  })
}
