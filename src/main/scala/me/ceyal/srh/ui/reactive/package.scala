package me.ceyal.srh.ui

import me.ceyal.srh.ui.reactive.ReactiveValue.ChangeListener

package object reactive {
  implicit class ReactiveListOps[T](rl: ReactiveValue[List[T]]) {
    val values: Seq[ReactiveValue[T]] = rl.get.indices.map(index => {
      rl.map(_ (index), (oldList, newValue) => oldList.take(index) ::: newValue :: oldList.drop(index + 1))
    })
  }
}
