package com.siriusxm.example.cart.domain

import cats.syntax.all._
import com.siriusxm.example.cart.domain.Product.ProductName


case class ShoppingCart(
  items: Map[ProductName, Quantity] = Map.empty
) {
  def updated(productName: ProductName, quantity: Quantity): ShoppingCart = {
    val newItems = items.updatedWith(productName) {
      case Some(currentQuantity) => (currentQuantity |+| quantity).some
      case None => quantity.some
    }

    ShoppingCart(newItems)
  }
}