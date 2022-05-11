package me.ceyal.tuitui

trait Selectable[T] {
  def select(option: Option[Int]): Selectable[T] with Drawable

  val numChoices: Int

  def option(index: Int): T

  val selectedOption: T
  val selectedIndex: Int
}
