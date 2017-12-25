package pyboat.game

/**
 * BoardState represents the state of the game board at a given turn.
 *
 * This class has units and a location ownership matrix.
 * @param units The list of DipUnit objects present on the board.
 * @param ownershipMatrix A Map of the form Map(location: String -> player: CountryType)
 */
class BoardState(val units: List[DipUnit], val ownershipMatrix: Map[String, CountryType]) {
  def getUnit(unitId: Int) : DipUnit = {
    for (u <- units) {
      if (u.unitId == unitId)
        return u
    }
    throw new NullPointerException("No unit with unitId: " + unitId)
  }

  def historyString() : String = {
    var sb = StringBuilder.newBuilder
    sb.append("    BOARD STATE:\n")
    for (u <- units) {
      sb.append("      " + u + "\n")
    }
    sb.append("\n")
    for ((location, owner) <- ownershipMatrix) {
      sb.append("      " + location + " owned by " + owner + "\n")
    }
    return sb.toString()
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> no unit here
   * 255 -> unit present
   */
  def getUnitMask() : INDArray = {
    //TODO
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> unit is an army
   * 255 -> unit is a fleet
   */
  def getUnitTypeMask() : INDArray = {
    //TODO
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> unit is owned by nobody
   * 36  -> unit is owned by Austria
   * 72  -> unit is owned by England
   * 109 -> unit is owned by France
   * 145 -> unit is owned by Germany
   * 182 -> unit is owned by Italy
   * 218 -> unit is owned by Russia
   * 255 -> unit is owned by Turkey
   */
  def getUnitOwnershipMask() : INDArray = {
    //TODO
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> location is land
   * 128 -> location is impassible
   * 255 -> location is sea
   */
 def getLandTypeMask() : INDArray = {
    //TODO
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> location is owned by nobody
   * 36  -> location is owned by Austria
   * 72  -> location is owned by England
   * 109 -> location is owned by France
   * 145 -> location is owned by Germany
   * 182 -> location is owned by Italy
   * 218 -> location is owned by Russia
   * 255 -> location is owned by Turkey
   */
  def getLandOwnershipMask() : INDArray = {
    //TODO
  }

  /**
   * 21 x 21 INDArray where:
   * 0   -> location is not an SC
   * 255 -> location is an SC
   */
  def getSCMask() : INDArray = {
    //TODO
  }
}
