package com.example.chapter5_news

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "channel")
data class RssChannel(
    @PropertyElement(name = "title")
    val title: String,

    @Element(name = "item")
    val items: List<NewsItems>? = null,
)

@Xml(name = "item")
data class NewsItems(
    @PropertyElement(name = "title")
    val title: String? = null,
    @PropertyElement(name = "link")
    val link: String? = null,
)