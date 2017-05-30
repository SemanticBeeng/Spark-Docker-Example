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
  def inspect(r: Row): Unit = {
    val id = r.getInt(0)
    //val name = r.getString(1)
    println(s"$id")
    //(id, name)
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

