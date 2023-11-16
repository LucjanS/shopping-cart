package com.siriusxm.example.cart.service

import cats.effect.IO
import cats.implicits._
import com.siriusxm.example.cart.domain.Product.{ProductName, ProductTitle}
import com.siriusxm.example.cart.domain._
import com.siriusxm.example.cart.service.Products.ProductNotFound
import com.siriusxm.example.cart.service.ShoppingCartService.CartNotFound
import munit.CatsEffectSuite

class ShoppingCartServiceSuite extends CatsEffectSuite {

  val cornflakes = Product(ProductName("cornflakes"), ProductTitle("Corn Flakes"), Money(2.52))
  val weetabix = Product(ProductName("weetabix"), ProductTitle("Weetabix"), Money(9.98))

  test("Adding products") {
    val products = new TestProducts(cornflakes :: weetabix :: Nil)

    for {
      cart <- ShoppingCartService.make[IO](products)
      userA = UserId.random
      userB = UserId.random
      _ <- cart.add(userA, cornflakes.name, Quantity(2))
      _ <- cart.add(userA, weetabix.name, Quantity(1))
      _ <- cart.add(userA, cornflakes.name, Quantity(3))
      _ <- cart.add(userB, weetabix.name, Quantity(1))
      itemsForUserA <- cart.getItems(userA)
      itemsForUserB <- cart.getItems(userB)
    } yield {
      assertEquals(itemsForUserA, List(CartItem(cornflakes, Quantity(5)), CartItem(weetabix, Quantity(1))))
      assertEquals(itemsForUserB, List(CartItem(weetabix, Quantity(1))))
    }
  }

  test("Calculating totals") {
    val products = new TestProducts(cornflakes :: weetabix :: Nil)

    for {
      cart <- ShoppingCartService.make[IO](products)
      userA = UserId.random
      userB = UserId.random
      _ <- cart.add(userA, cornflakes.name, Quantity(2))
      _ <- cart.add(userA, weetabix.name, Quantity(1))
      _ <- cart.add(userB, weetabix.name, Quantity(1))
      totalsForUserA <- cart.calculateTotal(userA)
      totalsForUserB <- cart.calculateTotal(userB)
    } yield {
      assertEquals(totalsForUserA, CartTotal(subTotal = Money(15.02), tax = Money(1.88), total = Money(16.90)))
      assertEquals(totalsForUserB, CartTotal(subTotal = Money(9.98), tax = Money(1.25), total = Money(11.23)))
    }
  }

  test("Adding products fails if product doesn't exist") {
    for {
      userId <- UserId.random.pure[IO]
      cart <- ShoppingCartService.make[IO](new TestProducts(Nil))
      result <- cart.add(userId, cornflakes.name, Quantity(2)).attempt
      expected = ProductNotFound(cornflakes.name).asLeft
    } yield {
      assertEquals(result, expected)
    }
  }

  test("Fetching products fails if user doesn't exist") {
    val products = new TestProducts(cornflakes :: weetabix :: Nil)

    for {
      userA <- UserId.random.pure[IO]
      userB <- UserId.random.pure[IO]
      cart <- ShoppingCartService.make[IO](products)
      _ <- cart.add(userA, cornflakes.name, Quantity(2))
      result <- cart.getItems(userB).attempt
      expected = CartNotFound(userB).asLeft
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
