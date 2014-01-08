name := "yolo"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  //jdbc,
  //anorm,
  //cache
  "org.mongodb" %% "casbah" % "2.6.3",
  "com.github.t3hnar" % "scala-bcrypt_2.10" % "2.3"
)     

play.Project.playScalaSettings

scalacOptions += "-feature"
