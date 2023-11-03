package com.siriusxm.example.cart.domain

import munit.FunSuite

class MoneySuite extends FunSuite {

  test("Rounding up") {
    assertEquals(Money(12).rounded, Money(12))
    assertEquals(Money(13.494).rounded, Money(13.49))
    assertEquals(Money(13.499).rounded, Money(13.5))
    assertEquals(Money(13.5).rounded, Money(13.5))
    assertEquals(Money(13.501).rounded, Money(13.5))
    assertEquals(Money(88.656456).rounded, Money(88.66))
  }

}
