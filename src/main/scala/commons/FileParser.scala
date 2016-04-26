package commons

import java.io.File

import org.apache.spark.mllib.recommendation.Rating

/**
  * Created on 21/04/16.
  *
  * @author Mauro Losciale and Pietro Tedeschi.
  */
object FileParser {

  def parseRatings () = {

    SparkCommons.sc.textFile(new File("ratings.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (timestamp % 10, Rating(userId, movieId, rating))
      (fields(3).toLong % 10, Rating(fields(0).toInt, fields(1).toInt, fields(2).toDouble))
    }
  }

  def parseMovies () = {

    SparkCommons.sc.textFile(new File("movies.dat").toString).map { line =>
      val fields = line.split("::")
      // format: (movieId, movieName)
      (fields(0).toInt, fields(1))
    }.collect().toMap
  }

}
