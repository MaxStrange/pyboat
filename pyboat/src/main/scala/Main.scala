import org.apache.spark.sql.SparkSession

object PyBoat {
  def main(args: Array[String]) {
//    val g = new Game(124311)
//    println(g.historyString())
    util.Random.setSeed(12345)

    val allGameIds = Database.getAllGameIds()
    val shuffledGameIds = util.Random.shuffle(allGameIds)
    for (i <- 0 until 10) {
      println("GAME ID: " + allGameIds(i))
      val g = new Game(allGameIds(i))
      println(g.historyString())
    }
  }
}

//object SimpleApp {
//  def main(args: Array[String]) {
//    val logFile = "YOUR_SPARK_HOME/README.md" // Should be some file on your system
//    val spark = SparkSession.builder.appName("Simple Application").getOrCreate()
//    val logData = spark.read.textFile(logFile).cache()
//    val numAs = logData.filter(line => line.contains("a")).count()
//    val numBs = logData.filter(line => line.contains("b")).count()
//    println(s"Lines with a: $numAs, Lines with b: $numBs")
//    spark.stop()
//  }
//}

