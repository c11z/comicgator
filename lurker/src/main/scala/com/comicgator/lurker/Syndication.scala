package com.comicgator.lurker

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging

object Syndication extends Conf with LazyLogging {
  def makeFeed(items: Vector[Item]): String = {
    val sortedItems: Vector[Item] =
      items.sortBy(- _.stripNumber)
    val channel = sortedItems.head
    val title = s"${channel.comicTitle} RSS Feed by Comic Gator"
    (Vector(channelPrefix(title)) ++ sortedItems.map(channelItem) ++ Vector(
      channelPostfix)).mkString("\n")
  }

  private def channelPrefix(title: String): String =
    s"""<?xml version="1.0" encoding="utf-8"?>
       |<rss version="2.0">
       |    <channel>
       |        <title>$title</title>
       |        <link>http://comicgator.com</link>
       |        <description></description>
       |        <webMaster>mr@comicgator.com</webMaster>
       |        <language>en</language>
       |        <ttl>60</ttl>""".stripMargin

  private def channelPostfix: String =
    """    </channel>
      |</rss>
      |""".stripMargin

  private def channelItem(item: Item): String = {
    val pubDate = item.feedStripUpdatedAt
      .atZone(ZoneId.of("UTC"))
      .format(DateTimeFormatter.RFC_1123_DATE_TIME)
    s"""        <item>
       |            <title>${item.comicTitle}: ${item.stripTitle}</title>
       |            <link>${item.stripUrl}</link>
       |            <pubDate>$pubDate</pubDate>
       |            <guid>${item.stripUrl}</guid>
       |        </item>""".stripMargin
  }
}
