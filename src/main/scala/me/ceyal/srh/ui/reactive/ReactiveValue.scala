package me.ceyal.srh.ui.reactive

import com.googlecode.lanterna.gui2.Component
import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

import scala.collection.mutable

object ReactiveValue {
  trait ChangeListener[T] {
    def onChange(value: T, previous: T)
  }

  def of[T](t: T): ReactiveValue[T] = new ReactiveValueImpl[T](t)

  def immutable[T](t: T): ReactiveValue[T] = new ReactiveValue[T] {
    override def get: T = t

    override def set(other: T): ReactiveValue[T] = throw new UnsupportedOperationException()

    override def addListener(listener: ChangeListener[T]): ReactiveValue[T] = {
      listener.onChange(t, t)
      this
    }

    override def removeListener(listener: ChangeListener[T]): ReactiveValue[T] = this
  }
}

trait ReactiveValue[T] {
  def get: T

  def set(other: T): ReactiveValue[T]

  def update(mapping: T => T): ReactiveValue[T] = set(mapping(get))

  def addListener(listener: ChangeListener[T]): ReactiveValue[T]

  def removeListener(listener: ChangeListener[T]): ReactiveValue[T]

  def map[U](mapping: T => U, updater: (T, U) => T = (a: T, _: U) => a): ReactiveValue[U] = {
    val parent = this
    new ReactiveValue[U] {
      private val listeners = mutable.Map[ChangeListener[U], ChangeListener[T]]()

      override def get: U = mapping(parent.get)

      override def set(other: U): ReactiveValue[U] = {
        parent.update((t: T) => updater(t, other))
        this
      }

      override def addListener(listener: ChangeListener[U]): ReactiveValue[U] = {
        val tListener = listeners.getOrElseUpdate(listener, (nv, pv) => {
          val (nu, pu) = (mapping(nv), mapping(pv))

          if (nu != pu) listener.onChange(nu, pu)
        })
        parent.addListener(tListener)
        listener.onChange(mapping(parent.get), mapping(parent.get))
        this
      }

      override def removeListener(listener: ChangeListener[U]): ReactiveValue[U] = {
        listeners.remove(listener).foreach(parent.removeListener)
        this
      }
    }
  }

  def ==>[U <: Component](map: T => U) = new ReactiveComponent[T, U](this, map)

  def <==(other: ReactiveValue[T]): ReactiveValue[T] = {
    other.addListener((nv, _) => this.set(nv))
    this
  }
}
