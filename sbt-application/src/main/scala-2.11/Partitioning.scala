
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.spark.sql.Row


case class TablePartitioning(name :String, fetchSize: Int, partitionColumn: String, lowerBound: Long, upperBound: Long, numPartitions : Int) {
  val dbName = "core_db"

  def toOptions : Map[String, String] = Map (
    "dbtable" → s"$dbName.$name",
    "fetchSize" → fetchSize.toString,
    "partitionColumn" → partitionColumn,
    "lowerBound" → lowerBound.toString,
    "upperBound" → upperBound.toString,
    "numPartitions" → numPartitions.toString
  )
}

object Partitioning {

//  import org.apache.spark.streaming._
//
//  def makeScc(conf : SparkConf) = {
//    val ssc = new StreamingContext(conf, Seconds(2))
//
//    //val topicMap = topics.split(",").map((_, numThreads.toInt)).toMap
//    val zkQuorum = "123"
//    val topics = "ccore_db"
//    val groupId = "group1"
//    val numThreads = 3
//    val topicMap = topics.split(",").map((_, numThreads)).toMap
//    //val lines: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(ssc, zkQuorum, groupId , topicMap)//.map(_._2)
//    ssc
//  }

  val kafkaOpTopic = "coredb-output"

  /**
    * https://docs.cloud.databricks.com/docs/latest/databricks_guide/07%20Spark%20Streaming/09%20Write%20Output%20To%20Kafka.html
    */
  def makeProducer = {

    val kafkaBrokers = "localhost:9092"

    import ProducerConfig._
    import scala.collection.JavaConverters._
    val props = Map[String, Object](
      BOOTSTRAP_SERVERS_CONFIG → kafkaBrokers,
      VALUE_SERIALIZER_CLASS_CONFIG → "org.apache.kafka.common.serialization.StringSerializer",
      KEY_SERIALIZER_CLASS_CONFIG → "org.apache.kafka.common.serialization.StringSerializer").asJava

    new KafkaProducer[String, String](props)
  }

  def send(producer: KafkaProducer[String, String], data: String) = {
    producer.send(new ProducerRecord[String, String](kafkaOpTopic, null, data))
  }

  def process(producer: KafkaProducer[String, String], r: Row): Unit = {
    val id = r.getInt(0)
    //val name = r.getString(1)
    println(s"$id")
    //(id, name)

    send(producer, r.toString())
  }

  private val numPartitions = 1000
  val paper
  = TablePartitioning("paper",                numPartitions, "id",       1,    27169771, numPartitions)
  val paper_to_author_v2
  = TablePartitioning("paper_to_author_v2",     numPartitions, "id",     1,   103374152, numPartitions)
  val paper_to_concept
  = TablePartitioning("paper_to_concept",       numPartitions, "id_inc", 1,   171984568, numPartitions)
  val paper_to_venue
  = TablePartitioning("paper_to_venue",         numPartitions, "id_inc", 1,    27163980, numPartitions)
  val paper_to_institution
  = TablePartitioning("paper_to_institution",   numPartitions, "id_inc", 1,    51336994, numPartitions)

  val author
  = TablePartitioning("author_v2",numPartitions, "id", 1,  17167613, numPartitions)

  val concept
  = TablePartitioning("concept",                    numPartitions, "id",         1,  19929833, numPartitions)
  val concept_to_semantic_type
  = TablePartitioning("concept_to_semantic_type",   numPartitions, "id_inc",     1,   3227757, numPartitions)
  val concept_to_org_ref
  = TablePartitioning("concept_to_org_ref",         numPartitions, "id_concept", 1,   8906151, numPartitions)
  val concept_to_atom
  = TablePartitioning("concept_to_atom",            numPartitions, "id_inc",     1,  29458364, numPartitions)

  val citation
  = TablePartitioning("citation",     numPartitions, "id", 1,  514293296, numPartitions)
  val institution
  = TablePartitioning("institution",  numPartitions, "id", 1,    427686, numPartitions)
  val semantic_type
  = TablePartitioning("semantic_type",numPartitions, "id", 1,       133, numPartitions)
  val venue
  = TablePartitioning("venue",        numPartitions, "id", 1,     36860, numPartitions)
}

