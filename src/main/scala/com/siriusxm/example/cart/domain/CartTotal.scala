package com.siriusxm.example.cart.domain

case class CartTotal(
  subTotal: Money,
  tax: Money,
  total: Money
) {

  def rounded: CartTotal =
    CartTotal(
      subTotal.rounded,
      tax.rounded,
      total.rounded
    )
}