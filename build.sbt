name := "magic-mirror-api"

version := "1.0"

lazy val `magic-mirror-api` = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

classpathTypes += "maven-plugin"

PlayKeys.externalizeResources := false

libraryDependencies ++= Seq(
  javaJdbc ,
  cache ,
  javaWs,
  filters,
  "org.bytedeco" % "javacv" % "1.1",
  "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1",
  "org.bytedeco.javacpp-presets" % "opencv" % "3.0.0-1.1" classifier "macosx-x86_64"
)

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )  

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"  