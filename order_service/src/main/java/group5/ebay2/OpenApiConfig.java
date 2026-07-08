package group5.ebay2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service API")
                        .description("Order management and status tracking")
                        .version("1.0.0"));
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder().build();
    }

    @Bean
    public MqttClient mqttClient() throws Exception {
        String broker = System.getenv().getOrDefault("MQTT_BROKER", "mqtt-broker");
        int port = Integer.parseInt(System.getenv().getOrDefault("MQTT_PORT", "1883"));
        String serverUri = "tcp://" + broker + ":" + port;
        MqttClient client = new MqttClient(serverUri, "order-service", new MemoryPersistence());
        MqttConnectOptions opts = new MqttConnectOptions();
        opts.setAutomaticReconnect(true);
        opts.setCleanSession(true);
        client.connect(opts);
        return client;
    }
}
