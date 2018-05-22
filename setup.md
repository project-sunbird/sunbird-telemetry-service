## Pre-requisites
## Configuration
1. Environment Variabls
    1. ekstep_telemetry_auth : authorization header value
    2. ekstep_telemetry_api_base_url : telemetry service base url "https://qa.ekstep.in/api"
	3. ekstep_telemetry_storage_toggle : possible values are on or off . default value is on. on indicate data will sent to Ekstep.
	4. sunbird_telemetry_dispatchers : possible value as comma separated String (Ex: kafka) currently only "kafka" value is supported
    5. sunbird_telemetry_kafka_servers_config : Comma-separated list of host and port pairs EX: localhost:9092,localhost:9093,localhost:9094
    6. sunbird_telemetry_kafka_topic :  String topic name for kakfa client.	First topic need to be created under kafka and then put the same topic name here.
	     if point 4 is not kafka then 5 and 6 are not needed.

## Build
1. Run "mvn clean install" from "sunbird-telemetry-service/service" to build the services.
2. Go to "sunbird-telemetry-service/service" and run the command "mvn play2:dist" to generate the dist file for services.
3. The build file "telemetry-service-1.0-SNAPSHOT-dist.zip" is generated in "sunbird-telemetry-service/service/target" folder.

## Run
1. Unzip the dist file "telemetry-service-1.0-SNAPSHOT-dist.zip".
2. Run the command "java -cp 'telemetry-service-1.0-SNAPSHOT/lib/*' play.core.server.ProdServerStart telemetry-service-1.0-SNAPSHOT" to start the service.