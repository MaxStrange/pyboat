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
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.api.ndarray.INDArray

case class MoveOrHoldMLP() extends ModelArch {
  Nd4j.ENFORCE_NUMERICAL_STABILITY = true
  val batchSize = 1
  val fetcher = new MLPDataFetcher()

  def getConfiguration() : MultiLayerConfiguration = {
    val builder = new NeuralNetConfiguration.Builder
    builder.seed(123)
    builder.iterations(1)
    builder.regularization(true).l2(0.0005)
    builder.learningRate(0.01)
    builder.weightInit(WeightInit.XAVIER)
    builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    builder.updater(Updater.NESTEROVS).momentum(0.9)

    val listBuilder = builder.list

    val dense0 = new DenseLayer.Builder
    dense0.activation(Activation.SIGMOID)
    dense0.nIn(21 * 21 * fetcher.nChannels)
    dense0.nOut(2048)
    listBuilder.layer(0, dense0.build)

    val dense1 = new DenseLayer.Builder
    dense1.activation(Activation.SIGMOID)
    dense1.nOut(1024)
    listBuilder.layer(1, dense1.build)

    val dense2 = new DenseLayer.Builder
    dense2.activation(Activation.SIGMOID)
    dense2.nOut(512)
    listBuilder.layer(2, dense2.build)

    val dense3 = new DenseLayer.Builder
    dense3.activation(Activation.SIGMOID)
    dense3.nOut(256)
    listBuilder.layer(3, dense3.build)

    val output = new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
    output.nOut(21 * 21)
    output.activation(Activation.SIGMOID)
    listBuilder.layer(4, output.build)

    listBuilder.backprop(true)
    listBuilder.pretrain(false)
    val conf = listBuilder.build

    // TODO: make loss function ignore non-unit locations in the label
    return conf
  }

  def getDatasetIterator() : BaseDatasetIterator = {
    return new MLPDatasetIterator(batchSize, fetcher.totalExamples, fetcher)
  }
}

class MLPDataFetcher() extends BaseDataFetcher {
  val shuffledGameIds = util.Random.shuffle(Database.getAllGameIds())
  var curGame = new Game(shuffledGameIds(0))
  val nChannels = curGame.getNumChannelsHoldOrMove()
  numOutcomes = curGame.getNumOutcomesHoldOrMove()
  inputColumns = curGame.getNumInputColumnsHoldOrMove()
  totalExamples = Database.getTotalNumExamplesMoveOrHold()
  cursor = 0

  println("Initializing fetcher: curGame: " + curGame + "\n  nChannels: " + nChannels + "\n  numOutcomes: " + numOutcomes + "\n  inputColumns: " + inputColumns + "\n  totalExamples: " + totalExamples + "\n  cursor: " + cursor)

  /**
   * Create a dataset from a list of datasets. This is the dataset that will be currently fed into the model.
   */
  override def initializeCurrFromList(examples: java.util.List[DataSet]) = {
    require(!examples.isEmpty)
    val inputs = createInputMatrix(examples.size())
    val labels = createOutputMatrix(examples.size())
    for (i <- 0 until examples.size()) {
      val nextInput = examples.get(i).getFeatureMatrix()
      val nextLabel = examples.get(i).getLabels()
      inputs.putRow(i, examples.get(i).getFeatureMatrix())
      labels.putRow(i, examples.get(i).getLabels())
    }
    curr = new DataSet(inputs, labels)
  }

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
      lb += new DataSet(turnFlat, labelFlat)
    }
    val examples = lb.toList
    initializeCurrFromList(examples.asJava)
    cursor += numExamples
  }

  /**
   * Gets the next Game in the list and sets curGame to it. If the list
   * is done, this will set it to null.
   */
  def fetchNextGame() = {
    val ls = shuffledGameIds.dropWhile(i => i != curGame.id).drop(1)
    if (ls.length == 0)
      curGame = null
    else
      curGame = new Game(ls(0))
  }
}

class MLPDatasetIterator(batchSize: Int, numExamples: Int, fetcher: DataSetFetcher) extends BaseDatasetIterator(batchSize, numExamples, fetcher) {
  batch = batchSize
  println("Initializing iterator. BatchSize: " + batch + " Number of examples: " + numExamples)
}
