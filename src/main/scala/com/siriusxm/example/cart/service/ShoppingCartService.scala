package com.siriusxm.example.cart.service

import cats.MonadThrow
import cats.effect.kernel.Ref
import cats.syntax.all._
import com.siriusxm.example.cart.domain.Product.ProductName
import com.siriusxm.example.cart.domain.{CartItem, CartTotal, Quantity, ShoppingCart, UserId}

import scala.util.control.NoStackTrace

trait ShoppingCartService[F[_]] {

  def add(userId: UserId, productName: ProductName, quantity: Quantity): F[Unit]

  def getItems(userId: UserId): F[List[CartItem]]

  def calculateTotal(userId: UserId): F[CartTotal]

}

object ShoppingCartService {

  sealed trait ShoppingCartError extends NoStackTrace

  case class CartNotFound(userId: UserId) extends ShoppingCartError {
    override def getMessage: String = s"Cart for user with id ${userId.value} doesn't exist."
  }

  def make[F[_] : MonadThrow : Ref.Make](
    products: Products[F]
  ): F[ShoppingCartService[F]] =
    Ref.of[F, Map[UserId, ShoppingCart]](Map.empty).map { ref =>
      new ShoppingCartService[F] {
        def add(userId: UserId, productName: ProductName, quantity: Quantity): F[Unit] = {
          def checkProduct =
            products.getByName(productName).void

          def updateCart() =
            ref.update { carts =>
              val cart =
                carts
                  .getOrElse(userId, ShoppingCart())
                  .updated(productName, quantity)

              carts.updated(userId, cart)
            }

          checkProduct *> updateCart
        }

        def getItems(userId: UserId): F[List[CartItem]] =
          for {
            carts <- ref.get
            cart <- MonadThrow[F].fromOption(carts.get(userId), CartNotFound(userId))
            items <- cart.items.toList.traverse {
              case (name, quantity) => products.getByName(name).map(CartItem(_, quantity))
            }
          } yield items


        def calculateTotal(userId: UserId): F[CartTotal] =
          getItems(userId)
            .map { items =>
              CartTotal(
                items.foldMap(_.subTotal),
                items.foldMap(_.tax),
                items.foldMap(_.total)
              ).rounded
            }
      }
    }
}
