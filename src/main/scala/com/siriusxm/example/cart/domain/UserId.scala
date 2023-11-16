package com.siriusxm.example.cart.domain

import java.util.UUID

case class UserId(value: UUID) extends AnyVal
object UserId {
  def random: UserId = UserId(UUID.randomUUID())
}
