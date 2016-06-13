
case class Recursive(name: String, arguments: Seq[Recursive]) {
  def slowToString(): String = {
    s"$name(${arguments.mkString(", ")}"
  }

  def fastToString(): String = {
    val argString = arguments.mkString(", ")
    s"$name($argString)"
  }
}

object string_interpolation_test {
  def main(args: Array[String]) {
    val depth = 200
    var current = Recursive("base", Seq())
    for (i <- 0 until depth) {
      current = Recursive(s"level_$i", Seq(current))
    }
    println("Starting")
    var start = System.currentTimeMillis
    val slow = current.slowToString
    var end = System.currentTimeMillis
    println(s"Done computing slow; took ${end - start} millis")
    start = System.currentTimeMillis
    val fast = current.fastToString
    end = System.currentTimeMillis
    println(s"Done computing fast; took ${end - start} millis")
  }


}
