import java.io.File
import java.nio.file.Files
import java.nio.file.FileSystems
import org.apache.spark.sql.SparkSession
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator

abstract class ModelArch

object PyBoat {
  def main(args: Array[String]) {
    util.Random.setSeed(12345)

    // !! Change this value to change what model is being trained !! //
    val architecture: ModelArch = XorFullyConnected()

    val networkConf: MultiLayerConfiguration = architecture match {
      case XorFullyConnected() => XorFullyConnected().getConfiguration()
      case MoveOrHoldCNN() => MoveOrHoldCNN().getConfiguration()
    }

    val dsItr: BaseDatasetIterator = architecture match {
      case XorFullyConnected() => XorFullyConnected().getDatasetIterator()
      case MoveOrHoldCNN() => MoveOrHoldCNN().getDatasetIterator()
    }

    val net = new MultiLayerNetwork(networkConf)
    net.init()

    val uiServer = UIServer.getInstance()
    val fpath = "savedstuff"
    Files.deleteIfExists(FileSystems.getDefault().getPath(fpath))
    val statsStorage = new FileStatsStorage(new File(fpath))
    uiServer.attach(statsStorage)

    net.setListeners(new StatsListener(statsStorage))

    var i = 0
    while (dsItr.hasNext()) {
      val ds = dsItr.next()
      net.fit(ds)
    }
    dsItr.reset()

    val output: INDArray = net.output(dsItr.next().getFeatureMatrix)
    dsItr.reset()
    println(output)

    val eval: Evaluation = new Evaluation(2)
    eval.eval(dsItr.next().getLabels, output)
    dsItr.reset()
    println(eval.stats)
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

