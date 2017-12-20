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
}
