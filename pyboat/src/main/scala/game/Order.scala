package pyboat.game

abstract class OrderType
case class Move() extends OrderType {}
case class Hold() extends OrderType {}
case class Convoy() extends OrderType {}
case class Support() extends OrderType {}
case class Build() extends OrderType {}
case class Retreat() extends OrderType {}
case class Destroy() extends OrderType {}

class Order(val gameId : Int, val unitId : Int, val orderType : OrderType,
            val location : String, val target : String, val targetDest : String,
            val success : Boolean, val reason : String, val turnNum : Int) {
  // There are 40 Orders in the database where a CONVOY was input incorrectly and the target ends up being ""
  if (reason != "Invalid order or syntax error") {
    orderType match {
      case m: Move => require(AllowedLocations.contains(location) && AllowedLocations.contains(target))
      case h: Hold => require(AllowedLocations.contains(location))
      case c: Convoy => require(AllowedLocations.contains(location) && AllowedLocations.contains(target)
                                && AllowedLocations.contains(targetDest))
      case s: Support => require(AllowedLocations.contains(location) && AllowedLocations.contains(target)
                                && AllowedLocations.contains(targetDest))
      case b: Build => ;
      case r: Retreat => require(AllowedLocations.contains(location) && AllowedLocations.contains(target))
      case d: Destroy => require(AllowedLocations.contains(location))
    }
  }

  def isSupportToHold() : Boolean = {
    return (this == Support()) && (target == targetDest)
  }

  override def toString() : String = {
    val str = orderType match {
      case Move() => location + " MOVE to " + target + " ? " + success + ": " + reason
      case Hold() => location + " HOLDs ? " + success + ": " + reason
      case Convoy() => location + " CONVOY " + target + " to " + targetDest + " ? " + success + ": " + reason
      case Support() => location + " SUPPORT " + target + " to " + targetDest + " ? " + success + ": " + reason
      case Build() => " BUILD " + target + " ? " + success + ": " + reason
      case Retreat() => location + " RETREAT to " + target + " ? " + success + ": " + reason
      case Destroy() => location + " DESTROY ? " + success + ": " + reason
    }
    return "" + unitId + " " + str
  }
}
