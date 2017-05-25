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
      .appName("core_db migrate")
      .getOrCreate()

    val driver = "com.mysql.jdbc.Driver"
    val dbHostname = "ds-db6.scivulcan.com"
    val dbPort = "3306"
    val dbName = "core_db"
    val url = s"jdbc:mysql://$dbHostname:$dbPort/$dbName"
    val dbUser = "nick"
    val dbPassword = "readonlySQL"

//    println("trying to connect")
//    Class.forName(driver)
//    import java.sql.DriverManager
//    val connection = DriverManager.getConnection(url, dbUser, dbPassword)
//    throw new Exception("Worked!")
//    println("connected")

    val concepts = sparkSession.sqlContext.read.
      format("jdbc").
      option("driver", driver).
      option("url", url).
      option("dbtable", "concept").
      option("user", "nick").
      option("password", "readonlySQL").load()

    import sparkSession.sqlContext.implicits._
    def inspect(r: Row): (Long, String) = {
      val id = r.getLong(0)
      val name = r.getString(1)
      println(s"$id = $name")
      (id, name)
    }

    concepts.sqlContext.sql("select * from concept")/*.toDF("id", "name")*/.map(inspect)
    sparkSession.stop()
  }
}
