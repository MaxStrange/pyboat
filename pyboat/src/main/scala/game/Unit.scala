abstract class UnitType { val code: Char }
case class Fleet() extends UnitType { val code = 'F' }
case class Army() extends UnitType { val code = 'A' }

/**
 * A class to represent a Unit in the game of Diplomacy.
 */
class DipUnit(val gameId: Int, val country: CountryType, val unitType: UnitType,
              val startTurn: Int, val endTurn: Int, val unitId: Int, var location: String) {
  require(AllowedLocations.allowedLocations.contains(location) || location == null)

  override def toString() : String = {
    return "" + unitId + ": " + unitType.code + " " + location + " owned by " + country.code
  }
}
