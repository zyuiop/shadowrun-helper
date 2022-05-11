package me.ceyal.tuitui

import me.ceyal.tuitui.Table.Align.Align
import me.ceyal.tuitui.Table.Column
import org.fusesource.jansi.Ansi

case class Table[T](columns: Seq[Column[T]], rows: Seq[T], box: Box = Box.Simple, selected: Option[Int] = None) extends Drawable with Selectable[T] {
  private lazy val columnsContent: Seq[Seq[String]] = columns.map(col => rows.map(col.f))
  private lazy val columnsLengths = (columnsContent zip columns) map {
    case (cells, column) => (cells :+ column.title).map(_.length).max
  } // margin

  override def select(option: Option[Int]): Table[T] = copy(selected = option)

  override val numChoices: Int = rows.length

  override def size: (Int, Int) = {
    val width = columnsLengths.size * 3 + columnsLengths.sum + columnsLengths.size
    val height = rows.length + 4

    (width, height)
  }

  override def draw(x: Int, y: Int, term: Ansi): Ansi = {
    val toPrint: List[String] = List(
      box.upLeft + columnsLengths.map(length => box.horizontal * (length + 2)).mkString(box.horizontalSplitDown) + box.upRight,
      box.vertical + (columns zip columnsLengths).map { case (col, colLength) => " " + col.title + " " * (1 + (colLength - col.title.length)) }.mkString(box.vertical) + box.vertical,
      box.verticalSplitLeft + columnsLengths.map(length => box.horizontal * (length + 2)).mkString(box.cross) + box.verticalSplitRight
    ) :++ rows.zipWithIndex.map {
      case (row, rowIndex) =>
        val columns = this.columns.map(col => col.f(row))
        val (colorPre, colorPost) = if (selected.contains(rowIndex)) (Console.WHITE_B + Console.BLACK, Console.RESET) else ("", "")

        box.vertical + (columns zip columnsLengths).map { case (col, colLength) => colorPre + " " + col + " " * (1 + (colLength - col.length)) + colorPost }.mkString(box.vertical) + box.vertical
    } :+ box.downLeft + columnsLengths.map(length => box.horizontal * (length + 2)).mkString(box.horizontalSplitUp) + box.downRight

    toPrint.zipWithIndex.foldLeft(term) {
      case (term, (content, index)) =>
        term.cursor(y + index, x).a(content)
    }
  }

  override def option(index: Int): T = rows(index)

  override lazy val selectedIndex: Int = selected.get
  override lazy val selectedOption: T = option(selectedIndex)
}

object Table {

  object Align extends Enumeration {
    type Align = Value
    val Left, Right, Center = Value
  }

  case class Column[T](title: String, f: T => String, align: Align = Align.Left)

  def columns[T](names: Seq[String], map: T => Seq[String]): Seq[Column[T]] =
    names.zipWithIndex.map {
      case (col, idx) => Column[T](col, map(_)(idx))
    }

}