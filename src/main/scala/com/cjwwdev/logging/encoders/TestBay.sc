val logMessage = "GET request to /administration/service-health returned a 200 and took 40862ms"

def processLog(message: String): Option[(String, Int, String)]  = {
  message match {
    case x if x.matches("""^(HEAD|GET|POST|PUT|PATCH|DELETE) request to (.*) returned a \d{3} and took \d+ms$""") =>
      val digits = ("""\d+""".r findAllIn x).toList
      val method = ("""(HEAD|GET|POST|PUT|PATCH|DELETE)""".r findAllIn x).toList.head
      Some((method, digits.head.toInt, s"${digits.last}ms"))
    case _ => None
  }
}

val (method, status, responseTime) = processLog(logMessage).get

println(s"Method: $method")
println(s"Status: $status")
println(s"Time: $responseTime")