package me.ceyal.srh.random

import scala.util.Random

object Dices {
  def launchDices(n: Int) = {
    (0 until n) map (_ => Random.nextInt(6) + 1)
  }
}
