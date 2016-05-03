import com.mattg.util.FileUtil

object quoted_triples_to_tsv {
  val fileUtil = new FileUtil

  val tripleFile = "/home/mattg/data/aristo_kb/animal_tensor.txt"
  val outfile = "/home/mattg/data/aristo_kb/animal_tensor.tsv"

  def main(args: Array[String]) {
    val out = fileUtil.getFileWriter(outfile)
    for (line <- fileUtil.getLineIterator(tripleFile)) {
      val triple = line
      val tripleFields = triple.split("\" \"")
      val subject = tripleFields(0).substring(2)
      val verb = tripleFields(1)
      val obj = tripleFields(2).dropRight(2)
      out.write(s"$subject\t$verb\t$obj\n")
    }
    out.close()
  }
}
