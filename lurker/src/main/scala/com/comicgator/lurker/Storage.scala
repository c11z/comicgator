package com.comicgator.lurker

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import com.google.cloud.storage.{Blob, Bucket, StorageOptions}
import com.typesafe.scalalogging.LazyLogging
import org.bson.types.ObjectId

object Storage extends Conf with LazyLogging {
  private val storage = StorageOptions.getDefaultInstance.getService
  private val bucket: Bucket = storage.get(FEED_STORAGE_BUCKET)

  def putFeed(feedId: ObjectId, content: String): Blob = {
    val contentBytes = content.getBytes("UTF-8")
    val bos = new ByteArrayOutputStream(contentBytes.length)
    val gzip = new GZIPOutputStream(bos)
    gzip.write(contentBytes)
    gzip.close()
    val compressed = bos.toByteArray
    bos.close()
    bucket
      .create(s"${feedId.toString}/rss.xml",
              compressed,
              "application/xml; charset=UTF-8")
      .toBuilder
      .setContentEncoding("gzip")
      .setCacheControl("public,max-age=3600")
      .build()
      .update()
  }
}
