package com.siriusxm.example.cart

import cats.effect.{IO, IOApp}
import cats.syntax.applicative._
import com.siriusxm.example.cart.domain.Product.ProductName
import com.siriusxm.example.cart.domain.{Quantity, UserId}
import com.siriusxm.example.cart.service.{Products, ShoppingCartService}
import org.http4s.ember.client.EmberClientBuilder


object Main extends IOApp.Simple {

  def run: IO[Unit] =
    EmberClientBuilder.default[IO].build.use { client =>
      for {
        userId <- UserId.random.pure[IO]
        products <- Products.make[IO](client).pure[IO]
        cart <- ShoppingCartService.make[IO](products)
        _ <- cart.add(userId, ProductName("cornflakes"), Quantity(2))
        _ <- cart.add(userId, ProductName("weetabix"), Quantity(1))
        total <- cart.calculateTotal(userId)
        _ <- IO.println(
          s"""
             |Cart subtotal: ${total.subTotal.value}
             |Tax payable:   ${total.tax.value}
             |Total payable: ${total.total.value}""".stripMargin)
      } yield ()
    }
}
