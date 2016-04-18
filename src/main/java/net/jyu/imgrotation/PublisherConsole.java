package net.jyu.imgrotation;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by jyu on 4/15/16.
 */
public class PublisherConsole {

    //TODO: user input to close connection in a graceful way
    public static void main(String[] args) throws IOException, TimeoutException {
        ClassLoader classLoader = PublisherConsole.class.getClassLoader();
        File originalFile = new File(classLoader.getResource("Whatnaught.png").getFile());
        Connection connection = ConnectionCreator.newConnection(ConnectionCreator.HOST_NAME);
        Channel channel = connection.createChannel();

        channel.queueDeclare(net.jyu.imgrotation.ConnectionCreator.ROTATE_QUEUE, false, false, false, null);

        Boolean keepgo = true;
        int idx = 1;
        while (keepgo) {
            JSONObject obj = new JSONObject();
            obj.put("warp_id", "abc000"+idx);
            obj.put("data_type", "image_plate");
            obj.put("data", Base64Util.pngToBase64String(originalFile));

            channel.basicPublish("", net.jyu.imgrotation.ConnectionCreator.NORMAL_ROTATING_KEY /* routing key*/,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    obj.toJSONString().getBytes("UTF-8"));

            System.out.println("abc000" + idx + " Sent to Queue");
            idx ++;
            System.out.println("type  \"Enter\" to continue publish, and type \"quit\" to quit ...");
            String read = System.console().readLine();
            keepgo = read.equalsIgnoreCase("quit") ? false : true;
        }

        channel.close();
        connection.close();

    }

}
