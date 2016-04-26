package cep

import ml.CollaborativeFiltering
import org.apache.spark.mllib.recommendation.Rating
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.DataFrame

/**
  * Created by makaveli on 23/04/16.
  *
  * @author Mauro Losciale and Pietro Tedeschi.
  */
object CEP {

  def hasEnoughVotes(query: DataFrame, table: DataFrame, ratings: RDD[(Long,Rating)], movies: Map[Int, String]) ={

    query.count() match {

      case c if c == 0 => println("No valid vote")
      case c if c > 3 =>
        val votesRating = table.map(v => Rating(0, v(0).toString.toInt, v(1).toString.toDouble))
        println("Make CF")
        CollaborativeFiltering.userCF(votesRating, ratings, movies)
      case c if c <= 3 => println("Valid votes less then 4")
    }

  }

}
