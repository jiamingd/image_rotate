# image_rotate

Application can be build and packaged in standard maven way.

## How to run application

### Publisher
execute net.jyu.imgrotation.ConsumerConsole

### Consumer
execute ProducerConsole

### S3 account and credential
aws_access_key_id and aws_secret_access_key are required to upload to S3. Please contact me if you need me to share.

### Discussion on Fault tolerance: what if the post to S3 fails
    To achieve NOT blocking the queue by not acknowledge back, which will have worker/s keep working on the same failed message, we would rather take other solutions:
        A. Retry(implemented), Retry 3 times to handle ad hoc fail.
        B. Further option after retry would be (proposal):
            B-1 : resubmit the message with processed png data to another Q for later processing. This solution has the
                  risk of reach uplimit while too many messages can not be process successfully
            B-2 : Persist the failed message to DB. (favor this option)
