## Pure Movies
**Cache Movies for further requests**

```scala
    case class Movie(movieid:Long,genres:Set[String],title:String,year:Int)
    val movies = sc.cassandraTable[Movie]("movie","movies").cache
```
**Movies in DB**
```scala
    movies.count 
    => 27286
```
**Movies in 2000**
```scala
    movies.filter{case Movie(i,g,t,y) => y == 2000}.count
```
**Count Movies containing "The" in title**
```scala
    movies.filter{case Movie(i,g,t,y) => t.contains("The")}.count
```
**Count Movies in Genre "Drama"**
```scala
    movies.filter{case Movie(i,g,t,y) => g.contains("Drama")}
```

**Movies in the 80ies**

Caching for further requests
```scala
    val in80 = movies.filter{case Movie(i,g,t,y) => y >= 1980 && y <= 1989}.cache
    val in80count = in80.count
```
**Percentage on 80's movies on all movies**
```scala
    in80count / movies.count.toFloat
```
**Percentage for every year in the 80s**
```scala
    val in80byYear = in80.map{case Movie(i,g,t,y) => (y,1)}.reduceByKey(_+_).sortByKey()
    val percentIn80ByYear = in80byYear.mapValues(v => "%.2f%%".format(100*v/in80count.toDouble))
    percentIn80ByYear.collect.foreach(println)
```
**Min/Max/Average Year**
```scala
    val years = sc.cassandraTable[Int]("movie","movies").select("year").cache
    years.min
    years.max
    years.reduce((a,b) => a+b) / years.count.toFloat
```
## User

**User with most Tags**
```scala
    case class Tag(userid:Long,movieid:Long,tag:String,time:java.util.Date)
    val tagsByUser = sc.cassandraTable[Tag]("movie","tags_by_user").cache
    tagsByUser.map{case Tag(u,m,tag,time) => (u,1)}.reduceByKey(_ + _).sortBy(-_._2).take(1)
    => user: 185721, count: 20356
```
**User with most Ratings - Alternative 1**
```scala
    case class Rating (userid:Long, movieid:Long, rating:Double, time: java.util.Date)
    val ratingsByUser = sc.cassandraTable[Rating]("movie","ratings_by_user").cache
    ratingsByUser.map{case Rating(u,m,r,t) => (u,1)}.reduceByKey(_+_).sortBy(-_._2).take(1)
    => user: 69535, count: 9254
```
**User with most Ratings - Alternative 2**
```scala
    sc.cassandraTable[(Long,Long)]("movie","ratings_by_user").select("userid","movieid").map{t => (t._1,1)}.reduceByKey(_+_).sortBy(-_._2).take(1)
```
**User with most Ratings - Alternative 3**
```scala
    sc.cassandraTable[(Long,Long)]("movie","ratings_by_user").select("userid","movieid").countByKey().toSeq.sortBy(-_._2).take(1)
```
## Movies with Ratings / Tags

**Movies tagged with "Oscar (Best Picture)"**
```scala
    val tagsByMovie = sc.cassandraTable[Tag]("movie","tags_by_movie").cache
    val movieIdsWithTag = tagsByMovie.filter{case Tag(u,m,tag,time) => tag == "Oscar (Best Picture)"}.map{case Tag(u,m,tag,time) => (m,tag)}
    val movieIdsWithTitle = movies.map{case Movie(m,g,t,y) => (m,t)}
    movieIdsWithTag.join(movieIdsWithTitle).map(e => e._2._2).distinct.collect.foreach(println)
```
**All tags for Movie „Lord of the Rings: The Two Towers, The"**
```scala
    val movieId = movies.filter{case Movie(i,g,t,y) => t == "Lord of the Rings: The Two Towers, The"}.map{case Movie(i,g,t,y) => i}.first
    val tags = sc.cassandraTable[String]("movie","tags_by_movie").select("tag").where("movieId = " +movieId)
```
**Movies Without Tag**
```scala
    val movieIdsWithTitle = movies.map{case Movie(m,g,t,y) => (m,t)} // 27286
    val movieIdsWithTag = sc.cassandraTable[Long]("movie","tags_by_movie").select("movieid").map(i => (i,1)).distinct() // 19553
    val movieTitlesWithoutTag = movieIdsWithTitle.subtractByKey(movieIdsWithTag).map(e => e._2) // 7738
```
**Movie with best rating**
```scala
    val ratingsByMovie = sc.cassandraTable[Rating]("movie","ratings_by_movie").cache
    ratingsByMovie.map{case Rating(u,m,r,t) => (m,r)}.mapValues(r => (r,1)).reduceByKey((x,y) => (x._1 + y._1,x._2 + y._2)).map{case (k,v) => (k, v._1 / v._2.toFloat)}.sortBy(-_._2)
```
a lot of movies rated 5.0, new condition: at least 50 votes!
```scala
    val movieIdsWithRating = ratingsByMovie.map{case Rating(u,m,r,t) => (m,r)}.mapValues(r => (r,1)).reduceByKey((x,y) => (x._1 + y._1,x._2 + y._2)).filter{case (k,v) => v._2 >= 50}.map{case (k,v) => (k, v._1 / v._2.toFloat)} // .sortBy(-_._2)
    val movieIdsWithTitle = movies.map{case Movie(m,g,t,y) => (m,t)}
    val highestMovies = movieIdsWithRating.join(movieIdsWithTitle).map{case (i,(r,t)) => (t,r)}.sortBy(-_._2)
    highestMovies.mapValues(v => "%.2f".format(v)).collect.foreach(println)
```
**Movie with worst rating**

see above, just change the sortBy

**Movie with most tags**
```scala
    val movieIdsWithTagCount = tagsByMovie.map{case Tag(u,m,tag,time) => (m,1)}.reduceByKey(_+_)
    val movieIdsWithTitle = movies.map{case Movie(m,g,t,y) => (m,t)}
    movieIdsWithTagCount.join(movieIdsWithTitle).map{case (i,(c,t)) => (t,c)}.sortBy(-_._2)
```

### User, Tags, Ratings, Movie

**titles and year for all movies rated by User `65157`**
```scala
    val ratingsByUser = sc.cassandraTable[Long]("movie","ratings_by_user").where("userid=65157").select("movieid").map(v => (v,1))
    val movieIdsWithTitle = movies.map{case Movie(m,g,t,y) => (m,(t,y))}
    ratingsByUser.join(movieIdsWithTitle).map{case (m,(r,(t,y))) => (t,y)}
```
**Average count of ratings per User**
```scala
    val ratingsOnlyUser = sc.cassandraTable[Long]("movie","ratings_by_user").select("userid").cache
    val userCount = ratingsOnlyUser.distinct().count
    val ratingCount ratingsOnlyUser.count // 21063128
    val ratingPerUser = ratingCount / userCount.toFloat
```
**Average count of tags per User**
```scala
    val tagsOnlyUser = sc.cassandraTable[Long]("movie","tags_by_user").select("userid").cache
    val userCount = tagsOnlyUser.distinct().count
    val tagCount tagsOnlyUser.count // 470508
    val tagPerUser = tagCount / userCount.toFloat
```
**Average rating of all users on all movies**
```scala
    val allRatings = sc.cassandraTable[Long]("movie","ratings_by_movie").select("rating")
    allRatings.reduce(_+_) / allRatings.count.toFloat // allRatings.count => 21063128
```
**Count of active Users (activ: at least 1 rating and 1 tag)**
```scala
    val ratingsOnlyUser = sc.cassandraTable[Long]("movie","ratings_by_user").select("userid").cache
    val tagsOnlyUser = sc.cassandraTable[Long]("movie","tags_by_user").select("userid").cache
    val count = tagsOnlyUser.collect.toSet.intersect(ratingsOnlyUser.collect.toSet).size
```