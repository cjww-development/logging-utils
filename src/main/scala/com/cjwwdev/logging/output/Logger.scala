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

package com.cjwwdev.logging.output

import org.slf4j.{Logger => SLF4JLogger, LoggerFactory}
import play.api.mvc.Request

trait Logger {

  private val logger: SLF4JLogger = LoggerFactory.getLogger(this.getClass)

  private def getRequestId(implicit request: Request[_]): String = {
    request.headers.get("requestId").getOrElse("-")
  }

  object LogAt {
    def debug(msg: String)(implicit request: Request[_]): Unit = {
      if(logger.isDebugEnabled) logger.debug(s"requestId=[$getRequestId] $msg")
    }

    def debug(msg: String, e: Throwable)(implicit request: Request[_]): Unit = {
      if(logger.isDebugEnabled) logger.debug(s"requestId=[$getRequestId] $msg", e)
    }

    def trace(msg: String)(implicit request: Request[_]): Unit = {
      if(logger.isTraceEnabled) logger.trace(s"requestId=[$getRequestId] $msg")
    }

    def trace(msg: String, e: Throwable)(implicit request: Request[_]): Unit = {
      if(logger.isTraceEnabled) logger.trace(s"requestId=[$getRequestId] $msg", e)
    }

    def info(msg: String)(implicit request: Request[_]): Unit = {
      if(logger.isInfoEnabled) logger.info(s"requestId=[$getRequestId] $msg")
    }

    def info(msg: String, e: Throwable)(implicit request: Request[_]): Unit = {
      if(logger.isInfoEnabled) logger.info(s"requestId=[$getRequestId] $msg", e)
    }

    def warn(msg: String)(implicit request: Request[_]): Unit = {
      if(logger.isWarnEnabled) logger.warn(s"requestId=[$getRequestId] $msg")
    }

    def warn(msg: String, e: Throwable)(implicit request: Request[_]): Unit = {
      if(logger.isWarnEnabled) logger.warn(s"requestId=[$getRequestId] $msg", e)
    }

    def error(msg: String)(implicit request: Request[_]): Unit = {
      if(logger.isErrorEnabled) logger.error(s"requestId=[$getRequestId] $msg")
    }

    def error(msg: String, e: Throwable)(implicit request: Request[_]): Unit = {
      if(logger.isErrorEnabled) logger.error(s"requestId=[$getRequestId] $msg", e)
    }
  }
}
