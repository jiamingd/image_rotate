package net.jyu.imgrotation;

import java.io.File;
import java.io.IOException;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;

/**
 * Created by jyu on 4/13/16.
 */
public class S3UploadWorker {
    private static String DEFAULT_BUCKET     = "jiamingtranscript";

    private AmazonS3 s3client = new AmazonS3Client(new ProfileCredentialsProvider());
    private static S3UploadWorker instance = new S3UploadWorker();

    public static S3UploadWorker getInstance(){
        return instance;
    }

    public void upload2DefaultBucket(File file, String key) throws UploadFailException {

        try {
            s3client.putObject(new PutObjectRequest(DEFAULT_BUCKET, key, file));
        } catch (AmazonServiceException ase) {
            StringBuffer sb = new StringBuffer();
            sb.append("Caught an AmazonServiceException, which " +
                    "means your request made it " +
                    "to Amazon S3, but was rejected with an error response" +
                    " for some reason.");
            sb.append("Error Message:    " + ase.getMessage());
            sb.append("HTTP Status Code: " + ase.getStatusCode());
            sb.append("AWS Error Code:   " + ase.getErrorCode());
            sb.append("Error Type:       " + ase.getErrorType());
            sb.append("Request ID:       " + ase.getRequestId());
            throw new UploadFailException("S3 upload fail:" + sb.toString());
        } catch (AmazonClientException ace) {
            StringBuffer sb = new StringBuffer();
            sb.append("Caught an AmazonClientException, which " +
                    "means the client encountered " +
                    "an internal error while trying to " +
                    "communicate with S3, " +
                    "such as not being able to access the network.");
            sb.append("Error Message: " + ace.getMessage());
            throw new UploadFailException("S3 upload fail : " + sb);
        }
    }


}
