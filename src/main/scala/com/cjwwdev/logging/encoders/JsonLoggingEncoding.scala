/*
 * Copyright 2019 CJWW Development
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
import org.apache.commons.io.IOUtils.LINE_SEPARATOR
import org.apache.commons.lang3.time.FastDateFormat

class JsonLoggingEncoding extends EncoderBase[ILoggingEvent] with EncoderConfig with EncoderUtils {
  private val mapper = new ObjectMapper().configure(Feature.ESCAPE_NON_ASCII, true)

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
      "thread"         -> event.getThreadName,
      "message"        -> event.getMessage.replaceAll(requestIdRegex, "").trim
    )

    getLogType(event.getMessage.replaceAll(requestIdRegex, "").trim) match {
      case RequestLog(method, status, duration) =>
        eventNode.put("logType", "request")
        eventNode.put("method", method)
        eventNode.put("status", status)
        eventNode.put("duration", duration)
      case OutboundLog(method, status, outboundHost) =>
        eventNode.put("logType", "outbound")
        eventNode.put("method", method)
        eventNode.put("status", status)
        eventNode.put("outboundHost", outboundHost)
      case StandardLog =>
        eventNode.put("logType", "standard")
    }

    eventNode.put("requestId", getRequestIdFromMessage(event.getMessage))

    Option(event.getThrowableProxy).map(e => eventNode.put("exception", ThrowableProxyUtil.asString(e)))

    loggingContent.foreach { case (key, content) => eventNode.put(key, content) }
    s"${mapper.writeValueAsString(eventNode)}$LINE_SEPARATOR".getBytes(StandardCharsets.UTF_8)
  }

  override def headerBytes(): Array[Byte] = LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8)
  override def footerBytes(): Array[Byte] = LINE_SEPARATOR.getBytes(StandardCharsets.UTF_8)
}
