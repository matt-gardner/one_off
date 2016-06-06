import com.mattg.util.FileUtil

object generate_split_for_animal_tensor {
  val fileUtil = new FileUtil

  val trainFile = "/home/mattg/data/animal_tensor_kbc/HPzootrain3.txt"
  val validFile = "/home/mattg/data/animal_tensor_kbc/HPzoodev3.txt"
  val testFile = "/home/mattg/data/animal_tensor_kbc/HPzootest3.txt"
  val testFileWithNegatives = "/home/mattg/data/animal_tensor_kbc/hole_top_10s.tsv"
  val splitDir = "/home/mattg/pra/splits/animals/"
  val relationsToRunFile = splitDir + "relations_to_run.tsv"

  def main(args: Array[String]) {
    fileUtil.mkdirs(splitDir)
    val trainingTriples = getTriplesFromFiles(Seq(trainFile, validFile))
    writeTriplesToFile("training.tsv", trainingTriples)
    val testTriples = getTriplesFromFiles(Seq(testFile))
    fileUtil.writeLinesToFile(relationsToRunFile, testTriples.keys)
    writeTriplesToFile("testing.tsv", testTriples)
  }

  def getTriplesFromFiles(files: Seq[String]) = {
    val triples = files.flatMap(file => fileUtil.mapLinesFromFile(file, (line: String) => {
      val fields = line.split("\t")
      val triple = fields(0)
      val triple_fields = triple.split(",")
      val subj = triple_fields(0)
      val relation = triple_fields(1)
      val obj = triple_fields(2)
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
