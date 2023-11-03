package com.siriusxm.example.cart.service

import cats.effect.IO
import com.siriusxm.example.cart.domain.{Money, Product, ProductResponse}
import com.siriusxm.example.cart.domain.Product.{ProductName, ProductTitle}
import com.siriusxm.example.cart.service.Products.ProductNotFound
import munit.CatsEffectSuite
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.client.Client
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.io._

class ProductsSuite extends CatsEffectSuite {

  val product = Product(ProductName("cornflakes"), ProductTitle("Corn Flakes"), Money(2.52))

  def routes(name: ProductName)(mkResponse: IO[Response[IO]]) =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "mattjanks16" / "shopping-cart-test-data" / "main" / s"${name.value}.json" => mkResponse
      }
      .orNotFound

  test("Fetching the product") {
    val response = Ok(ProductResponse(product.title, product.price))
    val client = Client.fromHttpApp(routes(product.name)(response))

    Products
      .make(client)
      .getByName(product.name)
      .assertEquals(product)
  }

  test("Failing with ProductNotFound") {
    val client = Client.fromHttpApp(routes(product.name)(NotFound()))

    Products
      .make(client)
      .getByName(product.name)
      .attempt
      .map {
        case Left(ProductNotFound(name)) => assertEquals(name, product.name)
        case _ => fail("Expected ProductNotFound")
      }
  }

}
