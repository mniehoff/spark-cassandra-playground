Simple Example with Streaming MQTT Data.

Process a stream of MQTT Messages and save them to Cassandra

Install & start mosquitto mqtt broker. I.e with brew on OSX
```
brew install mosquitto
/usr/local/Cellar/mosquitto/<version>/sbin/mosquitto
```

Create Keyspace and table in Cassandra

```
CREATE KEYSPACE mqtt WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
CREATE TABLE mqtt.sensors(sensorId uuid, time timeuuid, temperature double, primary key(sensorId,time));
```

- MQTTPublisher:  produces Messages
- MqttStreaming:  Spark Streaming App. Process the messages and store them to Cassandra
- SimpleAnalysis: Spark Batch App. Does a simple analysis on the stored data
