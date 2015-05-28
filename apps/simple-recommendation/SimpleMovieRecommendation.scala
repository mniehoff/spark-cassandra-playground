import com.datastax.spark.connector._, org.apache.spark.SparkConf, org.apache.spark.SparkContext, org.apache.spark.SparkContext._
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating

case class RatingRaw (userid:Long, movieid:Long, rating:Double, time: java.util.Date)

object SimpleMovieRecommendation {
  def main(args: Array[String]) {

    //    Create Spark Context
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")
	 // .set("spark.executor.memory","3g")
    val sc = new SparkContext("spark://Matthiass-MacBook-Pro.local:7077", "Simple Movie Recommendation", conf)

	val data = sc.cassandraTable[RatingRaw]("movie","ratings_by_movie_4m")
	val ratings = data.map{case RatingRaw(user,movie,rating,_) => Rating(user.toInt,movie.toInt,rating-2.5)}.cache

	val rank = 20
	val numIterations = 20
	val model = ALS.trainImplicit(ratings, rank, numIterations)

	// Evaluate the model on rating data
	val usersProducts = ratings.map { case Rating(user, product, rate) =>
	  (user, product)
	}
	val predictions = 
	  model.predict(usersProducts).map { case Rating(user, product, rate) => 
	    ((user, product), rate)
	  }
	val ratesAndPreds = ratings.map { case Rating(user, product, rate) => 
	  ((user, product), rate)
	}.join(predictions)
	val MSE = ratesAndPreds.map { case ((user, product), (r1, r2)) => 
	  val err = (r1 - r2)
	  err * err
	}.mean()
	println("Rank: " + rank + ", Iterations: " +  numIterations + ", Mean Squared Error = " + MSE)

  }
}