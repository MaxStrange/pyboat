package pyboat

import pyboat.models.ModelArch
import pyboat.models.MoveOrHoldCNN
import pyboat.models.MoveOrHoldMLP
import pyboat.models.XorFullyConnected

import java.io.File
import java.nio.file.Files
import java.nio.file.FileSystems
import org.apache.spark.api.java.JavaSparkContext
import org.apache.spark.SparkConf
import org.apache.spark.api.java.JavaRDD
import org.apache.spark.sql.SparkSession
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
//import org.deeplearning4j.spark.api.RDDTrainingApproach
//import org.deeplearning4j.spark.api.TrainingMaster
//import org.deeplearning4j.spark.impl.multilayer.SparkDl4jMultiLayer
//import org.deeplearning4j.spark.parameterserver.training.SharedTrainingMaster
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator

object PyBoat {
  def main(args: Array[String]) {
    util.Random.setSeed(12345)

    // !! Change this value to change what model is being trained !! //
    val architecture: ModelArch = MoveOrHoldCNN()
    println("ARCHITECTURE: " + architecture)

    val networkConf: MultiLayerConfiguration = architecture match {
      case XorFullyConnected() => XorFullyConnected().getConfiguration()
      case MoveOrHoldCNN() => MoveOrHoldCNN().getConfiguration()
      case MoveOrHoldMLP() => MoveOrHoldMLP().getConfiguration()
    }

    val dsItr: BaseDatasetIterator = architecture match {
      case XorFullyConnected() => XorFullyConnected().getDatasetIterator()
      case MoveOrHoldCNN() => MoveOrHoldCNN().getDatasetIterator()
      case MoveOrHoldMLP() => MoveOrHoldMLP().getDatasetIterator()
    }

    println("Building network...")
    val net = new MultiLayerNetwork(networkConf)
    net.init()

    ///////////////////////////////////////////////////
    // Manually output a forward pass of the network //
    ///////////////////////////////////////////////////
    val manualOutput: INDArray = net.output(dsItr.next().getFeatureMatrix)
    dsItr.reset()
    println(manualOutput)

    val manualEval: Evaluation = new Evaluation(2)
    manualEval.eval(dsItr.next().getLabels, manualOutput)
    dsItr.reset()
    println(manualEval.stats)

    //// Feed forward
    //val dataNext = dsItr.next()
    //val activations = net.feedForward(dataNext.getFeatureMatrix)
    //println("ACTIVATIONS: " + activations)

    //// Get the gradients
    //net.setLabels(dataNext.getLabels)
    //val grad = net.gradient()
    //println("GRADIENT: " + grad)

    //val errSignal = net.scoreExamples(dataNext, true)
    //println("ERR SIGNAL: " + errSignal)
    ////val err = net.error(errSignal)

    //// Fit and check
    //net.fit(dataNext)
    //println("ACTIVATIONS AFTER FITTING: " + net.feedForward(dataNext.getFeatureMatrix))
    ///////////////////////////////////////////////////

    println("Initializing UI server...")
    val uiServer = UIServer.getInstance()
    val fpath = "savedstuff"
    Files.deleteIfExists(FileSystems.getDefault().getPath(fpath))
    val statsStorage = new FileStatsStorage(new File(fpath))
    uiServer.attach(statsStorage)

    net.setListeners(new StatsListener(statsStorage))

    ////// SPARK TRAINING ////////////
    //val sparkConf = new SparkConf().setAppName("DiploTrainer").setMaster("local")
    //val sc = new JavaSparkContext(conf)
    //val trainingData: JavaRDD[DataSet] = SparkMachine.getRDDs()//TODO: Make sure that each DataSet has exactly 4 examples in it

    ////Create the TrainingMaster instance
    //val examplesPerDataSetObject = 4
    //val trainingMaster = new ParameterAveragingTrainingMaster.Builder(examplesPerDataSetObject).build()

    ////Create the SparkDl4jMultiLayer instance
    //val sparkNetwork = new SparkDl4jMultiLayer(sc, conf, trainingMaster)

    ////Fit the network using the training data:
    //sparkNetwork.fit(trainingData)
    //////////////////////////////////

    ///// TRAINING ////////
    println("Training...")
    var i: Int = 0
    while (dsItr.hasNext() && i < 1000) {
      val ds = dsItr.next()
      net.fit(ds)
      i += 1
      if (i % 10 == 0) {
        val out = net.output(ds.getFeatureMatrix)
        var s = ""
        for (j <- out.shape)
          s += j + " "
        println("Shape: " + s)
        println("Value of [0, 0]: " + out.getDouble(0, 0))
        println("Value of [0, 25]: " + out.getDouble(0, 25))
        if (i % 100 == 0)
          println(out.getRow(10))
      }
    }
    dsItr.reset()
    //////////////////////

    ///// EVALUATION /////
    val output: INDArray = net.output(dsItr.next().getFeatureMatrix)
    dsItr.reset()
    println(output)

    val eval: Evaluation = new Evaluation(2)
    eval.eval(dsItr.next().getLabels, output)
    dsItr.reset()
    println(eval.stats)
    /////////////////////
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

