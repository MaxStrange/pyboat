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
import org.deeplearning4j.ui.api.UIServer
import org.deeplearning4j.ui.stats.StatsListener
import org.deeplearning4j.ui.storage.FileStatsStorage
import org.deeplearning4j.util.ModelSerializer;
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

    val modelTypeName: String = architecture match {
      case XorFullyConnected() => "XORFullyConnected"
      case MoveOrHoldCNN() => "MoveOrHoldCNN"
      case MoveOrHoldMLP() => "MoveOrHoldMLP"
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
    ///////////////////////////////////////////////////

    println("Initializing UI server...")
    val uiServer = UIServer.getInstance()
    val fpath = "savedstuff"
    Files.deleteIfExists(FileSystems.getDefault().getPath(fpath))
    val statsStorage = new FileStatsStorage(new File(fpath))
    uiServer.attach(statsStorage)

    net.setListeners(new StatsListener(statsStorage))

    ///// TRAINING ////////
    println("Training...")
    var i: Int = 0
    while (dsItr.hasNext()) {
      val ds = dsItr.next()
      println("Fitting network to next batch...")
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
        if (i % 100 == 0) {
          println("=================== Mask  ===================")
          println(ds.getLabelsMaskArray().getRow(0))
          println("=================== Label ===================")
          println(ds.getLabels().getRow(0))
          println("=================== Output ===================")
          println(out.getRow(0))
        }
        if (i % 1000 == 0) {
          println("Saving model...")
          Files.deleteIfExists(FileSystems.getDefault().getPath(modelTypeName + ".zip"))
          val locationToSave = new File(modelTypeName + ".zip")
          val saveUpdater = true
          ModelSerializer.writeModel(net, locationToSave, saveUpdater);
        }
      }
      println("Readying next batch...")
    }
    dsItr.reset()
    //////////////////////

    ///// EVALUATION /////
    val output: INDArray = net.output(dsItr.next().getFeatureMatrix)
    dsItr.reset()
    println(output)
    /////////////////////
  }
}

