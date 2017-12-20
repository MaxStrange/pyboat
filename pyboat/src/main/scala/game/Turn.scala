import scala.collection.mutable.Map

abstract class PhaseType
case class WinterPhase() extends PhaseType {}
case class OrdersPhase() extends PhaseType {}
case class BuildPhase() extends PhaseType {}
case class RetreatPhase() extends PhaseType {}

abstract class SeasonType
case class Winter() extends SeasonType {}
case class Spring() extends SeasonType {}
case class Fall() extends SeasonType {}

/**
 * Turn represents a single Turn's state in the game. It wraps a BoardState and provides additional metadata.
 * It also provides a means by which to derive new turns using the database.
 */
class Turn(val gameId: Int, val turnNum: Int, val phase: PhaseType, val year: Int, val season: SeasonType) {
  var board = createStartingBoard()

  override def toString() : String = {
    return "Turn " + turnNum + ": " + season + " " + phase + " " + year + " gID: " + gameId
  }

  /**
   * Creates a new BoardState and returns it. The BoardState created is representative of the initial
   * state of a classic Diplomacy game.
   */
  def createStartingBoard() : BoardState = {
    var austria = Database.getStartingUnits(gameId, Austria())
    var england = Database.getStartingUnits(gameId, England())
    var france = Database.getStartingUnits(gameId, France())
    var germany = Database.getStartingUnits(gameId, Germany())
    var italy = Database.getStartingUnits(gameId, Italy())
    var russia = Database.getStartingUnits(gameId, Russia())
    var turkey = Database.getStartingUnits(gameId, Turkey())

    var units = List(austria, england, france, germany, italy, russia, turkey).flatten

    //for each unit in units, add their location to the ownership matrix
    var ownershipMatrix = Map[String, CountryType]()
    for (u <- units) {
      ownershipMatrix += (u.location -> u.country)
    }

    return new BoardState(units, ownershipMatrix.toMap)
  }

  /**
   * Derives a new Turn object from this one, by checking all the orders for this turn in the database.
   * Does not change any state in this Turn.
   */
  def deriveNext() : Turn = {
    if (!Database.turnExists(gameId, turnNum))
      throw new NullPointerException("No turn with gameId: " + gameId + " and turnNum: " + turnNum)

    val orders = Database.getOrdersForTurn(gameId, turnNum)
    var newTurn = Database.getTurn(gameId, turnNum + 1)
    newTurn.copy(this)
    for (o <- orders) {
      newTurn.applyOrder(o)
    }
    return newTurn
  }

  def copy(other : Turn) = {
    // TODO: copy board state
  }

  def applyOrder(order : Order) = {
    // TODO: change board state according to the order if it succeeded
  }
}
