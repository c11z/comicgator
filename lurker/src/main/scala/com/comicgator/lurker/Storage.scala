package com.comicgator.lurker

import com.google.cloud.storage.{Blob, Bucket, StorageOptions}
import org.bson.types.ObjectId

object Storage extends Conf {
  private val storage = StorageOptions.getDefaultInstance.getService
  private val bucket: Bucket = storage.get(FEED_STORAGE_BUCKET)

  def putFeed(feedId: ObjectId, content: String): Blob = {
    bucket.create(s"${feedId.toString}/rss.xml",
                  content.getBytes("UTF-8"),
                  "application/xml; charset=UTF-8")
  }
}
