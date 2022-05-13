package me.ceyal.srh.ui.reactive

import com.googlecode.lanterna.gui2.{Component, Container}
import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

import scala.collection.mutable

trait ReactiveComponentTool extends Component {
  private val unregisters = mutable.Stack[() => Unit]()
  private val registers = mutable.Stack[() => Unit]()

  /**
   * Returns a function that can be used to retrieve the current value of a given reactive value.
   * Hooks the reactive value so that the component is redrawn when it changes
   */
  def use[T](rv: ReactiveValue[T]): () => T = {
    val listener: ChangeListener[T] = (_, _) => invalidate()
    rv.addListener(listener)
    unregisters.addOne(() => rv.removeListener(listener))
    registers.addOne(() => rv.addListener(listener))

    () => rv.get
  }

  override abstract def onRemoved(var1: Container): Unit = {
    super.onRemoved(var1)
    unregisters.foreach(_())
  }

  override abstract def onAdded(var1: Container): Unit = {
    super.onAdded(var1)
    registers.foreach(_())
  }

}
