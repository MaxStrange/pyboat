import scala.collection.JavaConverters._
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

case class MoveOrHoldCNN() extends ModelArch {
  val batchSize = 16
  val fetcher = new CNNDataFetcher()

  def getConfiguration() : MultiLayerConfiguration = {
    val builder = new NeuralNetConfiguration.Builder
    builder.seed(123)
    builder.iterations(1)
    builder.regularization(true).l2(0.0005)
    builder.learningRate(0.01)
    builder.lrPolicyDecayRate(0.001).lrPolicyPower(0.75)
    builder.weightInit(WeightInit.XAVIER)
    builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    builder.updater(Updater.NESTEROVS).momentum(0.9)

    val listBuilder = builder.list

    val cnnBuilder0 = new ConvolutionLayer.Builder(5, 5)
    cnnBuilder0.nIn(fetcher.nChannels)
    cnnBuilder0.stride(1, 1)
    cnnBuilder0.nOut(20) //number of filters in this layer
    cnnBuilder0.activation(Activation.RELU)
    listBuilder.layer(0, cnnBuilder0.build)

    val subsamp0 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp0.kernelSize(2, 2)
    subsamp0.stride(2, 2)
    listBuilder.layer(1, subsamp0.build)

    val cnnBuilder1 = new ConvolutionLayer.Builder(5, 5)
    cnnBuilder1.stride(1, 1)
    cnnBuilder1.nOut(50)
    cnnBuilder1.activation(Activation.RELU)
    listBuilder.layer(2, cnnBuilder1.build)

    val subsamp1 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp1.kernelSize(2, 2)
    subsamp1.stride(2, 2)
    listBuilder.layer(3, subsamp1.build)

    val dense = new DenseLayer.Builder
    dense.activation(Activation.RELU)
    dense.nOut(500)
    listBuilder.layer(4, dense.build)

    val output = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
    output.nOut(21 * 21)
    output.activation(Activation.SIGMOID)
    listBuilder.layer(5, output.build)

    listBuilder.setInputType(InputType.convolutionalFlat(21, 21, fetcher.nChannels))
    listBuilder.backprop(true)
    listBuilder.pretrain(false)
    val conf = listBuilder.build

    return conf
  }

  def getDatasetIterator() : BaseDatasetIterator = {
    return new CNNDatasetIterator(batchSize, fetcher.totalExamples, fetcher)
  }
}

class CNNDataFetcher() extends BaseDataFetcher {
  val shuffledGameIds = util.Random.shuffle(Database.getAllGameIds())
  var curGame = Game(shuffledGameIds(0))
  val nChannels = 7//unit_loc, unit_type, unit_own, loc_type, loc_own, season, scs//TODO: replace with a call to some game method that returns this
  cursor = 0
  numOutcomes = 21 * 21
  inputColumns = 21 * 21
  totalExamples = 100//TODO: Get this from the Database

  override def initializeCurrFromList(examples: List[DataSet]) = {
    if (examples.isEmpty())
      log.warn("Warning: empty dataset from the fetcher")

    val inputs = createInputMatrix(examples.size())
    val labels = createOutputMatrix(examples.size())
    for (i <- examples.size) {
      inputs.putRow(i, examples(i).getFeatureMatrix())
      inputs.putRow(i, examples(i).getLabels())
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
    val examples = generator[DataSet] {produce =>
      for (i <- suspendable(from until to)) {
        while (curGame != null && !curGame.hasNextHoldOrMoveMatrix()) {
          fetchNextGame()
        }
        //If this happens, there is something wrong with totalExamples (because we ran out of games before we were supposed to)
        require(curGame != null)
        val (turn, label) = curGame.getNextHoldOrMoveMatrix()
        produce(new DataSet(turn, label))
      }
    }
    initializeCurrFromList(examples.asJava)
    cursor += numExamples
  }

  /**
   * Gets the next Game in the list and sets curGame to it. If the list
   * is done, this will set it to null.
   */
  def fetchNextGame() = {
    val ls = shuffledGameIds.dropWhile(i => i != curGame.id)
    if (ls.length == 0)
      curGame = null
    else
      curGame = Game(ls(0))
  }
}

class CNNDatasetIterator(batchSize: Int, numExamples: Int, fetcher: DataSetFetcher) extends BaseDatasetIterator(batchSize, numExamples, fetcher) {
  batch = batchSize
  numExamples = if (numExamples < 0) fetcher.totalExamples else numExamples
  fetcher = fetcher
}
