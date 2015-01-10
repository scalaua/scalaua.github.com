
organization := "scala-lang.org.ua"

name := "site"

version := "0.1"

scalaVersion:="2.11.4"

libraryDependencies += "com.tristanhunt" %% "knockoff" % "0.8.3"

//libraryDependencies += "org.pegdown" % "pegdown" % "1.4.2"


lazy val root = (project in file(".")).enablePlugins(SbtTwirl)
