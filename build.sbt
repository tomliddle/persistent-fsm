name := "persistentfsm"
 
version := "1.0"
      
lazy val `persistentfsm` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers +=  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
      
scalaVersion := "2.11.7"

val AkkaV = "2.4.14" // TODO sort

libraryDependencies ++= Seq(
  //guice,
  "com.typesafe.akka" %% "akka-cluster" % AkkaV,
  "com.typesafe.akka" %% "akka-cluster-tools" % AkkaV,
  "com.typesafe.akka" %% "akka-persistence" % AkkaV,
  "mysql" % "mysql-connector-java" % "6.0.6",
  "com.github.dnvriend" %% "akka-persistence-jdbc" % "2.4.17.1",
  // Test
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % Test,
  // https://mvnrepository.com/artifact/com.typesafe.akka/akka-testkit_2.11
  "com.typesafe.akka" %% "akka-testkit" % AkkaV % Test

)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )


scalacOptions += "-language:postfixOps"