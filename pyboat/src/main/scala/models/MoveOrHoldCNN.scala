package pyboat.models

import pyboat.Database
import pyboat.game.Game

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.ConvolutionMode
import org.deeplearning4j.nn.conf.GradientNormalization
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
import org.nd4j.linalg.lossfunctions.impl.LossBinaryXENT
import org.nd4j.linalg.activations.IActivation
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.lossfunctions.ILossFunction
import org.nd4j.linalg.ops.transforms.Transforms
import org.nd4j.linalg.primitives.Pair

case class MoveOrHoldCNN() extends ModelArch {
  Nd4j.ENFORCE_NUMERICAL_STABILITY = true
  val batchSize = 8
  val fetcher = new CNNDataFetcher()

  def getConfiguration() : MultiLayerConfiguration = {
    val builder = new NeuralNetConfiguration.Builder
    builder.seed(123)
    builder.iterations(1)
    builder.learningRate(0.01)
    builder.weightInit(WeightInit.XAVIER)
    builder.optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
    builder.updater(Updater.NESTEROVS).momentum(0.9)
    builder.gradientNormalization(GradientNormalization.RenormalizeL2PerLayer)
    builder.l1(1e-4)
    builder.regularization(true)
    builder.l2(5 * 1e-4)

    val listBuilder = builder.list
    var lindex = 0

    val cnnBuilder0 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder0.nIn(fetcher.nChannels)
    cnnBuilder0.stride(1, 1)
    cnnBuilder0.padding(1, 1)
    cnnBuilder0.convolutionMode(ConvolutionMode.Same)
    cnnBuilder0.nOut(64) //number of filters in this layer
    cnnBuilder0.activation(Activation.RELU)
    cnnBuilder0.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder0.build)
    lindex += 1

    val subsamp0 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp0.kernelSize(2, 2)
    subsamp0.stride(2, 2)
    subsamp0.convolutionMode(ConvolutionMode.Same)
    listBuilder.layer(lindex, subsamp0.build)
    lindex += 1

    val cnnBuilder0_5 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder0_5.stride(1, 1)
    cnnBuilder0_5.padding(1, 1)
    cnnBuilder0_5.convolutionMode(ConvolutionMode.Same)
    cnnBuilder0_5.nOut(128)
    cnnBuilder0_5.activation(Activation.RELU)
    cnnBuilder0_5.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder0_5.build)
    lindex += 1

    val subsamp0_5 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp0_5.kernelSize(2, 2)
    subsamp0_5.stride(2, 2)
    subsamp0_5.convolutionMode(ConvolutionMode.Same)
    listBuilder.layer(lindex, subsamp0_5.build)
    lindex += 1

    val cnnBuilder1 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder1.stride(1, 1)
    cnnBuilder1.padding(1, 1)
    cnnBuilder1.convolutionMode(ConvolutionMode.Same)
    cnnBuilder1.nOut(256)
    cnnBuilder1.activation(Activation.RELU)
    cnnBuilder1.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder1.build)
    lindex += 1

    val cnnBuilder1_5 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder1_5.stride(1, 1)
    cnnBuilder1_5.padding(1, 1)
    cnnBuilder1_5.convolutionMode(ConvolutionMode.Same)
    cnnBuilder1_5.nOut(256)
    cnnBuilder1_5.activation(Activation.RELU)
    cnnBuilder1_5.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder1_5.build)
    lindex += 1

    val subsamp1 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp1.kernelSize(2, 2)
    subsamp1.stride(2, 2)
    subsamp1.convolutionMode(ConvolutionMode.Same)
    listBuilder.layer(lindex, subsamp1.build)
    lindex += 1

    val cnnBuilder2 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder2.stride(1, 1)
    cnnBuilder2.padding(1, 1)
    cnnBuilder2.convolutionMode(ConvolutionMode.Same)
    cnnBuilder2.nOut(512)
    cnnBuilder2.activation(Activation.RELU)
    cnnBuilder2.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder2.build)
    lindex += 1

    val cnnBuilder3 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder3.stride(1, 1)
    cnnBuilder3.padding(1, 1)
    cnnBuilder3.convolutionMode(ConvolutionMode.Same)
    cnnBuilder3.nOut(512)
    cnnBuilder3.activation(Activation.RELU)
    cnnBuilder3.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder3.build)
    lindex += 1

    val subsamp2 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp2.kernelSize(2, 2)
    subsamp2.stride(2, 2)
    subsamp2.convolutionMode(ConvolutionMode.Same)
    listBuilder.layer(lindex, subsamp2.build)
    lindex += 1

    val cnnBuilder4 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder4.stride(1, 1)
    cnnBuilder4.padding(1, 1)
    cnnBuilder4.convolutionMode(ConvolutionMode.Same)
    cnnBuilder4.nOut(512)
    cnnBuilder4.activation(Activation.RELU)
    cnnBuilder4.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder4.build)
    lindex += 1

    val cnnBuilder5 = new ConvolutionLayer.Builder(3, 3)
    cnnBuilder5.stride(1, 1)
    cnnBuilder5.padding(1, 1)
    cnnBuilder5.convolutionMode(ConvolutionMode.Same)
    cnnBuilder5.nOut(512)
    cnnBuilder5.activation(Activation.RELU)
    cnnBuilder5.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, cnnBuilder5.build)
    lindex += 1

    val subsamp3 = new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
    subsamp3.kernelSize(2, 2)
    subsamp3.stride(2, 2)
    subsamp3.convolutionMode(ConvolutionMode.Same)
    listBuilder.layer(lindex, subsamp3.build)
    lindex += 1

    val dense = new DenseLayer.Builder
    dense.activation(Activation.RELU)
    dense.nOut(4096)
    dense.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, dense.build)
    lindex += 1

    val dense1 = new DenseLayer.Builder
    dense1.activation(Activation.LEAKYRELU)
    dense1.nOut(4096)
    dense1.biasInit(0.01).biasLearningRate(0.02)
    listBuilder.layer(lindex, dense1.build)
    lindex += 1

    val output = new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
    output.nOut(21 * 21)
    output.activation(Activation.SIGMOID)
    listBuilder.layer(lindex, output.build)
    lindex += 1

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
    val masks = createOutputMatrix(examples.size())
    for (i <- 0 until examples.size()) {
      val nextInput = examples.get(i).getFeatureMatrix()
      val nextLabel = examples.get(i).getLabels()
      inputs.putRow(i, examples.get(i).getFeatureMatrix())
      labels.putRow(i, examples.get(i).getLabels())
      masks.putRow(i, examples.get(i).getLabelsMaskArray())
    }
    curr = new DataSet(inputs, labels, null, masks)
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
      val labelFlat = Nd4j.toFlattened(label)
      val labelMask = labelFlat.dup()
      val iter = new NdIndexIterator(labelMask.shape()(1))
      while (iter.hasNext()) {
        val nextIndex = iter.next()(0)
        val nextVal = labelFlat.getDouble(nextIndex)
        val maskVal = if (nextVal + 0.001 > 0.5 && nextVal - 0.001 < 0.5) 0 else 1
        labelMask.putScalar(nextIndex, maskVal)
      }
      lb += new DataSet(turn, labelFlat, null, labelMask)
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
    ////// TODO: FIXME: DEBUG: Use only the current game again and again /////
    val ls = shuffledGameIds.dropWhile(i => i != curGame.id)
    ///////////////////////////////
    //val ls = shuffledGameIds.dropWhile(i => i != curGame.id).drop(1)
    if (ls.length == 0)
      curGame = null
    else
      curGame = new Game(ls(0))
    println("GAME ID: " + curGame.id)
  }
}

class CNNDatasetIterator(batchSize: Int, numExamples: Int, fetcher: DataSetFetcher) extends BaseDatasetIterator(batchSize, numExamples, fetcher) {
  batch = batchSize
  println("Initializing iterator. BatchSize: " + batch + " Number of examples: " + numExamples)
}
