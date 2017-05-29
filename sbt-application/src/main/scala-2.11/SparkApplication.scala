import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * Sample Spark application.
  *  Calculates volume of a sphere (r = 1) using the Monte Carlo method.
  */
object SparkApplication {

  case class Point(x: Double, y: Double, z: Double)

  def main(args: Array[String]): Unit = {

    import scala.concurrent.duration._
    val conf = new SparkConf()
      // number of workers
      .set("spark.executor.instances", "1")
      // number of cores on each workers
      .set("spark.executor.cores", "4")
      // size of RAM per executor
      .set("spark.executor.memory", "4g")
      .set("spark.network.timeout", 300.seconds.toMillis.toString)

    val session = SparkSession
      .builder
      .appName("core_db migrate")
        .config(conf)
      .getOrCreate()

    val driver = "com.mysql.cj.jdbc.Driver"
    val dbHostname = "ds-db3.scivulcan.com"
    val dbPort = "3306"
    val url = s"jdbc:mysql://$dbHostname:$dbPort"
    val dbUser = "readonly"
    val dbPassword = "readonlySQL"

    //val predicates = new Array[String](1)
    //predicates(0) = "id % 100 = 3"
    case class TablePartitioning(name :String, fetchSize: Int, partitionColumn: String, lowerBound: String, upperBound: String, numPartitions : Int) {
      val dbName = "core_db"

      def toOptions : Map[String, String] = Map (
        "dbtable" → s"$dbName.$name",
        "fetchSize" → fetchSize.toString,
        "partitionColumn" → partitionColumn,
        "lowerBound" → lowerBound,
        "upperBound" → upperBound,
        "numPartitions" → numPartitions.toString
      )
    }

    object Partitioning {
      val paper
        = TablePartitioning("paper",                1000, "id",     "1",   "27169771", 1000)
      val paper_to_author_v2
      = TablePartitioning("paper_to_author_v2",     1000, "id",     "1",  "103374152", 1000)
      val paper_to_concept
      = TablePartitioning("paper_to_concept",       1000, "id_inc", "1",  "171984568", 1000)
      val paper_to_venue
      = TablePartitioning("paper_to_venue",         1000, "id_inc", "1",   "27163980", 1000)
      val paper_to_institution
      = TablePartitioning("paper_to_institution",   1000, "id_inc", "1",   "51336994", 1000)

      val author
        = TablePartitioning("author_v2",1000, "id", "1",  "17167613", 1000)

      val concept
        = TablePartitioning("concept",                    1000, "id",         "1",  "19929833", 1000)
      val concept_to_semantic_type
        = TablePartitioning("concept_to_semantic_type",   1000, "id_inc",     "1",   "3227757", 1000)
      val concept_to_org_ref
      = TablePartitioning("concept_to_org_ref",           1000, "id_concept", "1",   "8906151", 1000)
      val concept_to_atom
      = TablePartitioning("concept_to_atom",              1000, "id_inc",      "1", "29458364", 1000)

      val citation
      = TablePartitioning("citation",     1000, "id", "1", "514293296", 1000)
      val institution
      = TablePartitioning("institution",  1000, "id", "1",    "427686", 1000)
      val semantic_type
      = TablePartitioning("semantic_type",1000, "id", "1",       "133", 1000)
      val venue
      = TablePartitioning("venue",        1000, "id", "1",     "36860", 1000)
    }

    val table: DataFrame = //session.sqlContext.read.jdbc(url, "core_db.$tableName", predicates, props)
      session.sqlContext.read.format("jdbc").
      option("driver", driver).
      option("url", url).
      option("user", dbUser).
      option("password", dbPassword).
      options(Partitioning.paper_to_concept.toOptions).
      load()

    //table.createGlobalTempView("$tableName")
    println(s"Connected to $url")
    def inspect(r: Row): Unit = {
      val id = r.getInt(0)
      //val name = r.getString(1)
      println(s"$id")
      //(id, name)
    }

    /**
      * https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala#L50
      */
    //val df = table.sqlContext.sql("select * from $tableName")
    println(s"Started query at ${System.currentTimeMillis()}")
    table.foreach(inspect(_))
    //table.write.format("parquet").save(s"$tableName.parquet")
    println(s"Finished query at ${System.currentTimeMillis()}")
    session.stop()
  }
}
