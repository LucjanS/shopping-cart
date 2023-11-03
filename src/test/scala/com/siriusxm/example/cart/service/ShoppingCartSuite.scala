package com.siriusxm.example.cart.service

import cats.effect.IO
import cats.implicits._
import com.siriusxm.example.cart.domain.Product.{ProductName, ProductTitle}
import com.siriusxm.example.cart.domain._
import com.siriusxm.example.cart.service.Products.ProductNotFound
import munit.CatsEffectSuite

class ShoppingCartSuite extends CatsEffectSuite {

  val cornflakes = Product(ProductName("cornflakes"), ProductTitle("Corn Flakes"), Money(2.52))
  val weetabix = Product(ProductName("weetabix"), ProductTitle("Weetabix"), Money(9.98))

  test("Adding products") {
    val products = new TestProducts(cornflakes :: weetabix :: Nil)

    for {
      cart <- ShoppingCart.make[IO](products)
      _ <- cart.add(cornflakes.name, Quantity(2))
      _ <- cart.add(weetabix.name, Quantity(1))
      _ <- cart.add(cornflakes.name, Quantity(3))
      _ <- cart.add(weetabix.name, Quantity(1))
      obtained <- cart.getItems
    } yield {
      val expected = CartItem(cornflakes, Quantity(5)) :: CartItem(weetabix, Quantity(2)) :: Nil
      assertEquals(obtained, expected)
    }
  }

  test("Calculating totals") {
    val products = new TestProducts(cornflakes :: weetabix :: Nil)

    for {
      cart <- ShoppingCart.make[IO](products)
      _ <- cart.add(cornflakes.name, Quantity(2))
      _ <- cart.add(weetabix.name, Quantity(1))
      actual <- cart.calculateTotal
      expected = CartTotal(subTotal = Money(15.02), tax = Money(1.88), total = Money(16.90))
    } yield {
      assertEquals(actual, expected)
    }
  }

  test("Adding products fails if product doesn't exists") {
    for {
      cart <- ShoppingCart.make[IO](new TestProducts(Nil))
      result <- cart.add(cornflakes.name, Quantity(2)).attempt
      expected = ProductNotFound(cornflakes.name).asLeft
    } yield {
      assertEquals(result, expected)
    }
  }

}

protected class TestProducts(products: List[Product]) extends Products[IO] {
  def getByName(name: ProductName): IO[Product] =
    products
      .find(_.name == name)
      .fold(ProductNotFound(name).raiseError[IO, Product])(_.pure[IO])
}
