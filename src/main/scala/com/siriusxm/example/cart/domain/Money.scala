package com.siriusxm.example.cart.domain

import cats.kernel.Monoid
import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

import scala.math.BigDecimal.RoundingMode


case class Money(value: BigDecimal) extends AnyVal {
  def rounded: Money = Money(value.setScale(2, RoundingMode.HALF_UP))
}

object Money {

  implicit val codec: Codec[Money] = deriveUnwrappedCodec

  implicit val moneyMonoid: Monoid[Money] = new Monoid[Money] {
    def empty: Money = Money(0.0)

    def combine(a: Money, b: Money): Money =
      Money(a.value + b.value)
  }
}
