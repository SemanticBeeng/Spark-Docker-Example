import org.apache.spark.SparkConf
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

/**
  * Sample Spark application.
  *  Calculates volume of a sphere (r = 1) using the Monte Carlo method.
  */
object SparkApplication {

  def main(args: Array[String]): Unit = {

    import scala.concurrent.duration._
    val conf = new SparkConf()
      // number of workers
      .set("spark.executor.instances", "1")
      // number of cores on each workers
      .set("spark.executor.cores", "4")
      // size of RAM per executor
      .set("spark.executor.memory", "5g")
      .set("spark.driver.memory", "3g")
      // https://stackoverflow.com/questions/37260230/spark-cluster-full-of-heartbeat-timeouts-executors-exiting-on-their-own
      .set("spark.network.timeout", 300.seconds.toMillis.toString)
      // https://databricks.com/blog/2015/05/28/tuning-java-garbage-collection-for-spark-applications.html
      //.set("spark.executor.extraJavaOptions", "-XX:+UseG1GC -XX:InitiatingHeapOccupancyPercent=35 -XX:ConcGCThread=20 " +
                                              //"-XX:+PrintFlagsFinal -XX:+PrintReferenceGC -verbose:gc " +
                                              //"-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintAdaptiveSizePolicy " +
                                              //"-XX:+UnlockDiagnosticVMOptions -XX:+G1SummarizeConcMark ")

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

    val table = Partitioning.citation

    val rdd: DataFrame = //session.sqlContext.read.jdbc(url, "core_db.$tableName", predicates, props)
      session.sqlContext.read.format("jdbc").
      option("driver", driver).
      option("url", url).
      option("user", dbUser).
      option("password", dbPassword).
      options(table.toOptions).
      load()

    //rdd.createGlobalTempView("$tableName")
    println(s"Connected to $url")
    /**
      * https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/sql/SQLDataSourceExample.scala#L50
      */
    //val df = rdd.sqlContext.sql("select * from $tableName")
    println(s"Started query at ${System.currentTimeMillis()}")
    val count = session.sparkContext.longAccumulator

    rdd.foreachPartition((partitionRows: Iterator[Row]) ⇒ {
        partitionRows.foreach {r ⇒ Partitioning.inspect(r); count.add(1)}
        //session.sparkContext.broadcast(count)
      }
    )
    //rdd.write.format("parquet").save(s"$tableName.parquet")
    println(s"Finished query at ${System.currentTimeMillis()}; found ${count.value} records")
    Predef.assert(count.value == table.upperBound)
    session.stop()
  }
}
