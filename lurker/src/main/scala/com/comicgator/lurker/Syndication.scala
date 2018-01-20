package com.comicgator.lurker

import java.time.ZoneId
import java.time.format.DateTimeFormatter

import com.typesafe.scalalogging.LazyLogging

object Syndication extends Conf with LazyLogging {
  def makeFeed(items: Vector[Item]): String = {
    val sortedItems: Vector[Item] =
      items.sortBy(i => (i.comicId, -i.stripNumber))
    val channel: Item = sortedItems.head
    // When feed_strips are inserted they often are batched and have updated_at timestamps indistinguishable at the
    // second precision. When feed_strip's are combined with context and extracted as Items, we use the updated_at as a
    // timestamp for the RFC_1123 pubDate tag. This results in a bunch of Items with the exact same pubDate, which
    // means that time based sorts are useless. Including an index in the map we can use it to add an accumulated amount
    // of seconds to each Item. This has the negative result of slightly adjusting the pubDate each time the feed is
    // generated but I think this will be ignored since most readers rely on the guid tag for uniqueness.
    (
      Vector(channelPrefix(channel))
      ++ sortedItems.zipWithIndex.map { case (item, index) => channelItem(item, index) }
      ++ Vector(channelPostfix)
    ).mkString("\n")
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

  private def channelItem(item: Item, index: Int): String = {
    val pubDate =
      item.feedStripUpdatedAt.minusSeconds(index).atZone(ZoneId.of("UTC")).format(DateTimeFormatter.RFC_1123_DATE_TIME)
    val br = xml.Utility.escape("<br>")
    val header = xml.Utility.escape(s"<p>${item.comicTitle} (${item.stripNumber})</p>")
    val mainImage = xml.Utility.escape(s"""<img src=\"${item.stripImageUrl}\" />""")
    val mouseOver = if (item.stripImageTitle.isEmpty && item.stripImageAlt.isEmpty) {
      ""
    } else if (item.stripImageTitle.isDefined) {
      xml.Utility.escape(s"""<blockquote>${item.stripImageTitle.get}</blockquote>""")
    } else {
      xml.Utility.escape(s"""<blockquote>${item.stripImageAlt.get}</blockquote>""")
    }
    val bonusImage = if (item.stripBonusImageUrl.isEmpty) {
      ""
    } else {
      xml.Utility.escape(s"""<img src=\"${item.stripBonusImageUrl.get}\" />""")
    }
    val description = header + br + mainImage + br + mouseOver + br + bonusImage
    s"""        <item>
       |            <title>${item.stripTitle}</title>
       |            <link>${item.stripUrl}</link>
       |            <description>$description</description>
       |            <pubDate>$pubDate</pubDate>
       |            <guid>${item.stripUrl}</guid>
       |        </item>""".stripMargin
  }
}
