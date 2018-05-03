## Pre-requisites
## Configuration
1. Environment Variabls
    1. ekstep_telemetry_auth : authorization header value
    2. ekstep_telemetry_api_base_url : telemetry service base url "https://qa.ekstep.in/api"

## Build
1. Run "mvn clean install" from "sunbird-mw/services" to build the services.
2. Go to "sunbird-mw/services/telemetry-service" and run the command "mvn play2:dist" to generate the dist file for services.
3. The build file "telemetry-service-1.0-SNAPSHOT-dist.zip" is generated in "sunbird-mw/services/telemetry-service/target" folder.

## Run
1. Unzip the dist file "telemetry-service-1.0-SNAPSHOT-dist.zip".
2. Run the command "java -cp 'telemetry-service-1.0-SNAPSHOT/lib/*' play.core.server.ProdServerStart telemetry-service-1.0-SNAPSHOT" to start the service.
