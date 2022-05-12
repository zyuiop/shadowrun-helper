package me.ceyal.srh.ui.reactive

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.gui2._
import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

class ReactiveComponent[T, U <: Component](rv: ReactiveValue[T], map: T => U) extends AbstractComposite[ReactiveComponent] {
  private val listener = new ChangeListener[T] {
    override def onChange(value: T, previous: T): Unit = {
      setComponent(map(value))
    }
  }

  override def onAdded(container: Container): Unit = {
    rv.addListener(listener)
    if (this.getComponent == null) setComponent(map(rv.get))
    super.onAdded(container)
  }

  override def onRemoved(container: Container): Unit = {
    rv.removeListener(listener)
    super.onRemoved(container)
  }

  override protected def createDefaultRenderer: ComponentRenderer[Container] = {
    new ComponentRenderer[Container]() {
      override def getPreferredSize(component: Container): TerminalSize = {
        val subComponent: Component = ReactiveComponent.this.getComponent

        if (subComponent == null) TerminalSize.ZERO
        else subComponent.getPreferredSize
      }

      override def drawComponent(graphics: TextGUIGraphics, component: Container): Unit = {
        val subComponent: Component = ReactiveComponent.this.getComponent
        if (subComponent != null) subComponent.draw(graphics)
      }
    }
  }
}
