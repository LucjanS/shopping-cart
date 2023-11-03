package com.siriusxm.example.cart.service

import cats.Monad
import cats.effect.kernel.Ref
import cats.syntax.all._
import com.siriusxm.example.cart.domain.Product.ProductName
import com.siriusxm.example.cart.domain.{CartItem, CartTotal, Quantity}

trait ShoppingCart[F[_]] {
  def add(productName: ProductName, quantity: Quantity): F[Unit]

  def getItems: F[List[CartItem]]

  def calculateTotal: F[CartTotal]
}

object ShoppingCart {

  def make[F[_] : Ref.Make : Monad](
    products: Products[F]
  ): F[ShoppingCart[F]] = {
    Ref.of[F, Map[ProductName, Quantity]](Map.empty).map { ref =>
      new ShoppingCart[F] {
        def add(
          productName: ProductName,
          quantity: Quantity
        ): F[Unit] =
          ref.update(
            _.updatedWith(productName) {
              case Some(currentQuantity) => (currentQuantity |+| quantity).some
              case None => quantity.some
            })

        def calculateTotal: F[CartTotal] =
          getItems.map { items =>
            CartTotal(
              items.foldMap(_.subTotal),
              items.foldMap(_.tax),
              items.foldMap(_.total)
            ).rounded
          }

        def getItems: F[List[CartItem]] =
          ref.get.flatMap {
            _.toList.traverse {
              case (name, quantity) =>
                products
                  .getByName(name)
                  .map(CartItem(_, quantity))
            }
          }
      }
    }
  }
}
