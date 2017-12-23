scalaVersion := "2.11.8"
name := "pyboat"
version := "0.0.1"

classpathTypes += "maven-plugin"

val nd4jVersion = "0.9.1"
libraryDependencies += "org.typelevel" %% "cats" % "0.9.0"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "2.2.1"
libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "2.2.1"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.45"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-core" % "0.9.1"
libraryDependencies += "org.deeplearning4j" % "deeplearning4j-ui_2.11" % "0.9.1"
libraryDependencies += "org.nd4j" % "nd4j-native-platform" % nd4jVersion
