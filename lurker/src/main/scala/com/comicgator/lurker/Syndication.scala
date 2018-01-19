package com.comicgator.lurker

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging

object Syndication extends Conf with LazyLogging {
  def makeFeed(items: Vector[Item]): String = {
    val sortedItems: Vector[Item] =
      items.sortBy(-_.stripNumber)
    val channel: Item = sortedItems.head
    (Vector(channelPrefix(channel)) ++ sortedItems.map(channelItem) ++ Vector(
      channelPostfix)).mkString("\n")
  }

  private def channelPrefix(channel: Item): String = {
    s"""<?xml version="1.0" encoding="utf-8"?>
       |<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
       |    <channel>
       |        <atom:link href="http://feed.comicgator.com/${channel.feedId}/rss.xml" rel="self" type="application/rss+xml" />
       |        <title>${channel.feedName}</title>
       |        <link>http://comicgator.com</link>
       |        <description></description>
       |        <webMaster>mr@comicgator.com (Mr. Comic Gator)</webMaster>
       |        <language>en</language>""".stripMargin
  }

  private def channelPostfix: String =
    """    </channel>
      |</rss>
      |""".stripMargin

  private def channelItem(item: Item): String = {
    val pubDate = item.feedStripUpdatedAt
      .atZone(ZoneId.of("UTC"))
      .format(DateTimeFormatter.RFC_1123_DATE_TIME)
    val br = xml.Utility.escape("<br>")
    val mainImage = xml.Utility.escape(s"""<img src=\"${item.stripImageUrl}\" />""")
    val mouseOver = if (item.stripImageTitle.isEmpty && item.stripImageAlt.isEmpty) {
      ""
    } else if (item.stripImageTitle.isEmpty) {
      xml.Utility.escape(s"""<blockquote>${item.stripImageAlt}</blockquote>""")
    } else {
      xml.Utility.escape(s"""<blockquote>${item.stripImageTitle}</blockquote>""")
    }
    val bonusImage = if (item.stripBonusImageUrl.isEmpty) {
      ""
    } else {
      xml.Utility.escape(s"""<img src=\"${item.stripBonusImageUrl}\" />""")
    }
    val description = mainImage + br + mouseOver + br + bonusImage
    s"""        <item>
       |            <title>${item.stripTitle}</title>
       |            <link>${item.stripUrl}</link>
       |            <description>$description</description>
       |            <pubDate>$pubDate</pubDate>
       |            <guid>${item.stripUrl}</guid>
       |        </item>""".stripMargin
  }
}
