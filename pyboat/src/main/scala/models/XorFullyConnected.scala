import scala.collection.JavaConverters._
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.MultiLayerConfiguration
import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.distribution.UniformDistribution
import org.deeplearning4j.nn.conf.layers.DenseLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer
import org.deeplearning4j.nn.conf.layers.OutputLayer.Builder
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.dataset.api.iterator.BaseDatasetIterator
import org.nd4j.linalg.dataset.api.iterator.fetcher.BaseDataFetcher
import org.nd4j.linalg.dataset.api.iterator.fetcher.DataSetFetcher
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.factory.Nd4j
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.nd4j.linalg.api.ndarray.INDArray

case class XorFullyConnected() extends ModelArch {
  def getConfiguration() : MultiLayerConfiguration = {
    val builder = new NeuralNetConfiguration.Builder
    builder.iterations(10000)
    builder.learningRate(0.1)
    builder.seed(123)
    builder.useDropConnect(false)
    builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    builder.biasInit(0)
    builder.miniBatch(false)

    val listBuilder = builder.list

    val hiddenLayerBuilder = new DenseLayer.Builder
    hiddenLayerBuilder.nIn(2)
    hiddenLayerBuilder.nOut(4)
    hiddenLayerBuilder.activation(Activation.SIGMOID)
    hiddenLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
    hiddenLayerBuilder.dist(new UniformDistribution(0, 1))
    listBuilder.layer(0, hiddenLayerBuilder.build)

    val outputLayerBuilder: OutputLayer.Builder = new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
    outputLayerBuilder.nIn(4)
    outputLayerBuilder.nOut(2)
    outputLayerBuilder.activation(Activation.SOFTMAX)
    outputLayerBuilder.weightInit(WeightInit.DISTRIBUTION)
    outputLayerBuilder.dist(new UniformDistribution(0, 1))
    listBuilder.layer(1, outputLayerBuilder.build)

    listBuilder.pretrain(false)
    listBuilder.backprop(true)
    val conf = listBuilder.build

    return conf
  }

  def getDatasetIterator() : BaseDatasetIterator = {
    // create dataset object
    val batchSize = 4
    val fetcher = new XorDataFetcher()
    return new XorDatasetIterator(batchSize, fetcher.totalExamples, fetcher)
  }
}

class XorDataFetcher() extends BaseDataFetcher {
  numOutcomes = 2
  inputColumns = 2
  totalExamples = 4

  val input = Nd4j.zeros(totalExamples, inputColumns)
  val labels = Nd4j.zeros(totalExamples, numOutcomes)

  input.putScalar(Array[Int](0, 0), 0)
  input.putScalar(Array[Int](0, 1), 0)
  labels.putScalar(Array[Int](0, 0), 1)
  labels.putScalar(Array[Int](0, 1), 0)

  input.putScalar(Array[Int](1, 0), 1)
  input.putScalar(Array[Int](1, 1), 0)
  labels.putScalar(Array[Int](1, 0), 0)
  labels.putScalar(Array[Int](1, 1), 1)

  input.putScalar(Array[Int](2, 0), 0)
  input.putScalar(Array[Int](2, 1), 1)
  labels.putScalar(Array[Int](2, 0), 0)
  labels.putScalar(Array[Int](2, 1), 1)

  input.putScalar(Array[Int](3, 0), 1)
  input.putScalar(Array[Int](3, 1), 1)
  labels.putScalar(Array[Int](3, 0), 1)
  labels.putScalar(Array[Int](3, 1), 0)

  /**
   * Fill a buffer of examples to be drawn from sequentially.
   */
  override def fetch(numExamples: Int) = {
    val from = cursor
    val to = if (cursor + numExamples > totalExamples) totalExamples else cursor + numExamples
    // Get the whole dataset every time (since it's only four items)
    val examples = for (i <- from until to) yield new DataSet(input.getRow(i), labels.getRow(i))
    initializeCurrFromList(examples.asJava)
    cursor += numExamples
  }
}

class XorDatasetIterator(batchSize: Int, numExamples: Int, fetcher: DataSetFetcher) extends BaseDatasetIterator(batchSize, numExamples, fetcher) {
}
