import org.apache.spark.sql.{Row, SparkSession}

/**
  * Sample Spark application.
  *  Calculates volume of a sphere (r = 1) using the Monte Carlo method.
  */
object SparkApplication {

  case class Point(x: Double, y: Double, z: Double)

  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession
      .builder
      .appName("Spark Pi")
      .getOrCreate()

    val n = 2950000

    val concepts = sparkSession.sqlContext.read.
      format("jdbc").
      option("url", "jdbc:mysql://ds-db6.scivulcan.com/core_db").
      option("driver", "com.mysql.jdbc.Driver").
      //option("dbtable", "concept").
      option("user", "nick").
      option("password", "readonly").load()

    import sparkSession.sqlContext.implicits._
    def inspect(r: Row): (Long, String) = {
      val id = r.getLong(0)
      val name = r.getString(1)
      println(s"$id = $name")
      (id, name)
    }

    concepts.sqlContext.sql("select * from concept").map(inspect)
    sparkSession.stop()
  }
}
