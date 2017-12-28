package pyboat.game

import pyboat.Database

import scala.collection.mutable.ListBuffer
import org.nd4j.linalg.api.ndarray.INDArray

/**
 * Game represents an entire game from the PlayDiplomacy database.
 * The default constructor takes a single Int (a game ID) and finds it
 * in the database, then populates itself from what it finds.
 *
 */
class Game(val gameId: Int) {
  val (id, numTurns, numPlayers) = Database.getGame(gameId)
  require(id == gameId)

  var lb = new ListBuffer[Turn]()
  var turn = Database.getTurn(gameId, 0)
  lb += turn
  for (i <- 1 until numTurns - 1) {
    turn = turn.deriveNext()
    lb += turn
  }
  val turns = lb.toList
  var curTurn = turns(0)

  def testMasks() : List[String] = {
    return curTurn.testBoardStateLocationMasks()
  }

  def getNumChannelsHoldOrMove() : Int = {
    return turns(0).getHoldOrMoveMatrix().shape()(0)
  }

  def getNumOutcomesHoldOrMove() : Int = {
    return turns(1).getOrderMaskAsHoldsOrMoves().length()
  }

  def getNumInputColumnsHoldOrMove() : Int = {
    return turns(0).getHoldOrMoveMatrix().length()
  }

  def hasNextHoldOrMoveMatrix() : Boolean = {
    return curTurn.turnNum < turns.length - 1
  }

  def getNextHoldOrMoveMatrix() : (INDArray, INDArray) = {
    require(hasNextHoldOrMoveMatrix())
    val mat = curTurn.getHoldOrMoveMatrix()

    // Move curTurn to the next Spring/Fall Orders phase
    val allTurnsAfterThisOne = turns.dropWhile(x => x.turnNum != curTurn.turnNum).drop(1)
    curTurn = allTurnsAfterThisOne(0)

    val label = curTurn.getOrderMaskAsHoldsOrMoves()
    return (mat, label)
  }

  /**
   * Returns a String representation of the game that is useful for debugging.
   * This representation contains every order on every turn and what the game looks like
   * at the end of each turn.
   */
  def historyString() : String = {
    var sb = StringBuilder.newBuilder
    sb.append("--------------------------------------------------------------------\n")
    sb.append(" Game " + id + " numTurns: " + numTurns + " numPlayers: " + numPlayers + "\n")
    sb.append("--------------------------------------------------------------------\n")
    for (t <- turns) {
      sb.append(t.historyString())
      sb.append("\n")
    }
    return sb.toString()
  }
}
