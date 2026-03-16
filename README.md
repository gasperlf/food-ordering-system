# Getting Started

![project-overview-section-1.png](docs/project-overview-section-1.png)

### Reference Documentation

to generate graphic path of dependencies:

```shell
brew install graphviz
mvn com.github.ferstl:depgraph-maven-plugin::aggregate -DcreateImage=true -DreduceEdges=false -Dscope=compile "-Dincludes=com.food.ordering.system*"
```

# Services

## Order service

### dependencies
![dependency-graph_0.png](order-service/docs/dependency-graph_0.png)

after add common module

![dependency-graph_1.png](order-service/docs/dependency-graph_1.png)


![dependency-graph_2.png](order-service/docs/dependency-graph_2.png)

### High level design
![order-service-hexagonal-section-2-share.png](order-service/docs/order-service-hexagonal-section-2-share.png)

### Low level design
![order-service-domain-logic-oncourse.png](order-service/docs/order-service-domain-logic-oncourse.png)

### order status transitions
![order-state-transitions.png](order-service/docs/order-state-transitions.png)


![order-request-simple-flow.png](order-service/docs/order-request-simple-flow.png)

## Customer service

## Payment service

## Restaurant service

## Kafka

#### Zookeeper
```shell
docker compose -f common.yaml -f zookeeper.yaml up -d
-- check status zookeeper

echo ruok | nc localhost 2181
-- return iamok
```

#### Kafka
```shell
docker compose -f common.yaml -f kafka_cluster.yaml up -d
```

#### Init Kafka
```shell
docker compose -f common.yaml -f init_kafka.yaml up -d
```

browser localhost:9000 abd add cluster
name: food-ordering-system
zookeeper hot: zookeeper:2181

## Outbox Pattern

![outbox.png](docs/outbox.png)

![outbox-happy-flow.png](docs/outbox-happy-flow.png)

![outbox-approval-failure.png](docs/outbox-approval-failure.png)

![outbox-payment-failure.png](docs/outbox-payment-failure.png)
