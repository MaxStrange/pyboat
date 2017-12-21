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
  var board: BoardState = null
  if (turnNum == 0) {
    board = createStartingBoard() // this constructs a new board through the database - avoid if possible
  }
  val orders: List[Order] = Database.getOrdersForTurn(gameId, turnNum)

  override def toString() : String = {
    return "Turn " + turnNum + ": " + season + " " + phase + " " + year + " gID: " + gameId
  }

  /**
   * Returns a String representation of the turn that is useful for debugging.
   * This representation contains every unit's location and every player's list of territories.
   * It also lists the orders that were input to derive this turn.
   */
  def historyString() : String = {
    var sb = StringBuilder.newBuilder
    sb.append("  " + this.toString() + "\n")
    sb.append("    AFTER APPLYING THE FOLLOWING ORDERS:\n")
    for (o <- orders) {
      sb.append("    " + o)
      sb.append("\n")
    }
    sb.append("    THE BOARD STATE IS LIKE THIS:\n")
    sb.append(board.historyString())
    sb.append("\n")
    return sb.toString()
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
   * Derives a new Turn object from this one, by checking all the orders for the next turn in the database.
   * Does not change any state in this Turn.
   *
   * Remember that orders associated with a turn are the orders that were input to arrive
   * at the given turn. Hence, turn 0 (Winter 1900) has no orders (though it does have units).
   */
  def deriveNext() : Turn = {
    if (!Database.turnExists(gameId, turnNum))
      throw new NullPointerException("No turn with gameId: " + gameId + " and turnNum: " + turnNum)

    var newTurn = Database.getTurn(gameId, turnNum + 1)
    newTurn.board = new BoardState(board.units, board.ownershipMatrix)
    val livingUnitIds = collection.mutable.Set[Int]()
    for (o <- newTurn.orders) {
      newTurn.applyOrder(o)
      livingUnitIds += o.unitId
    }
    board = new BoardState(board.units.filter(u => livingUnitIds.contains(u.unitId)), board.ownershipMatrix)
    return newTurn
  }

  def applyOrder(order : Order) = {
    if (order.success) {
      order.orderType match {
        case Move() | Retreat() => applyMoveOrRetreatOrder(order)
        case Hold() | Convoy() | Support() => applyHoldConvoyOrSupportOrder(order)
        case Build() => applyBuildOrder(order)
        case Destroy() => applyDestroyOrder(order)
      }
    }
  }

  def applyMoveOrRetreatOrder(o : Order) = {
    val u = board.getUnit(o.unitId)
    val newLocation = o.target
    val newU = new DipUnit(gameId, u.country, u.unitType, u.startTurn, u.endTurn, u.unitId, newLocation)
    val newUnits = board.units.filter(un => un.unitId != u.unitId) ++ List(newU)

    if (season == Fall()) {
      val newMatrix = board.ownershipMatrix + (newU.location -> newU.country)
      board = new BoardState(newUnits, newMatrix)
    } else {
      board = new BoardState(newUnits, board.ownershipMatrix)
    }
  }

  def applyHoldConvoyOrSupportOrder(o : Order) = {
    if (season == Fall()) {
      val u = board.getUnit(o.unitId)
      val newMatrix = board.ownershipMatrix + (u.location -> u.country)
      board = new BoardState(board.units, newMatrix)
    }
  }

  def applyBuildOrder(o : Order) = {
    val u = Database.getUnit(gameId, o.unitId)
    // Build order targets look like: "fleet London" rather than just "London", so chop off the first word
    val orderTargetSplit = o.target.split(" ")
    val location = orderTargetSplit.slice(1, orderTargetSplit.length).mkString(" ")
    val newU = new DipUnit(gameId, u.country, u.unitType, u.startTurn, u.endTurn, u.unitId, location)
    val newUnits = board.units ++ List(newU)
    board = new BoardState(newUnits, board.ownershipMatrix)
  }

  def applyDestroyOrder(o : Order) = {
    board = new BoardState(board.units.filter(u => u.unitId != o.unitId), board.ownershipMatrix)
  }
}

