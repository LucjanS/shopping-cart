package com.siriusxm.example.cart.domain

import cats.syntax.semigroup._

case class CartItem(
  product: Product,
  quantity: Quantity
) {
  def subTotal: Money = Money(product.price.value * quantity.value)

  def tax: Money = Money(subTotal.value * TaxRate)

  def total: Money = subTotal |+| tax
}
