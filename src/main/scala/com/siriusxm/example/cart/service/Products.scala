package com.siriusxm.example.cart.service

import cats.effect.Async
import cats.syntax.all._
import com.siriusxm.example.cart.domain.{Product, ProductResponse}
import com.siriusxm.example.cart.domain.Product.ProductName
import org.http4s.{Status, Uri}
import org.http4s.circe.jsonOf
import org.http4s.client._

import scala.util.control.NoStackTrace

trait Products[F[_]] {
  def getByName(name: ProductName): F[Product]
}

object Products {

  def make[F[_] : Async](
    client: Client[F]
  ): Products[F] =
    new Products[F] {

      private val baseUri = "https://raw.githubusercontent.com/mattjanks16/shopping-cart-test-data/main/"

      def getByName(name: ProductName): F[Product] =
        Uri
          .fromString(baseUri + name.value + ".json")
          .liftTo[F]
          .flatMap { uri =>
            client
              .expect(uri)(jsonOf[F, ProductResponse])
              .map(resp => Product(name, resp.title, resp.price))
              .adaptError {
                case UnexpectedStatus(status, _, _) if status == Status.NotFound => ProductNotFound(name)
              }
          }
    }

  case class ProductNotFound(name: ProductName) extends NoStackTrace {
    override def getMessage: String = s"Product with name ${name.value} doesn't exist."
  }
}
