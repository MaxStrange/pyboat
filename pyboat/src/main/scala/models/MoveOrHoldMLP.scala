package pyboat.models

import pyboat.Database
import pyboat.game.Game

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.Updater
import org.deeplearning4j.nn.conf.distribution.UniformDistribution
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer.Builder
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.api.iter.NdIndexIterator
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions

case class MoveOrHoldMLP() extends ModelArch {
  Nd4j.ENFORCE_NUMERICAL_STABILITY = true
  val batchSize = 16
  val fetcher = new MLPDataFetcher()

  def getConfiguration() : MultiLayerConfiguration = {
    val builder = new NeuralNetConfiguration.Builder
    builder.seed(123)
    builder.iterations(1)
    //builder.regularization(true).l2(0.0005)
    builder.learningRate(0.01)
    builder.weightInit(WeightInit.XAVIER)
    builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    builder.updater(Updater.NESTEROVS).momentum(0.9)

    val listBuilder = builder.list

    val dense0 = new DenseLayer.Builder
    dense0.activation(Activation.SIGMOID)
    dense0.nIn(21 * 21 * fetcher.nChannels)
    dense0.nOut(4096)
    listBuilder.layer(0, dense0.build)

    val dense1 = new DenseLayer.Builder
    dense1.activation(Activation.SIGMOID)
    dense1.nOut(2048)
    listBuilder.layer(1, dense1.build)

    val dense2 = new DenseLayer.Builder
    dense2.activation(Activation.SIGMOID)
    dense2.nOut(1024)
    listBuilder.layer(2, dense2.build)

    val dense3 = new DenseLayer.Builder
    dense3.activation(Activation.SIGMOID)
    dense3.nOut(512)
    listBuilder.layer(3, dense3.build)

    val output = new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
    output.nOut(21 * 21)
    output.activation(Activation.SIGMOID)
    listBuilder.layer(4, output.build)

    listBuilder.backprop(true)
    listBuilder.pretrain(false)
    val conf = listBuilder.build

    return conf
  }

  def getDatasetIterator() : BaseDatasetIterator = {
    return new MLPDatasetIterator(batchSize, fetcher.totalExamples, fetcher)
  }
}

class MLPDataFetcher() extends CNNDataFetcher {
  /**
   * Called to fetch a new batch of `numExamples`.
   */
  override def fetch(numExamples: Int) = {
    val from = cursor
    val to = if (cursor + numExamples > totalExamples) totalExamples else cursor + numExamples
    // cursor is iterating over each turn in the dataset, so ask the current game if it has any more turns
    // if it doesn't, expand the next game and start getting turns from it, etc.
    var lb = new ListBuffer[DataSet]()
    for (i <- from until to ) {
      while (curGame != null && !curGame.hasNextHoldOrMoveMatrix()) {
        fetchNextGame()
      }
      //If this happens, there is something wrong with totalExamples (because we ran out of games before we were supposed to)
      require(curGame != null)
      val (turn, label) = curGame.getNextHoldOrMoveMatrix()
      val turnFlat = Nd4j.toFlattened(turn)
      val labelFlat = Nd4j.toFlattened(label)
      val labelMask = labelFlat.dup()
      val iter = new NdIndexIterator(labelMask.shape()(1))
      while (iter.hasNext()) {
        val nextIndex = iter.next()(0)
        val nextVal = labelFlat.getDouble(nextIndex)
        val maskVal = if (nextVal + 0.001 > 0.5 && nextVal - 0.001 < 0.5) 0 else 1
        labelMask.putScalar(nextIndex, maskVal)
      }
      lb += new DataSet(turnFlat, labelFlat, null, labelMask)
    }
    val examples = lb.toList
    initializeCurrFromList(examples.asJava)
    cursor += numExamples
  }
}

class MLPDatasetIterator(batchSize: Int, numExamples: Int, fetcher: DataSetFetcher) extends CNNDatasetIterator(batchSize, numExamples, fetcher) {
  batch = batchSize
  println("Initializing iterator. BatchSize: " + batch + " Number of examples: " + numExamples)
}
