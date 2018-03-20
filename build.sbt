
organization := "scala-lang.org.ua"

name := "site"

version := "0.1"

scalaVersion:="2.11.4"

//TODO: move to another markdown parser.
libraryDependencies += "com.tristanhunt" %% "knockoff" % "0.8.3"

libraryDependencies += "commons-io" % "commons-io" % "2.4"


lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

//libraryDependencies += "org.pegdown" % "pegdown" % "1.4.2"

// run embedded server
libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.2.6.v20141205"

lazy val serve = taskKey[Unit]("Run embedded web server with generated site")

serve := {
  (run in Compile).toTask(" server").value
}


// publish site to git
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "3.6.0.201412230720-r"

lazy val publishSite = taskKey[Unit]("Publish generated site to github")

publishSite := {
  (run in Compile).toTask(" publishSite").value
}

