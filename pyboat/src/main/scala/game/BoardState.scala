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
}
