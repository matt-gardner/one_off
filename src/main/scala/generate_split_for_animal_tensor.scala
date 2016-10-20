import com.mattg.util.FileUtil

object generate_split_for_animal_tensor {
  val fileUtil = new FileUtil

  val trainFile = "/home/mattg/data/for_SFE/HPzootrain.txt"
  val validFile = "/home/mattg/data/for_SFE/HPzoodev.txt"
  val testFile = "/home/mattg/data/for_SFE/all_schema_consistent.csv"
  val splitDir = "/home/mattg/pra/splits/animals/"
  val graphDir = "/home/mattg/data/animal_tensor_kbc/"
  val metadataDir = "/home/mattg/pra/relation_metadata/animals/"
  val relationsToRunFile = splitDir + "relations_to_run.tsv"

  def main(args: Array[String]) {
    fileUtil.mkdirs(splitDir)
    val trainingTriples = getTriplesFromFile(trainFile)
    val validationTriples = getTriplesFromFile(validFile)
    val testTriples = getTriplesFromFile(testFile)
    // TODO(matt): handle cases where there is no training data for a test relation
    fileUtil.writeLinesToFile(relationsToRunFile, testTriples.map(_._2).toSet)
    writeTriplesToSplitFiles("training.tsv", trainingTriples ++ validationTriples)
    writeTriplesToSplitFiles("testing.tsv", testTriples)
    writeTriplesToGraphFile("train.tsv", trainingTriples)
    writeTriplesToGraphFile("valid.tsv", validationTriples)
    writeTriplesToGraphFile("test.tsv", testTriples)

    writeRelationMetadata(trainingTriples ++ validationTriples ++ testTriples)
  }

  def removeNps(triples: Seq[(String, String, String)]) = {
    triples.filterNot(t => t._1.contains(" ") || t._3.contains(" "))
  }

  def getTriplesFromFile(file: String) = {
    fileUtil.mapLinesFromFile(file, (line: String) => {
      val fields = line.split("\t")
      val triple = fields(0)
      val triple_fields = triple.split(",")
      val subj = triple_fields(0)
      val relation = triple_fields(1)
      val obj = triple_fields(2)
      (subj, relation, obj)
    })
  }

  def mapToTriples(map: Map[String, Seq[(String, String)]]) =
    map.flatMap(entry => entry._2.map(p => (p._1, entry._1, p._2))).toSeq

  def triplesToMap(triples: Seq[(String, String, String)]) =
    triples.groupBy(_._2).mapValues(_.map(triple => (triple._1, triple._3)).toSeq)

  def combineTriples(first: Map[String, Seq[(String, String)]], second: Map[String, Seq[(String, String)]]) = {
    triplesToMap(mapToTriples(first) ++ mapToTriples(second))
  }

  def writeTriplesToGraphFile(filename: String, triples: Seq[(String, String, String)]) {
    val lines = triples.map(t => s"${t._1}\t${t._2}\t${t._3}")
    val graphFile = graphDir + filename
    fileUtil.writeLinesToFile(graphFile, lines)
  }

  def writeTriplesToSplitFiles(filename: String, triples: Seq[(String, String, String)]) {
    for ((relation, instances) <- triplesToMap(triples)) {
      val fixed = relation.replace("/", "_")
      val testFile = splitDir + fixed + "/" + filename
      fileUtil.mkdirsForFile(testFile)
      fileUtil.writeLinesToFile(testFile, instances.map(i => s"${i._1}\t${i._2}"))
    }
  }

  def writeRelationMetadata(triples: Seq[(String, String, String)]) {
    // For now, we're just ignoring the domains and ranges, and saying that any node is possible
    // for all relations.
    val allEntities = triples.flatMap(t => Seq(t._1, t._3)).toSet
    fileUtil.mkdirs(metadataDir + "category_instances/")
    fileUtil.writeLinesToFile(metadataDir + "category_instances/everything", allEntities.toSeq)
    val allRelations = triples.map(_._2).toSet
    val domainAndRange = allRelations.map(r => s"${r}\teverything")
    fileUtil.writeLinesToFile(metadataDir + "domains.tsv", domainAndRange)
    fileUtil.writeLinesToFile(metadataDir + "ranges.tsv", domainAndRange)
  }
}
