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
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs

/**
 *
 */
object SimpleAnalysis {
  case class MeasureDB(sensorid: String, time: UUID, temperature: Double)
  case class Measure(sensorid: String, time: Date, temperature: Double)

  def main(args: Array[String]) {

    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("com.datastax").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)
    val conf = new SparkConf().setAppName("MQT WordCount").set("spark.cassandra.connection.host", "127.0.0.1").setMaster("local[2]")
    val sc = new SparkContext(conf)
    val data = sc.cassandraTable[MeasureDB]("mqtt", "sensors");
    val dataBySensor = data.map(m => (m.sensorid, (m.temperature, 1)))
    val averages = dataBySensor.reduceByKey((x, y) => (x._1 + y._1, x._2 + y._2)).mapValues { case (sum, count) => Math.round(100 * sum / count) / 100d }
    averages.collect().foreach(println)

    //    val dataWithDate = data.map(m => Measure(m.sensorid, new Date(UUIDs.unixTimestamp(m.time)), m.temperature));
    // Wait for input before exiting
    System.in.read()
  }
}
