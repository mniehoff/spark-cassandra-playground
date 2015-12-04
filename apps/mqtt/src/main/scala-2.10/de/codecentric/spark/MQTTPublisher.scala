package de.codecentric.spark

import org.eclipse.paho.client.mqttv3._
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import scala.util.Random
import java.text.DecimalFormat
import java.util.UUID

/**
 *
 */
object MQTTPublisher {
  def main(args: Array[String]) {
    var client: MqttClient = null
    try {
      val persistence = new MemoryPersistence()
      client = new MqttClient("tcp://localhost:1883", MqttClient.generateClientId(), persistence)
      client.connect()
      val uuids = new Array[UUID](10);
      for (i <- 0 to 9) {
        uuids(i) = UUID.randomUUID()
      }
      val msgtopic = client.getTopic("foo")
      while (true) {
        try {
          for (i <- 0 to 9) {
            val msgContent = uuids(i) + "|" + Math.round((20 + (Random.nextDouble() * 4 - 2)) * 100) / 100d;
            val message = new MqttMessage(msgContent.getBytes("utf-8"))
            msgtopic.publish(message)
            println(s"Published data. topic: ${msgtopic.getName()}; Message: $message")
          }
          Thread.sleep(500)
        } catch {
          case e: MqttException if e.getReasonCode == MqttException.REASON_CODE_MAX_INFLIGHT =>
            Thread.sleep(10)
            println("Queue is full, wait for to consume data from the message queue")
        }
      }
    } catch {
      case e: MqttException => println("Exception Caught: " + e)
    } finally {
      if (client != null) {
        client.disconnect()
      }
    }
  }
}
