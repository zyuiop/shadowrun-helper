package me.ceyal.tuitui

import org.fusesource.jansi.Ansi

object Main extends App {

  val b0 = Ansi.ansi().saveCursorPosition().eraseScreen()

  case class CharacterStats(`for`: Int, con: Int, agi: Int)

  implicit def int2str(int: Int): String = int.toString

  val table = Table[CharacterStats](
    Seq(
      Table.Column("FOR", _.`for`),
      Table.Column("CON", _.con),
      Table.Column("AGI", _.agi),
    ),
    Seq(CharacterStats(7, 3, 8)),
  )

  val b1 = table.draw(0, 10, b0)

  val chrs = Seq("Alice", "Bob", "Cecile")
  val (withShort, shortcuts) = Selection.shortcuts(chrs)
  val mapping = (chrs zip withShort).toMap

  val secondTable = Table[String](
    Seq(Table.Column("Personnage", mapping)),
    chrs,
    Box.Double,
    selected = Some(1)
  )

  val (w, h) = table.size

  val b2 = secondTable.draw(0, h + 10 + 5, b1)

  // AnsiConsole.out().print(b1.reset().restoreCursorPosition())

  println("Selected : " + Selection.select(secondTable, shortcuts))

}
