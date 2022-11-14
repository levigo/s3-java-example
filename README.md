# S3 java client demo repository (with pre-signed url showcase)
This project serves as an example of how to create a s3 client in java for custom instances (not provided by aws) in 
order to:
- upload a file to s3 and receiving a presigned url that allows to access the file for a certain duration
- retrieving a file via a presigned url from the s3 service
- deleting a file via a presigned url from the s3 service

# Tests
To be able to run the tests provide an `application-test.yml` file in the `src/test/resources` folder with the following 
content:

```
publisher:
  s3:
    bucket: <bucket-name>
    endpoint: <s3-service-endpoint-name>
    access-key: <s3-service-access-key>
    secret-key: <s3-service-secret-key>
    protocol: <s3-service-http-protocol-scheme>
```

And after that remove the `@Disabled` annotation.

The build process of this repository creates a jar and a jar-with-dependencies that includes all the dependencies for
this project into on fat jar.
