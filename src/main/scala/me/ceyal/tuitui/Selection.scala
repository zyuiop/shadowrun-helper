package me.ceyal.tuitui

import jline.console.{ConsoleReader, KeyMap, Operation}
import org.fusesource.jansi.{Ansi, AnsiConsole}

import scala.annotation.tailrec
import scala.io.StdIn
import scala.language.postfixOps

object Selection {

  import sys.process._
  (Seq("sh", "-c", "stty -icanon min 1 < /dev/tty") !)
  (Seq("sh", "-c", "stty -echo < /dev/tty") !)

  def shortcuts(options: Seq[String]): (Seq[String], Map[Char, Int]) = {
    options.zipWithIndex.foldLeft((Seq[String](), Map[Char, Int]())) {
      case ((seq, map), (elem, position)) =>
        // find a char
        val index = elem.toLowerCase.indexWhere(c => !map.keySet(c))

        val transformed = if (index >= 0) {
          elem.take(index) + "[" + elem(index).toUpper + "]" + elem.drop(index + 1)
        } else elem

        (seq :+ transformed, if (index >= 0) map + (elem(index).toLower -> position) else map)
    }
  }

  def select[T](selectable: Selectable[T] with Drawable, shortcuts: Map[Char, Int] = Map()): T = {
    def print(sel: Int) = {
      val b0 = Ansi.ansi().saveCursorPosition().eraseScreen()// .saveCursorPosition().eraseScreen()
      val b1 = selectable.select(Some(sel)).draw(5, 5, b0)
      AnsiConsole.out().println(b1.reset())
    }

    @tailrec
    def printAndRead(sel: Int, buf: String = ""): Int = {
      print(sel)

      System.err.println(System.currentTimeMillis() + " -- " + buf)

      def readInput() = System.in.read().toChar

      (buf + readInput()) match {
        case c if c.length == 1 && shortcuts.contains(c.head.toLower) =>
          shortcuts(c.head)
        case "\u001B[A" =>
          printAndRead(if (sel > 0) sel - 1 else sel)
        case "\u001B[B" =>
          printAndRead(if (sel + 1 < selectable.numChoices) sel + 1 else sel)
        case "\u001B[C" =>
          sel
        case "\n" =>
          sel
        case other =>
          if (other.nonEmpty) {
            System.err.println(other + " -- " + other.getBytes.mkString("Array(", ", ", ")"))
          }
          printAndRead(sel, if (other.length > 3) "" else other)
      }
    }

    val selected = printAndRead(0)
    AnsiConsole.out().println(Ansi.ansi().saveCursorPosition().eraseScreen().reset())
    selectable.option(selected)
  }

}
