package edu.cmu.ml.rtw.users.matt.one_off

import edu.cmu.ml.rtw.users.matt.util.FileHelper
import edu.cmu.ml.rtw.users.matt.util.FileUtil
import edu.cmu.ml.rtw.pra.graphs.GraphBuilder

import java.io.File

import scala.collection.mutable
import scala.collection.JavaConverters._

object features_for_bishan {
  val fileUtil = new FileUtil()
  val graphBase = "/home/mg1/data/bishan/doc_graph/"
  val outDirectory = "/home/mg1/data/bishan/split/"

  def main(args: Array[String]) {
    val edgelists = FileHelper.recursiveListFiles(new File(graphBase), """.*\.edgelist$""".r)
    val relations = edgelists.par.map(edgelistFile => {
      val (graph, instances) = processEdgeListFile(edgelistFile)
      val lines = instances.map(sourceTarget => {
        sourceTarget._1 + "\t" + sourceTarget._2 + "\t1\t" + graph
      })
      val relation = edgelistFile.getName().replace(".edgelist", "")
      fileUtil.mkdirs(outDirectory + relation + "/")
      val outfile = outDirectory + relation + "/training.tsv"
      fileUtil.writeLinesToFile(outfile, lines.asJava)
      relation
    }).seq
    fileUtil.writeLinesToFile(outDirectory + "relations_to_run.tsv", relations.asJava)
  }

  def processEdgeListFile(file: File): (String, Seq[(String, String)]) = {
    val instances = new mutable.HashSet[(String, String)]
    // Not the most computationally efficient thing in the world to construct a graph, with
    // dictionaries and an odd internal representation, and then convert it back to triples and
    // write it out as strings.  But the code is easier, so that's what I'm doing for now.
    val builder = new GraphBuilder
    for (line <- fileUtil.readLinesFromFile(file).asScala) {
      val fields = line.split(" ")
      val source = fields(0)
      val target = fields(1)
      val relation = if (fields.size == 4) {
          fields(3).substring(1, fields(3).size - 2)
        } else if (fields.size == 6) {
          fields(5).substring(1, fields(5).size - 2)
        } else {
          throw new RuntimeException(s"Unrecognized line format: $line")
        }
      builder.addEdge(source, target, relation)
      if (source.startsWith("m_") && target.startsWith("m_")) {
        instances += Tuple2(source, target)
      }
    }
    (builder.toGraphInMemory.writeToInstanceGraph, instances.toSeq)
  }
}
