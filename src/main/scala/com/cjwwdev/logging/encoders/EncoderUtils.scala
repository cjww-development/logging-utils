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

import play.api.http.HttpVerbs

sealed trait LogType
case class RequestLog(method: String, status: Int, duration: String) extends LogType
case class OutboundLog(method: String, status: Int, outboundHost: String) extends LogType
case object StandardLog extends LogType

trait EncoderUtils {
  self: EncoderConfig =>

  protected def getLogType(message: String): LogType = message match {
    case x if x.matches(requestTypeRegex)  => createRequestLog(x)
    case x if x.matches(outboundTypeRegex) => createOutboundLog(x)
    case _                                 => StandardLog
  }

  private val httpVerbs = List(HttpVerbs.HEAD, HttpVerbs.GET, HttpVerbs.POST, HttpVerbs.PUT, HttpVerbs.PATCH, HttpVerbs.DELETE, HttpVerbs.OPTIONS)

  private def createRequestLog(message: String): RequestLog = {
    val splitMessage = message.split(" ").toList
    val method       = splitMessage.find(httpVerbs.contains).getOrElse("-")
    val status       = splitMessage(splitMessage.length - 4).toInt
    RequestLog(method, status, splitMessage.last)
  }

  private def createOutboundLog(message: String): OutboundLog = {
    val splitMessage = message.split(" ").toList
    val method       = splitMessage.find(httpVerbs.contains).getOrElse("-")
    val outboundHost = splitMessage
      .find(_.matches(outboundTypeRegex))
      .map(_.replace("http://", "").replace("https://", "").replace(""":\d{4}""", ""))
      .getOrElse("-")
    OutboundLog(method, splitMessage.last.toInt, outboundHost)
  }

  protected def getRequestIdFromMessage(msg: String): String = {
    requestIdRegex.r
      .findAllIn(msg)
      .toList
      .headOption
      .map(_.replace("requestId=[", "").replace("]", ""))
      .getOrElse("-")
  }
}
