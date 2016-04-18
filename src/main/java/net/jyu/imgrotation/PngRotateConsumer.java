package net.jyu.imgrotation;

import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

/**
 * Created by jyu on 4/16/16.
 */
public class PngRotateConsumer extends DefaultConsumer {
    static final Logger logger = Logger.getLogger(PngRotateConsumer.class.getName());

    private Channel consumerChannel;
    private Channel publisherChannel;

    public PngRotateConsumer(Channel consumerChannel, Channel publisherChannel) {
        super(consumerChannel);
        this.consumerChannel = consumerChannel;
        this.publisherChannel = publisherChannel;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String message = new String(body, "UTF-8");

        JSONParser parser = new JSONParser();
        try {
            Object ob = parser.parse(message);
            JSONObject jsonObject = (JSONObject) ob;
            String dataStr = (String)jsonObject.get("data");
            String wrapid = (String) jsonObject.get("warp_id");

            decodeAndrotateImage(dataStr);

            // only on successfully decodeAndrotateImage, will ack back Q to remove the msg
            System.out.println(" [x] Done, declarative announce back acknowledge");
            consumerChannel.basicAck(envelope.getDeliveryTag(), false);

            JSONObject s3infoObj = new JSONObject();
            s3infoObj.put("bucket", "transcriptic-interview");
            s3infoObj.put("key", ConnectionCreator.PROCESSED_ROTATES_KEY);

            JSONObject obj = new JSONObject();
            obj.put("warp_id", wrapid);
            obj.put("data_type", "image_plate");
            obj.put("s3_info", "image_plate");
            obj.put("bucket", s3infoObj);

            publisherChannel.queueDeclare(ConnectionCreator.PROCESSED_ROTATES_QUEUE, false, false, false, null);
            publisherChannel.basicPublish("", ConnectionCreator.PROCESSED_ROTATES_KEY /* routing key*/,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    obj.toJSONString().getBytes("UTF-8"));
        } catch (ParseException e) {
            logger.severe(e.getMessage());
        }
    }


    public static void decodeAndrotateImage(String encodedStr) throws IOException {
        BufferedImage sourceBI = Base64Util.base64StringToImg(encodedStr);

        Graphics2D g = (Graphics2D) sourceBI.getGraphics();

        AffineTransform at = new AffineTransform();

        at.rotate(180.0 * Math.PI / 180.0, sourceBI.getWidth() / 2.0, sourceBI
                .getHeight() / 2.0);

        BufferedImageOp bio;
        bio = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

        BufferedImage destinationBI = bio.filter(sourceBI, null);

        File processedFile = new File("/Users/jyu/Downloads/consumer_rotated.png");

        ImageIO.write(destinationBI, "png", processedFile);

        /*
        Goal: To avoid Blocking the queue by not acknowledge back, having worker/s keep working on the same failed message

         Note: To handle failed upload, we try to
        A. Retry 3 times to handle ad hoc fail.
        B. Further option after retry would be :
            B-1 : resubmitt the message with processed png data to another Q for later processing. This solution has the
                  risk of reach uplimit while too many messages can not be process successfully
            B-2 : Persist the failed message to DB. (favor this option)
        */
        int retryCount = 3;
        while (retryCount > 0) {
            try {
                S3UploadWorker.getInstance().upload2DefaultBucket(processedFile, processedFile.getName());
                break;
            } catch (UploadFailException e) {
                retryCount --;
                if(retryCount==0){
                    dealWithFailedUploading(processedFile);
                }
            }
        }



    }

    /**
     * To avoid Blocking the queue by not acknowledge back, having worker/s keep working on the same failed message

     To handle failed upload, we try to
     A. Retry 3 times to handle ad hoc fail.
     B. Further option after retry would be :
     B-1 : resubmitt the message with processed png data to another Q for later processing. This solution has the
     risk of reach uplimit while too many messages can not be process successfully
     B-2 : Persist the failed message to DB. (favor this option)
     * @param processedFile
     */
    private static void dealWithFailedUploading(File processedFile) {

        System.out.println("upload to S3 failed after certain times of retrying. further action may take following " +
                "javadoc.\n Pending implementation ... ...");

    }

}
