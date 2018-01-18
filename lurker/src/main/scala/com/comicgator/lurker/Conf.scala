package com.comicgator.lurker

import java.time.LocalDateTime

import com.typesafe.config.ConfigFactory

/**
  * Configuration trait loads contents from application.conf file
  * and makes them available as a mixin.
  */
trait Conf {
  private val config = ConfigFactory.load()
  val ETL_BATCH_SIZE: Int = config.getInt("etl_batch_size")
  val IS_DELTA: Boolean = config.getBoolean("is_delta")
  val FEED_STORAGE_BUCKET: String = config.getString("feed_storage_bucket")
  val INTERLUDE: Long = config.getLong("interlude")
}
