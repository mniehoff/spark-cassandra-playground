package de.codecentric.spark

import com.datastax.spark.connector._
import org.apache.spark._
import org.apache.spark.streaming._
import scala.util.Random
import org.apache.log4j.Logger
import org.apache.log4j.Level
import java.util.Date
import org.apache.spark.streaming._
import org.apache.spark.streaming.mqtt._
import org.apache.spark.storage.StorageLevel
import com.datastax.driver.core.utils.UUIDs

/**
 *
 */
object MqttStreaming {
  def main(args: Array[String]) {
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    val conf = new SparkConf().setAppName("MQT WordCount").set("spark.cassandra.connection.host", "127.0.0.1").setMaster("local[2]")
    val ssc = new StreamingContext(conf, Milliseconds(500))
    val brokerUrl = "tcp://localhost:1883"
    val topic = "foo"
    val lines = MQTTUtils.createStream(ssc, brokerUrl, topic, StorageLevel.MEMORY_ONLY_SER_2)
    val keyValue = lines.map(input => (input.split("\\|")(0), input.split("\\|")(1)))
    val data = keyValue.map(kv => (kv._1, UUIDs.timeBased(), kv._2)).cache()
    data.foreachRDD(_.saveToCassandra("mqtt", "sensors"))
    // Count of entries
    data.count().print();
    ssc.start()
    ssc.awaitTermination()
  }
}
