package models

sealed trait LineCookMessage

case object NoOpLineCook extends LineCookMessage

case object RunLatest extends LineCookMessage

case object SetupCron extends LineCookMessage

case object RunReplay extends LineCookMessage
