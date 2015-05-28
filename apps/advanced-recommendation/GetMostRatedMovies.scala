import com.datastax.spark.connector._, org.apache.spark.SparkConf, org.apache.spark.SparkContext, org.apache.spark.SparkContext._

// case class RatingRaw (userid:Long, movieid:Long, rating:Double, time: java.util.Date)
// case class Movie(movieid:Long,genres:Set[String],title:String,year:Int)

object GetMostRatedMovies {
  def main(args: Array[String]) {

    //    Create Spark Context
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")
	 // .set("spark.executor.memory","3g")
    val sc = new SparkContext("spark://Matthiass-MacBook-Pro.local:7077", "Simple Movie Recommendation", conf)

	val movies = sc.cassandraTable[Movie]("movie","movies").cache
	val data = sc.cassandraTable[RatingRaw]("movie","ratings_by_movie").cache
	data.map{case RatingRaw(u,m,r,t) => (m,r)}.join(movies.map{case Movie(m,g,t,y) => (m,t)}).map{case (m,(r,t)) => ((m,t),1)}.reduceByKey(_+_).sortBy(-_._2).map{case((m,t),_) => (m,t)}.repartition(1).saveAsTextFile("mostRatedMovies")


  }

}