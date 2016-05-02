import com.mattg.util.FileUtil

object generate_split_for_fb15k {
  val fileUtil = new FileUtil

  val trainFile = "/home/mattg/data/freebase/fb15k_train.tsv"
  val validFile = "/home/mattg/data/freebase/fb15k_valid.tsv"
  val testFile = "/home/mattg/data/freebase/fb15k_test.tsv"
  val splitDir = "/home/mattg/pra/splits/fb15k/"
  val relationsToRunFile = splitDir + "relations_to_run.tsv"

  def main(args: Array[String]) {
    fileUtil.mkdirs(splitDir)
    val trainingTriples = getTriplesFromFiles(Seq(trainFile, testFile))
    writeTriplesToFile("training.tsv", trainingTriples)
    val testTriples = getTriplesFromFiles(Seq(testFile))
    fileUtil.writeLinesToFile(relationsToRunFile, testTriples.keys)
    writeTriplesToFile("testing.tsv", testTriples)
  }

  def getTriplesFromFiles(files: Seq[String]) = {
    val triples = files.flatMap(file => fileUtil.mapLinesFromFile(file, (line: String) => {
      val fields = line.split("\t")
      val subj = fields(0)
      val relation = fields(1)
      val obj = fields(2)
      (relation, subj, obj)
    }))
    triples.groupBy(_._1).mapValues(_.map(triple => (triple._2, triple._3)))
  }

  def writeTriplesToFile(filename: String, relationInstances: Map[String, Seq[(String, String)]]) {
    for ((relation, instances) <- relationInstances) {
      val fixed = relation.replace("/", "_")
      val testFile = splitDir + fixed + "/" + filename
      fileUtil.mkdirsForFile(testFile)
      fileUtil.writeLinesToFile(testFile, instances.map(i => s"${i._1}\t${i._2}"))
    }
  }
}
