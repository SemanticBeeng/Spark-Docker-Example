import java.util.Properties

import org.apache.spark.sql.{DataFrame, SparkSession}

/**
  * Sample Spark application.
  *  Calculates volume of a sphere (r = 1) using the Monte Carlo method.
  */
object SparkApplication {

  case class Point(x: Double, y: Double, z: Double)

  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession
      .builder
      .appName("core_db migrate")
      .getOrCreate()

    val driver = "com.mysql.jdbc.Driver"
    val dbHostname = "ds-db6.scivulcan.com"
    val dbPort = "3306"
    val dbName = "core_db"
    val url = s"jdbc:mysql://$dbHostname:$dbPort"
    val dbUser = "nick"
    val dbPassword = "readonlySQL"

    val props = new Properties()
    props.put("user", dbUser)
    props.put("password", dbPassword)
    val predicates = new Array[String](1)
    predicates(0) = "id % 100 = 3"
    val concepts: DataFrame = //sparkSession.sqlContext.read.jdbc(url, "core_db.concept", predicates, props)
      sparkSession.sqlContext.read.format("jdbc").
      option("driver", driver).
      option("url", url).
      option("dbtable", s"$dbName.concept").
      option("user", "nick").
      option("password", "readonlySQL").
        load()
    concepts.createGlobalTempView("concept")
    println(s"Connected to $url")

//    import sparkSession.sqlContext.implicits._
//    def inspect(r: Row): (Long, String) = {
//      val id = r.getLong(0)
//      val name = r.getString(1)
//      println(s"$id = $name")
//      (id, name)
//    }

    import concepts.sqlContext.implicits._
    /**
      * https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala#L50
      */
    //val df = concepts.sqlContext.sql("select * from concept")
    concepts.write.format("parquet").save("concept.parquet")
        //.map(inspect)
    sparkSession.stop()
  }
}
