package final_project

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.SparkContext
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDDgi
import org.apache.spark.graphx._
import org.apache.spark.storage.StorageLevel
import org.apache.log4j.{Level, Logger}

object main{
  val rootLogger = Logger.getRootLogger()
  rootLogger.setLevel(Level.ERROR)
  type VertexState = Int
  Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
  Logger.getLogger("org.spark-project").setLevel(Level.WARN)

  def LubyMaxImplementation[g_in: Graph[Int, Int]): Graph[Int, Int] = {
    maxNumIterations: Int = Int.MaxValue
    val g_in = graph
    val Unknown = 0
    val Selected = 1
    val Excluded = 2
    val seed = Random.nextLong
    val space = Random.nextInt
    var numIterations = 0L

    var misGraph = graph.mapVertices((_, _) => true)

    // compute working graph with data attribute
    var misWorkGraph = {
      // remove isolated vertices
      val g = graph.outerJoinVertices(graph.degrees) {
        (data, deg) => deg.getOrElse(0)
      }
    var numEdges = misWorkGraph.numEdges

    while (numEdges > 0 && numIterations < maxNumIterations) {
      //     inf v1 = g_copy.aggregateMessages[(Int, Float)]( trip => { // Map Function
  //          trip.sendToDst(if ((trip.srcAttr._2 + trip.srcAttr._1) > (trip.dstAttr._2 + trip.dstAttr._1)) (0, 0) else (1, 0));
  //        },
  //        (a,b) => ((math.min(a._1, b._1)), 0F)
  //    )

      numIterations = numIterations + 1

      // mark each Unknown vertex as Selected randomly
      misWorkGraph = misWorkGraph.mapVertices {
        (v, data) =>
          // randomly assign state Selected or Unknown
          val nextState = if (data._3.nextDouble < data._2) Selected else Unknown
          (nextState, data._2, data._3)
      }.cache()

      // mark the neighbors of each Selected vertex as Unknown
      misWorkGraph = misWorkGraph.joinVertices(misWorkGraph.aggregateMessages[VertexState](
        e => {
          if (e.srcAttr._1 == Selected && e.dstAttr._1 == Selected)
            (if (e.srcId < e.dstId) e.sendToDst _ else e.sendToSrc _)(Unknown)
        },
        (_, s) => s
      )) {
        (attr, state) => (state, attr._2, attr._3)
      }

      // identify the vertices to exclude, i.e., the vertices near Selected vertices
      val exVertices = misWorkGraph.aggregateMessages[VertexState](
        e => {
          if (e.srcAttr._1 == Selected && e.dstAttr._1 == Unknown)
            e.sendToDst(Excluded)
          else if (e.srcAttr._1 == Unknown && e.dstAttr._1 == Selected)
            e.sendToSrc(Excluded)
        },
        (_, s) => s
      ).filter(_._2 == Excluded).cache()

      if (exVertices.count() > 0) {
        // remove Excluded vertices from the candidate MIS
        misGraph = misGraph.joinVertices(exVertices)((_, _, _) => false)
        misWorkGraph = misWorkGraph.filter[Boolean, ED](
          g => {
            g.outerJoinVertices(g.aggregateMessages[Boolean](
              e => {
                val remove = e.srcAttr._1 == Selected || e.dstAttr._1 == Selected
                e.sendToDst(remove)
                e.sendToSrc(remove)
              }, _ || _
            )) {
              // remove the vertices didn't get any messages
              (_, _, b) => b.getOrElse(true)
            }
          },
          vpred = (_, b) => !b
        )
        // if there are no adjacent Unknown vertices in working graph then stop
        numEdges = misWorkGraph.numEdges
      }
    }
    misGraph
  }



  def main(args: Array[String]) {

    val conf = new SparkConf().setAppName("final_project")
    val sc = new SparkContext(conf)
    val spark = SparkSession.builder.config(conf).getOrCreate()
/* You can either use sc or spark */

    if(args.length == 0) {
      println("Usage: final_proj option = {compute, verify}")
      sys.exit(1)
    }
    if(args(0)=="compute") {
      if(args.length != 3) {
        println("Usage: final_proj compute graph_path output_path")
        sys.exit(1)
      }
      val startTimeMillis = System.currentTimeMillis()
      val edges = sc.textFile(args(1)).map(line => {val x = line.split(","); Edge(x(0).toLong, x(1).toLong , 0)} )
      val g = Graph.fromEdges[Int, Int](edges, 0, edgeStorageLevel = StorageLevel.MEMORY_AND_DISK, vertexStorageLevel = StorageLevel.MEMORY_AND_DISK)
      val g2 = LubyMaxImplementation(g)

      val endTimeMillis = System.currentTimeMillis()
      val durationSeconds = (endTimeMillis - startTimeMillis) / 1000
      println("==================================")
      println("Luby's bidding completed in " + durationSeconds + "s.")
      println("==================================")

      val g2df = spark.createDataFrame(g2) //g2.vertices
      // save and override
    }
    else
    {
        println("Usage: final_proj option = {compute, verify}")
        sys.exit(1)
    }
  }
}
