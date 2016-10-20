import com.mattg.util.FileUtil

import java.io.File

object merge_split {
  val fileUtil = new FileUtil

  val originalTestDir = "/home/mattg/pra/splits/animals/"
  val augmentedTestDir = "/home/mattg/pra/splits/animals_with_negatives/"
  val relationsFile = "/home/mattg/pra/splits/animals_with_negatives/relations_to_run.tsv"
  val outfile = "/home/mattg/data/animal_tensor_kbc/merged_training_data.tsv"

  def main(args: Array[String]) {
    val relations = fileUtil.readLinesFromFile(relationsFile)
    val relationMap = relations.map(r => (r.replace("/", "_") -> r)).toMap
    val originalTriples = fileUtil.recursiveListFiles(new File(originalTestDir), """training\.tsv$""".r).flatMap(file => {
      val nodePairs = fileUtil.readStringPairsFromFile(file.getAbsolutePath())
      val relationWithUnderscores = file.getParentFile().getName()
      val relation = relationMap.getOrElse(relationWithUnderscores, relationWithUnderscores)
      nodePairs.map(np => (np._1, relation, np._2, true))
    }).toSet
    val augmentedTriples = fileUtil.recursiveListFiles(new File(augmentedTestDir), """training\.tsv$""".r).flatMap(file => {
      val relationWithUnderscores = file.getParentFile().getName()
      val relation = relationMap(relationWithUnderscores)
      fileUtil.getLineIterator(file).map(line => {
        val fields = line.split("\t")
        val source = fields(0)
        val target = fields(1)
        val isPositive = if (fields.length > 2) fields(2) == "1" else true
        (source, relation, target, isPositive)
      }).toSet
    }).toSet
    val allTriples = (originalTriples ++ augmentedTriples).map(t => {
      val correctStr = if (t._4) "1" else "-1"
      s"${t._1}\t${t._2}\t${t._3}\t${correctStr}"
    })
    fileUtil.writeLinesToFile(outfile, allTriples)
  }
}
