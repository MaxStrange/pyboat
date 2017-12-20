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
            val success : Bool, val reason : String, val turnNum : Int) {
  orderType match {
    case m: Move => require(AllowedLocations.contains(location) && AllowedLocations.contains(target))
    case h: Hold => require(AllowedLocations.contains(location))
    case c: Convoy => require(AllowedLocations.contains(location) && AllowedLocations.contains(target)
                              && AllowedLocations.contains(targetDest))
    case s: Support => require(AllowedLocations.contains(location) && AllowedLocations.contains(target)
                              && AllowedLocations.contains(targetDest))
    case b: Build => ;
    case r: Retreate => require(AllowedLocations.contains(location) && AllowedLocations.contains(target))
    case d: Destroy => require(AllowedLocations.contains(location))
  }
}
