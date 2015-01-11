
organization := "scala-lang.org.ua"

name := "site"

version := "0.1"

scalaVersion:="2.11.4"

libraryDependencies += "com.tristanhunt" %% "knockoff" % "0.8.3"

//libraryDependencies += "org.pegdown" % "pegdown" % "1.4.2"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.2.6.v20141205"

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

lazy val serve = taskKey[Unit]("Run embedded web server with generated site")

serve := {
  (run in Compile).toTask(" server").value
}
