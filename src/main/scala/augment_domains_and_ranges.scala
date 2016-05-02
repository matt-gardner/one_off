import com.mattg.util.FileUtil

import org.json4s._
import org.json4s.native.JsonMethods.parse

object augment_domains_and_ranges {
  implicit val formats = DefaultFormats
  val fileUtil = new FileUtil

  val relationsToRunFile = "/home/mattg/pra/splits/hanie/relations_to_run.tsv"
  val freebaseJsonFile = "/home/mattg/data/freebase/domains_and_ranges.json"
  val domainsFile = "/home/mattg/pra/relation_metadata/freebase/domains.tsv"
  val rangesFile = "/home/mattg/pra/relation_metadata/freebase/ranges.tsv"
  val newDomainsFile = "/home/mattg/pra/relation_metadata/freebase/new_domains.tsv"
  val newRangesFile = "/home/mattg/pra/relation_metadata/freebase/new_ranges.tsv"

  def NOT_main(args: Array[String]) {
    val relations = fileUtil.readLinesFromFile(relationsToRunFile)
    val json = parse(fileUtil.readFileContents(freebaseJsonFile))
    val info = (json \ "result").extract[Seq[JValue]]
    val domainsAndRanges = info.map(jval => {
      val id = (jval \ "id").extract[String]
      val domain = (jval \ "schema").extract[String]
      val range = (jval \ "expected_type").extract[String]
      (id -> (domain, range))
    }).toMap
    val domains = fileUtil.readMapFromTsvFile(domainsFile)
    val ranges = fileUtil.readMapFromTsvFile(rangesFile)
    val newDomains: Map[String, String] = relations.flatMap(relation => {
      val firstRelation = if (relation.contains(".")) relation.split("\\.")(0) else relation
      domains.get(relation) match {
        case Some(domain) => Seq()
        case None => {
          domains.get(firstRelation) match {
            case Some(domain) => Seq((relation, domain))
            case None => {
              domainsAndRanges.get(firstRelation) match {
                case None => {
                  println(s"Didn't find domain for relation $relation")
                  val domain = firstRelation.substring(0, firstRelation.lastIndexOf("/"))
                  Seq((relation, domain + "CHECK ME"))
                }
                case Some((domain, range)) => Seq((relation, domain))
              }
            }
          }
        }
      }
    }).toMap
    val newRanges: Map[String, String] = relations.flatMap(relation => {
      if (relation.contains("legislative_sessions")) println(relation)
      val lastRelation = if (relation.contains(".")) relation.split("\\.").last else relation
      ranges.get(relation) match {
        case Some(range) => Seq()
        case None => {
          ranges.get(lastRelation) match {
            case Some(range) => Seq((relation, range))
            case None => {
              domainsAndRanges.get(lastRelation) match {
                case None => {
                  println(s"Didn't find range for relation $relation")
                  val category = lastRelation.substring(lastRelation.lastIndexOf("/") + 1)
                  val domain = lastRelation.substring(0, lastRelation.indexOf("/", 1))
                  val range = domain + "/" + category
                  Seq((relation, range + "CHECK ME"))
                }
                case Some((domain, range)) => Seq((relation, range))
              }
            }
          }
        }
      }
    }).toMap
    val finalDomains = (domains ++ newDomains).map(e => s"${e._1}\t${e._2}")
    val finalRanges = (ranges ++ newRanges).map(e => s"${e._1}\t${e._2}")
    fileUtil.writeLinesToFile(newDomainsFile, finalDomains)
    fileUtil.writeLinesToFile(newRangesFile, finalRanges)
  }
}
