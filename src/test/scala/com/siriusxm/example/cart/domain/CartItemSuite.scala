package com.siriusxm.example.cart.domain

import com.siriusxm.example.cart.domain.Product.{ProductName, ProductTitle}
import munit.FunSuite

class CartItemSuite extends FunSuite {

  private val TaxRate = 12.5 / 100

  test("Calculating totals") {
    val price = Money(98.67)
    val quantity = Quantity(2)
    val item = CartItem(Product(ProductName("foo"), ProductTitle("title"), price), quantity)

    assertEquals(item.subTotal.value, price.value * quantity.value)
    assertEquals(item.tax.value, item.subTotal.value * TaxRate)
    assertEquals(item.total.value, item.subTotal.value + item.tax.value)
  }
}
