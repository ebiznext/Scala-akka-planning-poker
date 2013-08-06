import sbt._
import Keys._
import play.Project._
import com.github.play2war.plugin._

object ApplicationBuild extends Build {

  val appName = "scrum-deck-core"

  val appVersion = "1.0-SNAPSHOT"

  resolvers += "repo.codahale.com" at "http://repo.codahale.com"

  resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  
  val appDependencies = Seq(
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "org.scala-lang" % "scala-compiler" % "2.10.0",
    "com.typesafe.akka"  %% "akka-actor" % "2.1.0",
    "junit" % "junit" % "4.10" % "test",
    "javax.mail" % "mail" % "1.4",
    "commons-codec" % "commons-codec" % "1.8"
    )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(Play2WarPlugin.play2WarSettings: _*).settings(      
      routesImport += "models.QueryBinders._",
      scalaVersion := "2.10.0",
    //resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"
	resolvers += "Local Maven Repository" at Path.userHome.asFile.toURI.toURL+".m2/repository",
    Play2WarKeys.servletVersion := "3.0"
  )
}
