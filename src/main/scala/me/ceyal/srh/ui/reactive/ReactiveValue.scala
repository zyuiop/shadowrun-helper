package me.ceyal.srh.ui.reactive

import com.googlecode.lanterna.gui2.{Component, Panel}
import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

object ReactiveValue {
  trait ChangeListener[T] {
    def onChange(value: T, previous: T)
  }
}

class ReactiveValue[T](initial: T) {
  private var underlying: T = initial
  private var listeners: List[ChangeListener[T]] = List()

  def get: T = underlying

  def set(other: T) = {
    val previous = underlying
    underlying = other
    listeners.foreach(_.onChange(other, previous))
  }

  def addListener(listener: ChangeListener[T]): ReactiveValue[T] = {
    listeners = listener :: listeners
    listener.onChange(get, get)
    this
  }

  def removeListener(listener: ChangeListener[T]): ReactiveValue[T] = {
    listeners = listeners.filterNot(_ == listener)
    this
  }

  def use(body: T => Component) = {
    val panel = new Panel()
    panel.addComponent(body(underlying))
    addListener((value, _) => {
      panel.removeAllComponents()
      panel.addComponent(body(value))
    })

    panel
  }

  def ==>[U <: Component](map: T => U) = new ReactiveComponent[T, U](this, map)
}
