package commons

import java.util.Properties

import kafka.producer.{Producer, ProducerConfig}
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.Seconds
import org.apache.spark.streaming.kafka.{KafkaUtils, OffsetRange}

/**
  * Created on 21/04/16.
  *
  * @author Mauro Losciale and Pietro Tedeschi.
  */
object KafkaCommons {

  val kafkaTopics = "votes"    // command separated list of topics
  val kafkaBrokers = "localhost:9092"
  var offsetRanges = Array[OffsetRange]()
  val kafkaParams = Map[String, String]("metadata.broker.list" -> kafkaBrokers
  )
  val topicsSet = kafkaTopics.split(",").toSet
  val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
    SparkCommons.ssc, kafkaParams, topicsSet).window(Seconds(60), Seconds(30))

  val props:Properties = new Properties()
  props.put("metadata.broker.list", "localhost:9092")
  props.put("serializer.class", "kafka.serializer.StringEncoder")

  val config = new ProducerConfig(props)
  val producer = new Producer[String, String](config)

  def kafkaDirectStreaming() = {

    KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      SparkCommons.ssc, kafkaParams, topicsSet)
  }

}
