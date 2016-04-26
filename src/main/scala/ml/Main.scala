package ml

import cep.CEP
import commons.{FileParser, KafkaCommons, SparkCommons}
import org.apache.log4j.{Level, Logger}

/**
  * Created by makaveli on 21/04/16.
  *
  * @author Mauro Losciale and Pietro Tedeschi.
  */
object Main {

  def main(args: Array[String]): Unit ={

    Logger.getLogger("org.apache.spark").setLevel(Level.ERROR)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)

    val ratings = FileParser.parseRatings()
    val movies = FileParser.parseMovies()

    val numRatings = ratings.count()
    val numUsers = ratings.map(_._2.user).distinct().count()
    val numMovies = ratings.map(_._2.product).distinct().count()

    println("Got " + numRatings + " ratings from "
      + numUsers + " users on " + numMovies + " movies.")

    val messages = KafkaCommons.kafkaDirectStreaming()

    messages.foreachRDD { rdd =>
      val votes = rdd.map(_._2)
      val tableVotes = SparkCommons.sqlContext.read.option("header","true").option("inferSchema","true").json(votes).toDF().na.drop()

      if (tableVotes.count() > 0) {

        tableVotes.show()
        tableVotes.registerTempTable("tableVotes")

        val query = SparkCommons.sqlContext.sql("SELECT * FROM tableVotes WHERE rating > 3")
        CEP.hasEnoughVotes(query, tableVotes, ratings, movies)

      } else println("No vote received")

    }

    SparkCommons.ssc.start()
    SparkCommons.ssc.awaitTermination()

  }

}
