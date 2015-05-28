import com.datastax.spark.connector._, org.apache.spark.SparkConf, org.apache.spark.SparkContext, org.apache.spark.SparkContext._
import org.apache.spark.mllib.recommendation.ALS
import org.apache.spark.mllib.recommendation.MatrixFactorizationModel
import org.apache.spark.mllib.recommendation.Rating
import scala.util.Random
import org.apache.log4j.Logger
import org.apache.log4j.Level

case class RatingRaw(userid: Long, movieid: Int, rating: Double, time: java.util.Date)
case class Movie(movieid: Int, genres: Set[String], title: String, year: Int)


/**
* Inspired by and partially taken from: http://ampcamp.berkeley.edu/big-data-mini-course/movie-recommendation-with-mllib.html and 
* https://github.com/amplab/training/blob/ampcamp-china-1/machine-learning/scala/solution/MovieLensALS.scala
*/
object AdvancedRecommendation {
  def main(args: Array[String]) {

	Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
	Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    //    Create Spark Context
    val conf = new SparkConf(true)
      .set("spark.cassandra.connection.host", "127.0.0.1")
    // .set("spark.executor.memory","3g")
    val sc = new SparkContext("spark://Matthiass-MacBook-Pro.local:7077", "Advanced Movie Recommendation", conf)

    // Read Movies
    val movies = sc.cassandraTable[Movie]("movie", "movies").map { case Movie(m, g, t, y) => (m,t) }.cache
    
    // Read Ratings
    val ratings = sc.cassandraTable[RatingRaw]("movie", "ratings_by_movie").cache

    // Get some Random Movies and ask for a rating
    // Random Movies are generated before.
    val randomMovies = Random.shuffle(sc.textFile("moviesByTagCount").take(50).toList).slice(0, 10).map { x => (x.substring(1, x.indexOf(",")).toInt, x.substring(x.indexOf(",") + 1, x.length - 1)) }
    val myMovies = elicitateRatings(randomMovies)
    val myMoviesRDD = sc.parallelize(myMovies, 1)
    
    // Get / Compute the Model
    val model = getModel(sc, ratings, myMoviesRDD)
    
    // Candidates = All Movies - my recommend movies
    val myRatedMovieIds = myMovies.map(_.product).toSet
    val candidates = movies.keys.filter(!myRatedMovieIds.contains(_))
    
    // Get Recommendations
    val recommendations = model.predict(candidates.map((0, _))).collect.sortBy(-_.rating).take(50)
    
    // Print them
    val movieMap = movies.collect.toMap
    var i = 1
    println("Movies recommended for you:")
    recommendations.foreach { r =>
      println("%2d".format(i) + ": " + movieMap(r.product))
      i += 1
    }
	
	// if you want to do more stuff you should eventually clean up cached rdds with .unpersist(blocking = false)
  }

  /** Elicitate ratings from command-line. */
  def elicitateRatings(movies: Seq[(Int, String)]) = {
    val prompt = "Please rate the following movie (1-5 (best), or 0 if not seen):"
    println(prompt)
    val ratings = movies.flatMap { x =>
      var rating: Option[Rating] = None
      var valid = false
      while (!valid) {
        println(x._2 + ": ")
        try {
          val r = Console.readInt
          if (r < 0 || r > 5) {
            println(prompt)
          } else {
            valid = true
            if (r > 0) {
              rating = Some(Rating(0, x._1, r-2.5))
            }
          }
        } catch {
          case e: Exception => println(prompt)
        }
      }
      rating match {
        case Some(r) => Iterator(r)
        case None => Iterator.empty
      }
    }
    if (ratings.isEmpty) {
      error("No rating provided!")
    } else {
      ratings
    }
  }

  def getModel(sc: SparkContext, ratings: com.datastax.spark.connector.rdd.CassandraRDD[RatingRaw], userRatings:  org.apache.spark.rdd.RDD[Rating]): MatrixFactorizationModel = {
      println("generate new model")
      val ratingsForTraining = ratings.map { case RatingRaw(user, movie, rating, _) => Rating(user.toInt, movie.toInt, rating-2.5) }.union(userRatings)
      val rank = 10
      val numIterations = 20
      val model = ALS.trainImplicit(ratingsForTraining, rank, numIterations)
      return model
  }
  
}