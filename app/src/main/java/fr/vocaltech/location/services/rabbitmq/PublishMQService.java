package fr.vocaltech.location.services.rabbitmq;

import android.util.Log;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import io.github.cdimascio.dotenv.Dotenv;

public class PublishMQService {
    private final static String TAG = "PublishMQService";
    private final ConnectionFactory _connectionFactory;

    public PublishMQService() {
        Dotenv dotenv = Dotenv.configure()
                .directory("/assets")
                .filename("env")
                .load();

        _connectionFactory = new ConnectionFactory();
        _connectionFactory.setHost(dotenv.get("RABBITMQ_HOSTNAME"));
        _connectionFactory.setUsername(dotenv.get("RABBITMQ_USERNAME"));
        _connectionFactory.setPassword(dotenv.get("RABBITMQ_PASSWORD"));

    }
    public void publishToQueue(String queueName, String message) {
        try (Connection connection = _connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicPublish("", queueName, null, message.getBytes());
            Log.d(TAG, "[publishToQueue()] queueName: " + queueName + " - msg: " + message);
        } catch (TimeoutException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
