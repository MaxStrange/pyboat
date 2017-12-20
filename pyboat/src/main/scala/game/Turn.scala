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

    println("Deriving new turn " + (turnNum + 1) + " from game ID " + gameId)
    val orders = Database.getOrdersForTurn(gameId, turnNum)
    var newTurn = Database.getTurn(gameId, turnNum + 1)
    newTurn.board = new BoardState(board.units, board.ownershipMatrix)
    val livingUnitIds = collection.mutable.Set[Int]()
    for (o <- orders) {
      println("Order: " + o)
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

