package com.siriusxm.example.cart.domain

import com.siriusxm.example.cart.domain.Product.{ProductName, ProductTitle}
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec
import io.circe.{Codec, Decoder}


case class ProductResponse(
  title: ProductTitle,
  price: Money
)

object ProductResponse {
  implicit val jsonDecoder: Decoder[ProductResponse] =
    Decoder.forProduct2(
      "title",
      "price"
    )(ProductResponse.apply)
}

case class Product(
  name: ProductName,
  title: ProductTitle,
  price: Money
)

object Product {
  case class ProductName(value: String) extends AnyVal

  object ProductName {
    implicit val codec: Codec[ProductName] = deriveUnwrappedCodec
  }

  case class ProductTitle(value: String) extends AnyVal

  object ProductTitle {
    implicit val codec: Codec[ProductTitle] = deriveUnwrappedCodec
  }
}
