import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * Sample Spark application.
  *  Calculates volume of a sphere (r = 1) using the Monte Carlo method.
  */
object SparkApplication {

  case class Point(x: Double, y: Double, z: Double)

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      // number of workers
      .set("spark.executor.instances", "1")
      // number of cores on each workers
      .set("spark.executor.cores", "4")
      // size of RAM per executor
      .set("spark.executor.memory", "4g")

    val session = SparkSession
      .builder
      .appName("core_db migrate")
        .config(conf)
      .getOrCreate()

    val driver = "com.mysql.cj.jdbc.Driver"
    val dbHostname = "ds-db3.scivulcan.com"
    val dbPort = "3306"
    val dbName = "core_db"
    val url = s"jdbc:mysql://$dbHostname:$dbPort"
    val dbUser = "readonly"
    val dbPassword = "readonlySQL"

    val tableName = "citation"

    //val props = new Properties()
    //props.put("user", dbUser)
    //props.put("password", dbPassword)
    //val predicates = new Array[String](1)
    //predicates(0) = "id % 100 = 3"
    case class Partitioning(fetchSize: Int, partitionColumn: String, lowerBound: String, upperBound: String, numPartitions : Int) {
      def toOptions : Map[String, String] = Map (
        "fetchSize" → fetchSize.toString,
        "partitionColumn" → partitionColumn,
        "lowerBound" → lowerBound,
        "upperBound" → upperBound,
        "numPartitions" → numPartitions.toString
      )
    }

    object Partitioning {
      val citiation = Partitioning(1000, "id", "1", "502248885", 1000)
    }

    val table: DataFrame = //session.sqlContext.read.jdbc(url, "core_db.$tableName", predicates, props)
      session.sqlContext.read.format("jdbc").
      option("driver", driver).
      option("url", url).
      option("dbtable", s"$dbName.$tableName").
      option("user", dbUser).
      option("password", dbPassword).
      options(Partitioning.citiation.toOptions).
//      option("fetchSize", "1000").
//      option("partitionColumn", "id").
//      option("lowerBound", "1").
//      option("upperBound", "502248885"). //"20046865"
//      option("numPartitions", "1000").
      load()

    //table.createGlobalTempView("$tableName")
    println(s"Connected to $url")
    def inspect(r: Row): Unit = {
      val id = r.getInt(0)
      val name = r.getString(1)
      println(s"$id = $name")
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
