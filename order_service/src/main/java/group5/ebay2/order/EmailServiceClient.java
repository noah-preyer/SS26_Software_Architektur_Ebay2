package group5.ebay2.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EmailServiceClient {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceClient.class);

    private final MqttClient mqttClient;
    private final ObjectMapper objectMapper;
    private final UserServiceClient userServiceClient;

    private static final String EVENT_TOPIC = "order/complete";

    public EmailServiceClient(MqttClient mqttClient, ObjectMapper objectMapper, UserServiceClient userServiceClient) {
        this.mqttClient = mqttClient;
        this.objectMapper = objectMapper;
        this.userServiceClient = userServiceClient;
    }

    public void sendOrderComplete(Long userId, Order order) {
        try {
            UserDto user = userServiceClient.getUser(userId);
            if (user == null || user.email() == null) {
                log.warn("Could not find user or email for user {}", userId);
                return;
            }

            String products = order.getItems().stream()
                    .map(i -> "  \u2022 " + i.getProductTitle() + " (x" + i.getQuantity() + ") \u2014 "
                            + i.getPrice().multiply(java.math.BigDecimal.valueOf(i.getQuantity()))
                            + " " + order.getCurrency())
                    .collect(java.util.stream.Collectors.joining("\n"));

            publish(Map.of(
                    "email", user.email(),
                    "username", user.username(),
                    "orderId", order.getId().toString(),
                    "products", products,
                    "amount", order.getTotalAmount().toString(),
                    "currency", order.getCurrency()
            ));
            log.info("Published order/complete event for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to publish order/complete event for order {}: {}", order.getId(), e.getMessage());
        }
    }

    private void publish(Map<String, Object> data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            MqttMessage message = new MqttMessage(json.getBytes());
            message.setQos(1);
            mqttClient.publish(EVENT_TOPIC, message);
            log.info("Published event to MQTT topic {}", EVENT_TOPIC);
        } catch (Exception e) {
            log.error("Failed to publish to MQTT topic {}: {}", EVENT_TOPIC, e.getMessage());
        }
    }
}
