import com.mattg.util.FileUtil

object augment_domains_and_ranges {
  val fileUtil = new FileUtil

  val relationsToRunFile = "/home/mattg/pra/splits/hanie/relations_to_run.tsv"
  val domainsFile = "/home/mattg/pra/relation_metadata/freebase/domains.tsv"
  val rangesFile = "/home/mattg/pra/relation_metadata/freebase/ranges.tsv"
  val newDomainsFile = "/home/mattg/pra/relation_metadata/freebase/new_domains.tsv"
  val newRangesFile = "/home/mattg/pra/relation_metadata/freebase/new_ranges.tsv"

  def main(args: Array[String]) {
    val relations = fileUtil.readLinesFromFile(relationsToRunFile)
    val domains = fileUtil.readMapFromTsvFile(domainsFile)
    val ranges = fileUtil.readMapFromTsvFile(rangesFile)
    val newDomains: Map[String, String] = relations.flatMap(relation => {
      domains.get(relation) match {
        case None => {
          val firstRelation = if (relation.contains(".")) relation.split("\\.")(0) else relation
          val domain = firstRelation.substring(0, firstRelation.lastIndexOf("/"))
          Seq((relation, domain + "CHECK ME"))
        }
        case Some(domain) => Seq()
      }
    }).toMap
    val newRanges: Map[String, String] = relations.flatMap(relation => {
      ranges.get(relation) match {
        case None => {
          val lastRelation = if (relation.contains(".")) relation.split("\\.").last else relation
          val category = lastRelation.substring(lastRelation.lastIndexOf("/") + 1)
          val domain = lastRelation.substring(0, lastRelation.indexOf("/", 1))
          val range = domain + "/" + category
          Seq((relation, range + "CHECK ME"))
        }
        case Some(range) => Seq()
      }
    }).toMap
    val finalDomains = (domains ++ newDomains).map(e => s"${e._1}\t${e._2}")
    val finalRanges = (ranges ++ newRanges).map(e => s"${e._1}\t${e._2}")
    fileUtil.writeLinesToFile(newDomainsFile, finalDomains)
    fileUtil.writeLinesToFile(newRangesFile, finalRanges)
  }
}
