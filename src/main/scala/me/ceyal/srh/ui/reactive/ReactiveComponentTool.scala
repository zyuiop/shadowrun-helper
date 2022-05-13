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
    listen(rv, (_: T, _: T) => invalidate())
    () => rv.get
  }

  def listen[T](rv: ReactiveValue[T], changeListener: ChangeListener[T]): Unit = {
    rv.addListener(changeListener)
    unregisters.addOne(() => rv.removeListener(changeListener))
    registers.addOne(() => rv.addListener(changeListener))
  }

  override abstract def onRemoved(var1: Container): Unit = {
    super.onRemoved(var1)
    unregisters.foreach(_ ())
  }

  override abstract def onAdded(var1: Container): Unit = {
    super.onAdded(var1)
    registers.foreach(_ ())
  }

}
