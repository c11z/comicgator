package com.comicgator.lurker

import com.typesafe.scalalogging.LazyLogging

object Syndication extends Conf with LazyLogging {
  def makeFeed(feedStrips: Vector[RSS]):String = {
    """
      |<?xml version="1.0" encoding="utf-8"?>
      |<rss version="2.0">
      |    <channel>
      |        <title>xkcd.com</title>
      |        <link>https://xkcd.com/</link>
      |        <description>xkcd.com: A webcomic of romance and math humor.</description>
      |        <language>en</language>
      |        <item>
      |            <title>Self Driving</title>
      |            <link>https://xkcd.com/1897/</link>
      |            <description>&lt;img src="https://imgs.xkcd.com/comics/self_driving.png" title="&amp;quot;Crowdsourced steering&amp;quot; doesn't sound quite as appealing as &amp;quot;self driving.&amp;quot;" alt="&amp;quot;Crowdsourced steering&amp;quot; doesn't sound quite as appealing as &amp;quot;self driving.&amp;quot;" /&gt;</description>
      |            <pubDate>Mon, 02 Oct 2017 04:00:00 -0000</pubDate>
      |            <guid>https://xkcd.com/1897/</guid>
      |        </item>
      |        <item>
      |            <title>Active Ingredients Only</title>
      |            <link>https://xkcd.com/1896/</link>
      |            <description>&lt;img src="https://imgs.xkcd.com/comics/active_ingredients_only.png" title="Contains the active ingredients from all competing cold medicines, plus the medicines for headaches, arthritis, insomnia, indigestion, and more, because who wants THOSE things?" alt="Contains the active ingredients from all competing cold medicines, plus the medicines for headaches, arthritis, insomnia, indigestion, and more, because who wants THOSE things?" /&gt;</description>
      |            <pubDate>Fri, 29 Sep 2017 04:00:00 -0000</pubDate>
      |            <guid>https://xkcd.com/1896/</guid>
      |        </item>
      |        <item>
      |            <title>Worrying Scientist Interviews</title>
      |            <link>https://xkcd.com/1895/</link>
      |            <description>&lt;img src="https://imgs.xkcd.com/comics/worrying_scientist_interviews.png" title="They always try to explain that they're called 'solar physicists', but the reporters interrupt with &amp;quot;NEVER MIND THAT, TELL US WHAT'S WRONG WITH THE SUN!&amp;quot;" alt="They always try to explain that they're called 'solar physicists', but the reporters interrupt with &amp;quot;NEVER MIND THAT, TELL US WHAT'S WRONG WITH THE SUN!&amp;quot;" /&gt;</description>
      |            <pubDate>Wed, 27 Sep 2017 04:00:00 -0000</pubDate>
      |            <guid>https://xkcd.com/1895/</guid>
      |        </item>
      |        <item>
      |            <title>Real Estate</title>
      |            <link>https://xkcd.com/1894/</link>
      |            <description>&lt;img src="https://imgs.xkcd.com/comics/real_estate.png" title="I tried converting the prices into pizzas, to put it in more familiar terms, and it just became a hard-to-think-about number of pizzas." alt="I tried converting the prices into pizzas, to put it in more familiar terms, and it just became a hard-to-think-about number of pizzas." /&gt;</description>
      |            <pubDate>Mon, 25 Sep 2017 04:00:00 -0000</pubDate>
      |            <guid>https://xkcd.com/1894/</guid>
      |        </item>
      |    </channel>
      |</rss>
    """.stripMargin
  }

  private def channel(title: String, link: String, description: String): String = ???

  private def item(rss: RSS): String = ???
}
