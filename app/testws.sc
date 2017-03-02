import org.joda.time.{DateTime, DateTimeZone}

DateTime.now(DateTimeZone.forID("Europe/Kiev"))
DateTime.now(DateTimeZone.forID("UTC"))

List(1).zipWithIndex