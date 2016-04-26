package ml

import commons.{KafkaCommons, SparkCommons}
import kafka.producer.{KeyedMessage, Producer}
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

/**
  * Created on 08/04/16.
  *
  * @author Mauro Losciale and Pietro Tedeschi.
  */
object CollaborativeFiltering {

  case class Vote(movieid: Int, rating: Double)

    def userCF(rdd: RDD[Rating], ratings: RDD[(Long, Rating)], movies: Map[Int, String]) = {


      val myRatingsRDD = rdd

      // split ratings into train (60%), validation (20%), and test (20%) based on the
      // last digit of the timestamp, add myRatings to train, and cache them

      val numPartitions = 4

      val training = ratings.filter(x => x._1 < 6)
        .values
        .union(myRatingsRDD)
        .repartition(numPartitions)
        .cache()

      val validation = ratings.filter(x => x._1 >= 6 && x._1 < 8)
        .values
        .repartition(numPartitions)
        .cache()

      val test = ratings.filter(x => x._1 >= 8).values.cache()

      val numTraining = training.count()
      val numValidation = validation.count()
      val numTest = test.count()

      println("Training: " + numTraining + ", validation: " + numValidation + ", test: " + numTest)

      // train models and evaluate them on the validation set

      val ranks = List(8, 12)
      val lambdas = List(0.1, 10.0)
      val numIters = List(10, 20)
      var bestModel: Option[MatrixFactorizationModel] = None
      var bestValidationRmse = Double.MaxValue
      var bestRank = 0
      var bestLambda = -1.0
      var bestNumIter = -1
      for (rank <- ranks; lambda <- lambdas; numIter <- numIters) {
        val model = ALS.train(training, rank, numIter, lambda)
        val validationRmse = computeRmse(model, validation, numValidation)
        println("RMSE (validation) = " + validationRmse + " for the model trained with rank = "
          + rank + ", lambda = " + lambda + ", and numIter = " + numIter + ".")
        if (validationRmse < bestValidationRmse) {
          bestModel = Some(model)
          bestValidationRmse = validationRmse
          bestRank = rank
          bestLambda = lambda
          bestNumIter = numIter
        }
      }

      // evaluate the best model on the test set

      val testRmse = computeRmse(bestModel.get, test, numTest)

      println("The best model was trained with rank = " + bestRank + " and lambda = " + bestLambda
        + ", and numIter = " + bestNumIter + ", and its RMSE on the test set is " + testRmse + ".")

      // make personalized recommendations

      val myRatedMovieIds = myRatingsRDD.map(_.product).collect().toSet
      val candidates = SparkCommons.sc.parallelize(movies.keys.filter(!myRatedMovieIds.contains(_)).toSeq)
      val recommendations = bestModel.get
        .predict(candidates.map((0, _)))
        .collect()
        .sortBy(-_.rating)
        .take(9)

      out(recommendations, movies, KafkaCommons.producer)

    }

    /** Compute RMSE (Root Mean Squared Error). */
    def computeRmse(model: MatrixFactorizationModel, data: RDD[Rating], n: Long): Double = {
      val predictions: RDD[Rating] = model.predict(data.map(x => (x.user, x.product)))
      val predictionsAndRatings = predictions.map(x => ((x.user, x.product), x.rating))
        .join(data.map(x => ((x.user, x.product), x.rating)))
        .values
      math.sqrt(predictionsAndRatings.map(x => (x._1 - x._2) * (x._1 - x._2)).reduce(_ + _) / n)
    }

    /** Write recommendations array to a JSON file **/
    def out(data: Array[Rating], film: Map[Int, String], producer: Producer[String, String]) = {

      println("Movies recommended for you:")

      data.foreach { r =>

        val jsonVote = ("movieid", r.product) ~("rating", r.rating.toInt) ~("title", film(r.product))
        producer.send(new KeyedMessage[String, String]("result", compact(render(jsonVote)).toString))
      }
    }
}
