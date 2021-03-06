#!/usr/bin/python
# Copyright 2017 Telstra Open Source
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

from kafka import KafkaProducer

bootstrap_servers = 'kafka.pendev:9092'
topic = 'kilda.topo.eng'

MT_SWITCH = "org.openkilda.messaging.info.event.SwitchInfoData"
MT_ISL = "org.openkilda.messaging.info.event.IslInfoData"
MT_INFO="org.openkilda.messaging.info.InfoMessage"


producer = KafkaProducer(bootstrap_servers=bootstrap_servers)


producer.send(topic, b'{"clazz": "%s", "timestamp": 23478952134, "destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "switch_id": "00:00:00:00:00:00:00:00", "state": "ADDED", "address":"00:00:00:00:00:00:00:00", "hostname":"hostname", "description":"description", "controller":"controller"}}' % (MT_INFO, MT_SWITCH))
producer.send(topic, b'{"clazz": "%s", "timestamp": 23478952134, "destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "switch_id": "00:00:00:00:00:00:00:01", "state": "ADDED", "address":"00:00:00:00:00:00:00:01", "hostname":"hostname", "description":"description", "controller":"controller"}}' % (MT_INFO, MT_SWITCH))

producer.flush()

i = 2

while i < 10000:
    producer.send(topic,
                  b'{"clazz": "%s", "timestamp": 23478952136, "destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "state":"DISCOVERED", "latency_ns": 1123, "speed":1000000, "available_bandwidth":1000000, "path": [{"switch_id": "00:00:00:00:00:00:00:00", "port_no": 1, "seq_id": "0", "segment_latency": 1123}, {"switch_id": "00:00:00:00:00:00:00:01", "port_no": 1, "seq_id": "1"}]}}' % (MT_INFO, MT_ISL))
    i += 1
    producer.send(topic, b'{"clazz": "%s", "timestamp": 23478952136, "destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "state": "DISCOVERED", "latency_ns": 1123, "speed":1000000, "available_bandwidth":1000000, "path": [{"switch_id": "00:00:00:00:00:00:00:01", "port_no": 1, "seq_id": "0", "segment_latency": 1123}, {"switch_id": "00:00:00:00:00:00:00:00", "port_no": 1, "seq_id": "1"}]}}' % (MT_INFO, MT_ISL))
    i += 1
    producer.send(topic, b'{"clazz": "%s", "timestamp": 23478952136, '
                         b'"destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "state": "FAILED", "path": [{"switch_id": "00:00:00:00:00:00:00:00", "port_no": 1}]}}' % (MT_INFO, MT_ISL))
    i += 1
    producer.send(topic, b'{"clazz": "%s", "timestamp": 23478952136, '
                         b'"destination":"TOPOLOGY_ENGINE", "payload": {"clazz": "%s", "state": "FAILED", "path": [{"switch_id": "00:00:00:00:00:00:00:01", "port_no": 1}]}}' % (MT_INFO, MT_ISL))
    i += 1
    print i

producer.flush()
