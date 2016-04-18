package net.jyu.imgrotation;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by jyu on 4/15/16.
 */
public class ConnectionCreator {
    private static ConnectionFactory factory = new ConnectionFactory();

    public final static String HOST_NAME = "localhost";

    public final static String ROTATE_QUEUE = "rotate_queue";
    public final static String NORMAL_ROTATING_KEY = "rotate_queue";
    public static final String PROCESSED_ROTATES_KEY = "processed_rotates";
    public static final String PROCESSED_ROTATES_QUEUE = "processed_rotates";

    public static Connection newConnection(String hostName) throws IOException, TimeoutException {
        factory.setHost(hostName);
        Connection connection = factory.newConnection();
        return connection;
    }


}
