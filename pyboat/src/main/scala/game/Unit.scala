abstract class UnitType
case class Fleet() extends UnitType {}
case class Army() extends UnitType {}

/**
 * A class to represent a Unit in the game of Diplomacy.
 */
class DipUnit(val gameId: Int, val country: CountryType, val unitType: UnitType,
              val startTurn: Int, val endTurn: Int, val unitId: Int, var location: String) {
  require (AllowedLocations.allowedLocations.contains(location))
}
