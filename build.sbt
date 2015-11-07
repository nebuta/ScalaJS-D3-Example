// Turn this project into a Scala.js project by importing these settings
enablePlugins(ScalaJSPlugin)

name := "Example"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.1"

persistLauncher := true

persistLauncher in Test := false

libraryDependencies ++= Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % "0.5.1" % "test",
    "com.lihaoyi" %%% "scalatags" % "0.5.3",
    "com.lihaoyi" %%% "scalarx" % "0.2.8"
)

// To resolve the no longer supported Jasmine framework
resolvers += Resolver.url(
    "bintray-scala-js-releases",
    url("http://dl.bintray.com/content/scala-js/scala-js-releases"))(
    Resolver.ivyStylePatterns)