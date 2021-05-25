package com.evolutiongaming.bootcamp.applications.dto
import io.circe.generic.JsonCodec

@JsonCodec
final case class ApplicationSubmitDto(
  solutionMessage: String,
)
