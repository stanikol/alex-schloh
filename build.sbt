lazy val sharedJs = Shared.sharedJs

lazy val client = Client.client

lazy val sharedJvm = Shared.sharedJvm

lazy val server = Server.server

lazy val metrics = Utilities.metrics

lazy val benchmarking = Utilities.benchmarking

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)

enablePlugins(JavaAppPackaging)