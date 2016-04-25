import com.mattg.util.FileUtil

object generate_split_for_fb15k {
  val fileUtil = new FileUtil

  val testFile = "/home/mattg/pra/splits/hanie/freebase_mtr100_mte100-test.txt"
  val splitDir = "/home/mattg/pra/splits/hanie/"
  val relationsToRunFile = splitDir + "relations_to_run.tsv"

  def main(args: Array[String]) {
    val triples = fileUtil.mapLinesFromFile(testFile, (line: String) => {
      val fields = line.split("\t")
      val subj = fields(0)
      val relation = fields(1)
      val obj = fields(2)
      (relation, subj, obj)
    })
    val relationInstances = triples.groupBy(_._1).mapValues(_.map(triple => (triple._2, triple._3)))
    fileUtil.writeLinesToFile(relationsToRunFile, relationInstances.keys)
    for ((relation, instances) <- relationInstances) {
      val fixed = relation.replace("/", "_")
      val testFile = splitDir + fixed + "/testing.tsv"
      fileUtil.mkdirsForFile(testFile)
      fileUtil.writeLinesToFile(testFile, instances.map(i => s"${i._1}\t${i._2}"))
    }
  }
}
