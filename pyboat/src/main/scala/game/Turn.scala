package pyboat.game

import pyboat.Database

import scala.collection.mutable.Map
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

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
  /**
   * Orders are the list of Orders that resulted in this Turn's BoardState from last Turn.
   */
  var orders: List[Order] = Database.getOrdersForTurn(gameId, turnNum)

  override def toString() : String = {
    return "Turn " + turnNum + ": " + season + " " + phase + " " + year + " gID: " + gameId
  }

  def testBoardStateLocationMasks() : List[String] = {
    val strs: List[String] = (for (loc <- AllowedLocations.allowedLocations) yield board.testLookupTable(loc))(collection.breakOut)
    return strs
  }

  def testAllMasks() : String = {
    return board.testAllMasks()
  }

  /**
   * Gets this turn's BoardState as an INDArray of 21 x 21 x 7, where:
   * channel 0: 0   -> no unit here
   *            255 -> unit present
   * channel 1: 0   -> unit is not present
   *            128 -> unit is an army
   *            255 -> unit is a fleet
   * channel 2: 0   -> unit is owned by nobody
   *            36  -> unit is owned by Austria
   *            72  -> unit is owned by England
   *            109 -> unit is owned by France
   *            145 -> unit is owned by Germany
   *            182 -> unit is owned by Italy
   *            218 -> unit is owned by Russia
   *            255 -> location is owned by Turkey
   * channel 3: 0   -> location is land
   *            128 -> location is sea
   *            255 -> location is impassible
   * channel 4: 0   -> location is owned by nobody
   *            36  -> location is owned by Austria
   *            72  -> location is owned by England
   *            109 -> location is owned by France
   *            145 -> location is owned by Germany
   *            182 -> location is owned by Italy
   *            218 -> location is owned by Russia
   *            255 -> location is owned by Turkey
   * channel 5: 0   -> season is Spring or Winter
   *            255 -> season is Fall
   * channel 6: 0   -> phase is retreat
   *            128 -> phase is build
   *            255 -> phase is orders/winter
   * channel 7: 0   -> location is not an SC
   *            255 -> location is an SC
   */
  def getHoldOrMoveMatrix() : INDArray = {
    val channel0 = board.getUnitMask()
    val channel1 = board.getUnitTypeMask()
    val channel2 = board.getUnitOwnershipMask()
    val channel3 = board.getLandTypeMask()
    val channel4 = board.getLandOwnershipMask()
    val channel5 = if (season == Spring() || season == Winter()) Nd4j.zeros(1, 21, 21) else Nd4j.ones(1, 21, 21).mul(255)
    val channel6 = phase match {
      case WinterPhase()  => Nd4j.ones(1, 21, 21).mul(255)
      case OrdersPhase()   => Nd4j.ones(1, 21, 21).mul(255)
      case RetreatPhase() => Nd4j.zeros(1, 21, 21)
      case BuildPhase()   => Nd4j.ones(1, 21, 21).mul(128)
    }
    val channel7 = board.getSCMask()

    var matrix = Nd4j.concat(0, channel0, channel1)
    matrix = Nd4j.concat(0, matrix, channel2)
    matrix = Nd4j.concat(0, matrix, channel3)
    matrix = Nd4j.concat(0, matrix, channel4)
    matrix = Nd4j.concat(0, matrix, channel5)
    matrix = Nd4j.concat(0, matrix, channel6)
    matrix = Nd4j.concat(0, matrix, channel7)
    return matrix
  }

  /**
   * Gets this turn's list of orders (the orders that resulted in this turn's board state)
   * as an INDArray of 1 x 21 x 21, where 0 means a unit occupying that space either HOLD'd
   * or SUPPORT'd to HOLD, a 1 means a unit occupying that space either MOVE'd, CONVOY'd, or
   * SUPPORT'd to MOVE, and a 0.5 means that there is no order for that location.
   */
  def getOrderMaskAsHoldsOrMoves() : INDArray = {
    var mask = Nd4j.ones(1, 21, 21).mul(0.5)
    for (u <- board.units) {
      /* For each unit, get that unit's order - if we can't find it, it is because it was a destroy order */
      var uOrder: OrderType = null
      var orderIsSupportToHold = false
      try {
        val fullOrderObject = getOrderByUnit(u.unitId)
        uOrder = fullOrderObject.orderType
        orderIsSupportToHold = fullOrderObject.isSupportToHold()
      } catch {
        case npe: NullPointerException => uOrder = Destroy()
      }

      /* Now look up the correct value to associate with that order */
      val v = uOrder match {
        case Move() => 1.0
        case Hold() => 0.0
        case Convoy() => 1.0
        case Support() => if (orderIsSupportToHold) 0.0 else 1.0
        case Retreat() => 1.0
        case Build() => 0.0
        case Destroy() => 0.0
      }

      /* Put that value into the right location on the board */
      mask = board.putValueByLocation(v, u.location, mask)
    }
    return mask
  }

  def getOrderMaskAsHoldsOrMoves_DEBUG() : INDArray = {
    var sum: Double = 0
    for (u <- board.units) {
      /* For each unit, get that unit's order - if we can't find it, it is because it was a destroy order */
      var uOrder: OrderType = null
      var orderIsSupportToHold = false
      try {
        val fullOrderObject = getOrderByUnit(u.unitId)
        uOrder = fullOrderObject.orderType
        orderIsSupportToHold = fullOrderObject.isSupportToHold()
      } catch {
        case npe: NullPointerException => uOrder = Destroy()
      }

      /* Now look up the correct value to associate with that order */
      val v = uOrder match {
        case Move() => 1.0
        case Hold() => 0.0
        case Convoy() => 1.0
        case Support() => if (orderIsSupportToHold) 0.0 else 1.0
        case Retreat() => 1.0
        case Build() => 0.0
        case Destroy() => 0.0
      }
      sum += v
    }
    val avg = sum / board.units.length
    return if (avg > 0.5) Nd4j.ones(1, 1, 1) else Nd4j.zeros(1, 1, 1)
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
    if (!Database.turnExists(gameId, turnNum + 1))
      throw new NullPointerException("No turn with gameId: " + gameId + " and turnNum: " + (turnNum + 1))

    //println("Deriving turn: " + (turnNum + 1))
    var newTurn = Database.getTurn(gameId, turnNum + 1)
    newTurn.board = new BoardState(board.units, board.ownershipMatrix)
    val livingUnitIds = collection.mutable.Set[Int]()
    for (o <- newTurn.orders) {
      //println("  " + o)
      newTurn.applyOrder(o)
      livingUnitIds += o.unitId
    }
    // Now go through the units that DIDN'T input orders. In really old games (circa 2013), it was not illegal to not input orders
    // Make sure that the units that didn't input orders were destroyed - this can only happen by you being dislodged and not having
    // anywhere to go
    val destroyedCandidates = for (u <- newTurn.board.units if !livingUnitIds.contains(u.unitId)) yield u
    for (u <- destroyedCandidates) {
      if (!unitIsDislodged(u)) {
        // unit is not destroyed, unit should have a HOLD order instead
        livingUnitIds += u.unitId
        newTurn.orders = (new Order(gameId, u.unitId, Hold(), u.location, null, null, true, null, newTurn.turnNum)) :: newTurn.orders
      }
    }
    if (newTurn.phase != BuildPhase() && newTurn.phase != RetreatPhase()) {
      // Remove all units that did not enter an order this turn
      newTurn.board = new BoardState(newTurn.board.units.filter(
                                      u => livingUnitIds.contains(u.unitId)), newTurn.board.ownershipMatrix)
    }
    return newTurn
  }

  def applyOrder(order: Order) = {
    if (order.success) {
      order.orderType match {
        case Move() | Retreat() => applyMoveOrRetreatOrder(order)
        case Hold() | Convoy() | Support() => applyHoldConvoyOrSupportOrder(order)
        case Build() => applyBuildOrder(order)
        case Destroy() => applyDestroyOrder(order)
      }
    } else {
      val u = board.getUnit(order.unitId)
      if (season == Fall() && !unitIsDislodged(u)) {
        // As long as the unit is not dislodged, it takes ownership of the territory it is sitting on
        val newMatrix = updateMatrix(board.ownershipMatrix, u.location, u.country)
        board = new BoardState(board.units, newMatrix)
      }
    }
  }

  def unitIsDislodged(u: DipUnit) : Boolean = {
    for (o <- orders) {
      if (o.target == u.location && o.success && o.unitId != u.unitId) {
        // someone else successfully moved into u's location
        // get u's order and see if they succeeded in leaving this location
        // if they didn't, they are dislodged
        try {
          val uOrder = getOrderByUnit(u.unitId)
          if (!(uOrder.orderType == Move() && uOrder.success))
            return true
        } catch {
          // If the unit doesn't have an order, it should be a HOLD order
          case npe: NullPointerException => return true
        }
      }
    }
    return false
  }

  def getOrderByUnit(uid: Int) : Order = {
    for (o <- orders) {
      if (o.unitId == uid)
        return o
    }
    throw new NullPointerException("No order with unitId: " + uid)
  }

  def applyMoveOrRetreatOrder(o: Order) = {
    val u = board.getUnit(o.unitId)
    val newLocation = o.target
    val newU = new DipUnit(gameId, u.country, u.unitType, u.startTurn, u.endTurn, u.unitId, newLocation)
    val newUnits = board.units.filter(un => un.unitId != u.unitId) ++ List(newU)

    if (season == Fall()) {
      val newMatrix = updateMatrix(board.ownershipMatrix, newU.location, newU.country)
      board = new BoardState(newUnits, newMatrix)
    } else {
      board = new BoardState(newUnits, board.ownershipMatrix)
    }
  }

  def applyHoldConvoyOrSupportOrder(o: Order) = {
    if (season == Fall()) {
      val u = board.getUnit(o.unitId)
      val newMatrix = updateMatrix(board.ownershipMatrix, u.location, u.country)
      board = new BoardState(board.units, newMatrix)
    }
  }

  def applyBuildOrder(o: Order) = {
    val u = Database.getUnit(gameId, o.unitId)
    // Build order targets look like: "fleet London" rather than just "London", so chop off the first word
    val orderTargetSplit = o.target.split(" ")
    val location = orderTargetSplit.slice(1, orderTargetSplit.length).mkString(" ")
    val newU = new DipUnit(gameId, u.country, u.unitType, u.startTurn, u.endTurn, u.unitId, location)
    val newUnits = board.units ++ List(newU)
    board = new BoardState(newUnits, board.ownershipMatrix)
  }

  def applyDestroyOrder(o: Order) = {
    board = new BoardState(board.units.filter(u => u.unitId != o.unitId), board.ownershipMatrix)
  }

  /**
   * Creates a new ownership matrix from the given one and the update requested. Accounts for
   * coasts that change hands (which should therefore cause all other coasts and the mainland for that
   * country to change hands as well).
   */
  def updateMatrix(mat: collection.immutable.Map[String, CountryType],
                   location: String,
                   owner: CountryType) : collection.immutable.Map[String, CountryType] = {
    if (location == "St. Petersburg (South Coast)" || location == "St. Petersburg" || location == "St. Petersburg (North Coast)") {
      val update1 = mat + ("St. Petersburg (South Coast)" -> owner)
      val update2 = update1 + ("St. Petersburg" -> owner)
      val update3 = update2 + ("St. Petersburg (North Coast)" -> owner)
      return update3
    } else if (location == "Spain (North Coast)" || location == "Spain" || location == "Spain (South Coast)") {
      val update1 = mat + ("Spain (North Coast)" -> owner)
      val update2 = update1 + ("Spain" -> owner)
      val update3 = update2 + ("Spain (South Coast)" -> owner)
      return update3
    } else if (location == "Bulgaria (South Coast)" || location == "Bulgaria (East Coast)" || location == "Bulgaria") {
      val update1 = mat + ("Bulgaria (South Coast)" -> owner)
      val update2 = update1 + ("Bulgaria (East Coast)" -> owner)
      val update3 = update2 + ("Bulgaria" -> owner)
      return update3
    } else {
      return mat + (location -> owner)
    }
  }
}
//Turn 19: Fall() OrdersPhase() 1905 gID: 124311
