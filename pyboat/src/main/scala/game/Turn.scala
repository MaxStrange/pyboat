abstract class PhaseType
case class WinterPhase() extends PhaseType {}
case class Orders() extends PhaseType {}
case class Build() extends PhaseType {}
case class Retreat() extends PhaseType {}

abstract class SeasonType
case class Winter() extends SeasonType {}
case class Spring() extends SeasonType {}
case class Fall() extends SeasonType {}

/**
 * Turn represents a single Turn's state in the game. It wraps a BoardState and provides additional metadata.
 * It also provides a means by which to derive new turns using the database.
 */
class Turn(val gameId: Int, val turnNum: Int, val phase: PhaseType, val year: Int, val season: SeasonType) {
  override def toString() : String = {
    return "Turn " + turnNum + ": " + season + " " + phase + " " + year + " gID: " + gameId
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
