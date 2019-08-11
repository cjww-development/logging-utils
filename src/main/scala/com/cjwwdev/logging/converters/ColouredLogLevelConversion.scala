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

package com.cjwwdev.logging.converters

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.pattern.ClassicConverter
import ch.qos.logback.classic.spi.ILoggingEvent
import play.utils.Colors

class ColouredLogLevelConversion extends ClassicConverter {
  override def convert(event: ILoggingEvent): String = {
    event.getLevel match {
      case level@Level.TRACE => s"[${Colors.blue(level.levelStr)}]"
      case level@Level.DEBUG => s"[${Colors.cyan(level.levelStr)}]"
      case level@Level.INFO  => s"[${Colors.white(level.levelStr)}]"
      case level@Level.WARN  => s"[${Colors.yellow(level.levelStr)}]"
      case level@Level.ERROR => s"[${Colors.red(level.levelStr)}]"
    }
  }
}
