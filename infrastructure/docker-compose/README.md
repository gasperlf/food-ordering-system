### How to run Kafka files cluster
````sh
docker-compose -f common.yml -f zookeeper.yml up
````

to check zookeeper
````sh
echo rouk | nc localhost 2181
````
response from zookeeper
````
imok
````

Run kafka
````shell
docker-compose -f common.yml -f kafka_cluster.yml up
````

Run init Kafka to create topics
````shell
docker-compose -f common.yml -f init_kafka.yml up

````

Run in a browser
````broswer
localhost:9000
````
You need to add the cluster manually

name: food-ordering-system-cluster
cluster zookeeper: zookeeper:2181

and save.





