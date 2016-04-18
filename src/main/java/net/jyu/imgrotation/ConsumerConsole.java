package net.jyu.imgrotation;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by jyu on 4/15/16.
 */
public class ConsumerConsole {
    static Integer S3_RETRY_LIMIT = 3;

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connectionConsumer = ConnectionCreator.newConnection(ConnectionCreator.HOST_NAME);
        Connection connectionPublisher = ConnectionCreator.newConnection(ConnectionCreator.HOST_NAME);
        Channel consumerChannel = connectionConsumer.createChannel();
        Channel publisherChannel = connectionPublisher.createChannel();


        consumerChannel.queueDeclare(net.jyu.imgrotation.ConnectionCreator.ROTATE_QUEUE, false, false, false, null);
        /*
        Note: register with direct exchange to consumer only key specific from Q, for user guest, below approch not granted
        channel.queueBind(ChannelCreator.ROTATE_QUEUE, ChannelCreator.EXCHANGE_NAME, ChannelCreator.NORMAL_ROTATING_KEY);
        */
        System.out.println(" [*] Waiting for messages ...");

        Consumer consumer = new PngRotateConsumer(consumerChannel, publisherChannel);

        //jyu: autoAck should turn false and let consumer declaratively ping back processing successful
        String tag =  consumerChannel.basicConsume(net.jyu.imgrotation.ConnectionCreator.ROTATE_QUEUE, false, consumer);

        Boolean keepgo = true;
        while (keepgo) {
            System.out.println("type  \"Enter\" to continue publish, and type \"quit\" to quit ...");
            String read = System.console().readLine();
            keepgo = read.equalsIgnoreCase("quit") ? false : true;
        }
        consumerChannel.basicCancel(tag);
        consumerChannel.close();
        publisherChannel.close();
        connectionConsumer.close();
        connectionPublisher.close();
    }


}
