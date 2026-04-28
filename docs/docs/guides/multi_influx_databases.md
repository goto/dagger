# Multiple InfluxDB Databases

This guide explains how to configure a Dagger job to write metrics to **multiple InfluxDB databases** from a single job.

## Use Case

In a real-time metrics job, different metrics may have vastly different throughput levels. For example, live status metrics may generate millions of points per minute, while daily statistics aggregate to a few thousand. Writing all metrics to a single InfluxDB database can cause storage and performance issues.

With multi-database support, you can route high-throughput metrics to a dedicated database and low-throughput metrics to a shared database, each with its own connection settings and retention policies.

## Configuration

### Step 1: Define Named Databases

Set the `SINK_INFLUX_DATABASES_CONFIG` environment variable to a JSON array of database configurations:

```json
[
  {
    "name": "high-throughput-db",
    "url": "http://influx-ht:8086",
    "username": "admin",
    "password": "secret",
    "dbName": "metrics_live",
    "retentionPolicy": "autogen",
    "batchSize": 2000,
    "flushDurationMs": 500
  },
  {
    "name": "low-throughput-db",
    "url": "http://influx-lt:8086",
    "username": "admin",
    "password": "secret",
    "dbName": "metrics_daily",
    "retentionPolicy": "autogen",
    "batchSize": 500,
    "flushDurationMs": 1000
  }
]
```

Each entry has the following fields:

| Field | Required | Default | Description |
|-------|----------|---------|-------------|
| `name` | Yes | - | Logical identifier used in the API |
| `url` | Yes | - | InfluxDB HTTP endpoint |
| `dbName` | Yes | - | InfluxDB database name |
| `username` | No | `""` | Authentication username |
| `password` | No | `""` | Authentication password |
| `retentionPolicy` | No | `"autogen"` | InfluxDB retention policy |
| `batchSize` | No | `100` | Number of points per batch write |
| `flushDurationMs` | No | `1000` | Flush interval in milliseconds |

### Step 2: Map Measurements to Databases (Custom Jobs)

For custom job builders like `DriversMetricJobBuilder`, set `SINK_INFLUX_DB_NAMES_LIST` as a comma-separated list of database names that map 1:1 to `SINK_INFLUX_MEASUREMENTS_LIST`:

```
SINK_INFLUX_MEASUREMENTS_LIST=rt-supply-lstatus-01,rt-supply-lpref-01,rt-supply-dstats-01,rt-supply-hmap-01
SINK_INFLUX_DB_NAMES_LIST=high-throughput-db,high-throughput-db,low-throughput-db,high-throughput-db
```

This routes `rt-supply-dstats-01` to `low-throughput-db` while all live metrics go to `high-throughput-db`.

## Developer API

Use the `getInfluxSink` method on `SinkOrchestrator` to target a specific database:

```java
SinkOrchestrator sinkOrchestrator = new SinkOrchestrator(telemetryExporter);
sinkOrchestrator.initInfluxConfig(configuration);

// Route to a specific database
dataStream.sinkTo(sinkOrchestrator.getInfluxSink(
    configuration, columnNames, "high-throughput-db", "measurement-name"));
```

The existing `getSink()` method continues to work unchanged for backward compatibility.

## Backward Compatibility

If `SINK_INFLUX_DATABASES_CONFIG` is **not set**, the system automatically falls back to the legacy flat configuration keys:

- `SINK_INFLUX_URL`
- `SINK_INFLUX_USERNAME`
- `SINK_INFLUX_PASSWORD`
- `SINK_INFLUX_DB_NAME`
- `SINK_INFLUX_RETENTION_POLICY`
- `SINK_INFLUX_BATCH_SIZE`
- `SINK_INFLUX_FLUSH_DURATION_MS`

These are used to create a single database entry named `"default"`. Existing deployments require no configuration changes.
