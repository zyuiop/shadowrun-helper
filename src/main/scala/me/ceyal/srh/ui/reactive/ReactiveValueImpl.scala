package me.ceyal.srh.ui.reactive

import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

class ReactiveValueImpl[T](initial: T) extends ReactiveValue[T] {
  private var underlying: T = initial
  private var listeners: Set[ChangeListener[T]] = Set()

  def get: T = underlying

  def set(other: T): ReactiveValueImpl[T] = {
    if (get == other) return this

    val previous = underlying
    underlying = other
    listeners.foreach(_.onChange(other, previous))
    this
  }

  def addListener(listener: ChangeListener[T]): ReactiveValueImpl[T] = {
    listeners = listeners ++ Set(listener)
    listener.onChange(get, get)
    this
  }

  def removeListener(listener: ChangeListener[T]): ReactiveValueImpl[T] = {
    listeners = listeners.filterNot(_ == listener)
    this
  }
}
