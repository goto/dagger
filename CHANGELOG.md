# Changelog


All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

### [Unreleased]

### Features

- **Multiple InfluxDB Database Support** — Route different measurements to different InfluxDB databases from a single Dagger job. New `getInfluxSink(databaseName, measurementName)` API on `SinkOrchestrator`. Configure multiple named databases via `SINK_INFLUX_DATABASES_CONFIG` JSON array. Fully backward compatible with existing single-database configuration.

### [v0.9.0](https://github.com/goto/dagger/releases/tag/v0.9.0) (2023-03-16)

### Features

- Add Influx Sink
- Add Kafka Sink
- Add Longbow
- Add Longbow+
- Add Elasticsearch Post Processor
- Add HTTP Post Processor
- Add Postgres Post Processor
- Add GRPC Post Processor
- Add support for Transformers
- Add support for Pre Processors
