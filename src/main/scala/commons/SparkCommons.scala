package commons

import org.apache.spark.sql.SQLContext
import org.apache.spark.streaming._
import org.apache.spark.{SparkConf, SparkContext}

/**
 * Handles configuration, context and so
 *
 * @author Mauro Losciale and Pietro Tedeschi.
 */
object SparkCommons {
  //build the SparkConf  object at once
  lazy val conf = {
    new SparkConf(false)
      .setMaster("local[*]")
      //.setMaster("spark://192.168.1.32:7077")
      .setAppName("Collaborative Filtering with Kafka")
      .set("spark.logConf", "true")
  }

  lazy val sc = SparkContext.getOrCreate(conf)
  lazy val ssc = new StreamingContext(sc, Seconds(30))
  lazy val sqlContext = new SQLContext(sc)

  sc.addJar("target/scala-2.10/mlspark-assembly-1.0.jar")
}
