import com.mattg.util.FileUtil

import java.io.File

object merge_scores {
  val fileUtil = new FileUtil

  val scoresDir = "/home/mattg/pra/results/animal/pra/"
  val outfile = "/home/mattg/data/animal_tensor_kbc/pra_results.csv"

  def main(args: Array[String]) {
    val tripleScores = fileUtil.recursiveListFiles(new File(scoresDir), """scores\.tsv$""".r).par.flatMap(file => {
      val relation = file.getParentFile().getName()
      fileUtil.flatMapLinesFromFile(file.getAbsolutePath(), (line) => {
        if (line.isEmpty) {
          Seq()
        } else {
          val fields = line.split("\t")
          val source = fields(0)
          val target = fields(1)
          val score = fields(2).toDouble
          Seq((source, relation, target, score))
        }
      }).sortBy(_._4).reverse
    })
    val lines = tripleScores.map(t => {
      s"${t._1},${t._2},${t._3},${t._4}"
    }).seq
    fileUtil.writeLinesToFile(outfile, lines)
  }
}

