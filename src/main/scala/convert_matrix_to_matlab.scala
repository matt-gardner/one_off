import com.mattg.util.FileUtil
import com.mattg.util.MutableConcurrentDictionary

object convert_matrix_to_matlab {
  val fileUtil = new FileUtil

  val tripleFile = "/home/mattg/data/erosion_triples.txt"
  val outfile = "/home/mattg/data/erosion_triples_matlab.tsv"
  val dictionaryFile = "/home/mattg/data/erosion_triples_dictionary.tsv"
  val wordDictionary = new MutableConcurrentDictionary

  def main(args: Array[String]) {
    val out = fileUtil.getFileWriter(outfile)
    for (line <- fileUtil.getLineIterator(tripleFile)) {
      val fields = line.split("\t")
      val count = fields(0).toInt
      val triple = fields(1)
      val tripleFields = triple.split("\" \"")
      val subject = tripleFields(0).substring(2)
      val verb = tripleFields(1)
      val obj = tripleFields(2).dropRight(2)
      val subjectIndex = wordDictionary.getIndex(subject)
      val verbIndex = wordDictionary.getIndex(verb)
      val objectIndex = wordDictionary.getIndex(obj)
      out.write(s"$subjectIndex\t$verbIndex\t$objectIndex\t$count\n")
    }
    out.close()
    wordDictionary.writeToFile(dictionaryFile)
  }
}
