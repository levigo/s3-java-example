package com.jadice.flow.client.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class S3Client {
    public static final String S3_PATH_SEPARATOR = "/";
    private final Logger logger = LoggerFactory.getLogger(S3Client.class);
    private final AmazonS3 awsS3Client;
    private final Duration lifetime;
    private final ConfigProperties configurationProperties;

    public S3Client(final ConfigProperties configProperties, final AmazonS3 awsS3Client, final Duration lifetime) {
        this.configurationProperties = configProperties;
        this.awsS3Client = awsS3Client;
        this.lifetime = lifetime;
    }

    public S3Object getObject(final URI uri) {
        final String path = uri.getPath();
        logger.info("Handling file download {}", path);
        final String[] objectCredentials = path.split(S3_PATH_SEPARATOR);
        final GetObjectRequest req = new GetObjectRequest(objectCredentials[1], objectCredentials[2]);
        if (objectCredentials.length > 3) {
            throw new IllegalStateException("URI not correct");
        }
        try {
            return this.awsS3Client.getObject(req);
        } catch (Exception e) {
            throw new IllegalStateException("Error while fetching s3 object: " + e.getMessage());
        }
    }

    public void deleteObject(final URI uri) {
        final String path = uri.getPath();
        logger.info("Handling file delete {}", path);
        String[] objectCredentials = path.split(S3_PATH_SEPARATOR);
        DeleteObjectRequest req = new DeleteObjectRequest(objectCredentials[1], objectCredentials[2]);
        if (objectCredentials.length > 3) {
            throw new IllegalStateException("URI not correct");
        }
        try {
            awsS3Client.deleteObject(req);
        } catch (Exception e) {
            throw new IllegalStateException("Error while deleting s3 object: " + e.getMessage());
        }
    }

    protected static Date computeExpirationDate(final Duration lifetime) {
        final LocalDateTime expiry = LocalDateTime.now().plus(lifetime);
        return Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant());
    }

    protected URI createS3URI(final String bucket, final String fileName, final Date expiration) {
        return URI.create(awsS3Client.generatePresignedUrl(bucket, fileName, expiration).toString());
    }

    public URI putObjectAndCreatePsUri(final InputStream stream, final String mimeType, final String filename) {
        final String bucket = configurationProperties.getBucket();
        final Date expiration = computeExpirationDate(this.lifetime);
        final ObjectMetadata metadata = new ObjectMetadata();
        metadata.setExpirationTime(expiration);
        if (mimeType != null) {
            metadata.setContentType(mimeType);
        }
        final String identifier = UUID.randomUUID() + "_" + filename + ".dat";
        final PutObjectRequest request = new PutObjectRequest( //
                bucket, //
                identifier, //
                stream, //
                metadata //
        );
        // we don't care about any kind of stupid read limit, as our streams are actually seekable. Take that, crappy InputStream hierarchy!
        request.getRequestClientOptions().setReadLimit(Integer.MAX_VALUE);
        awsS3Client.putObject(request);
        return createS3URI(bucket, identifier, expiration);
    }
}
