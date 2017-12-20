abstract class CountryType() { val code : Char }
case class Austria() extends CountryType { val code = 'A' }
case class England() extends CountryType { val code = 'E' }
case class France() extends CountryType  { val code = 'F' }
case class Germany() extends CountryType { val code = 'G' }
case class Italy() extends CountryType   { val code = 'I' }
case class Russia() extends CountryType  { val code = 'R' }
case class Turkey() extends CountryType  { val code = 'T' }

