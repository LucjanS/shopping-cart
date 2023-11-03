package com.siriusxm.example.cart.domain

import cats.kernel.Semigroup

case class Quantity(value: Int) extends AnyVal

object Quantity {

  implicit val quantitySemigroup: Semigroup[Quantity] = new Semigroup[Quantity] {
    def combine(x: Quantity, y: Quantity): Quantity =
      Quantity(x.value + y.value)
  }
}
