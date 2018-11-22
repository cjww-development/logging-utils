/*
 * Copyright 2018 CJWW Development
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cjwwdev.logging.encoders

import java.net.InetAddress
import java.nio.charset.StandardCharsets

import ch.qos.logback.classic.spi.{ILoggingEvent, ThrowableProxyUtil}
import ch.qos.logback.core.encoder.EncoderBase
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.ObjectMapper
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.IOUtils.LINE_SEPARATOR
import org.apache.commons.lang3.time.FastDateFormat

import scala.util.Try

class JsonLoggingEncoding extends EncoderBase[ILoggingEvent] {
  private val mapper           = new ObjectMapper().configure(Feature.ESCAPE_NON_ASCII, true)
  private lazy val appName     = Try(ConfigFactory.load().getString("appName")).fold(_ => "", identity)
  private val DATE_FORMAT      = "yyyy-MM-dd HH:mm:ss.SSS"
  private val requestTypeRegex = """^(HEAD|GET|POST|PUT|PATCH|POST) request to (.*) returned a \d{3} and took \d+ms$"""

  private def processLog(message: String): Option[(String, Int, String)]  = message match {
    case x if x.matches("""^(HEAD|GET|POST|PUT|PATCH|DELETE) request to (.*) returned a \d{3} and took \d+ms$""") =>
      val digits = ("""\d+""".r findAllIn x).toList
      val method = ("""(HEAD|GET|POST|PUT|PATCH|DELETE)""".r findAllIn x).toList.head
      Some((method, digits.head.toInt, s"${digits.last}ms"))
    case _ => None
  }

  override def encode(event: ILoggingEvent): Array[Byte] = {
    val eventNode = mapper.createObjectNode()

    val loggingContent: Map[String, String] = Map(
      "timestamp"      -> FastDateFormat.getInstance(DATE_FORMAT).format(event.getTimeStamp),
      "service"        -> appName,
      "hostname"       -> InetAddress.getLocalHost.getHostName,
      "dockerIp"       -> InetAddress.getLocalHost.getHostAddress,
      "serviceVersion" -> System.getProperty("version", "-"),
      "logger"         -> event.getLoggerName,
      "level"          -> event.getLevel.levelStr,
      "thread"         -> event.getThreadName
    )

    processLog(event.getMessage).map { case (method, status, response) =>
      eventNode.put("logType", "response")
      eventNode.put("method", method)
      eventNode.put("status", status)
      eventNode.put("duration", response)
    }.getOrElse {
      eventNode.put("logType", "standard")
      eventNode.put("message", event.getMessage)
    }

    Option(event.getThrowableProxy).map(e => eventNode.put("exception", ThrowableProxyUtil.asString(e)))

    loggingContent.foreach { case (key, content) => eventNode.put(key, content) }
    s"${mapper.writeValueAsString(eventNode)}$LINE_SEPARATOR".getBytes(StandardCharsets.UTF_8)
  }

  override def headerBytes(): Array[Byte] = LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8)
  override def footerBytes(): Array[Byte] = LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8)
}
