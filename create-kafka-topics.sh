#!/bin/bash

KAFKA_BROKER="localhost:9092"

# Service codes for microservices
declare -a SERVICE_CODES=("SPK" "RFM" "TRM" "ENV" "WM" "PAT" "PRP" "STR" "WEM" "GDD" "MTU" "AGD" "AEP")

echo "========================================="
echo "Creating Kafka Topics for Claims System"
echo "========================================="

# Create service-specific topics (13 topics)
echo ""
echo "Creating service-specific topics..."
for CODE in "${SERVICE_CODES[@]}"; do
  TOPIC="claims.$CODE"
  echo "  → $TOPIC"
  kafka-topics.sh --create \
    --bootstrap-server $KAFKA_BROKER \
    --topic $TOPIC \
    --partitions 3 \
    --replication-factor 1 \
    --if-not-exists
done

# Create response topic
echo ""
echo "Creating response topics..."
echo "  → claims.responses"
kafka-topics.sh --create \
  --bootstrap-server $KAFKA_BROKER \
  --topic claims.responses \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# Create status updates topic
echo "  → claims.status-updates"
kafka-topics.sh --create \
  --bootstrap-server $KAFKA_BROKER \
  --topic claims.status-updates \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

# Create dead letter topic
echo "  → claims.dead-letter"
kafka-topics.sh --create \
  --bootstrap-server $KAFKA_BROKER \
  --topic claims.dead-letter \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

# List all topics
echo ""
echo "========================================="
echo "Topics created successfully!"
echo "========================================="
echo ""
echo "All claims.* topics:"
kafka-topics.sh --list --bootstrap-server $KAFKA_BROKER | grep "^claims\."

echo ""
echo "Total topics: 16 (13 service topics + 3 system topics)"
echo "Done!"
