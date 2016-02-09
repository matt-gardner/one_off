import edu.cmu.ml.rtw.users.matt.util.FileUtil

import org.json4s._
import org.json4s.native.JsonMethods.parse

object find_freebase_mediators {
  implicit val formats = DefaultFormats

  val fileUtil = new FileUtil()
  val domainsFile = "/home/mattg/pra/relation_metadata/freebase/domains.tsv"
  val rangesFile = "/home/mattg/pra/relation_metadata/freebase/ranges.tsv"
  val mediatorJson = "/home/mattg/data/freebase/mediators.json"
  val mediatorsFile = "/home/mattg/clone/pra/src/main/resources/freebase_mediators.tsv"
  val mediatorRelationsFile = "/home/mattg/clone/pra/src/main/resources/freebase_mediator_relations.tsv"

  def main(args: Array[String]) {
    val domains = fileUtil.readStringPairsFromFile(domainsFile).toMap
    val ranges = fileUtil.readStringPairsFromFile(rangesFile).toMap
    val mediators = readMediatorJson()
    val relations = domains.map(_._1) ++ ranges.map(_._1)
    val mediatorRelations = relations.filter(r => {
      val domainAndRange = Set(domains.getOrElse(r, ""), ranges.getOrElse(r, ""))
      domainAndRange.intersect(mediators).size > 0
    }).toSet
    fileUtil.writeLinesToFile(mediatorsFile, mediators.toSeq.sorted)
    fileUtil.writeLinesToFile(mediatorRelationsFile, mediatorRelations.toSeq.sorted)
  }

  def readMediatorJson(): Set[String] = {
    val text = fileUtil.readLinesFromFile(mediatorJson).mkString("\n")
    val json = parse(text)
    json.filterField(_._1 == "id").map(_._2.extract[String]).toSet
  }
}
