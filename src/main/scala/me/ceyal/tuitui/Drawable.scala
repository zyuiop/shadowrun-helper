package me.ceyal.tuitui

import org.fusesource.jansi.{Ansi, AnsiConsole, AnsiPrintStream}

trait Drawable {
  def draw(x: Int, y: Int, term: Ansi): Ansi

  def size: (Int, Int)
}
