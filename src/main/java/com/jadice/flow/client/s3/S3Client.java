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

/**
 * This class showcases how to create a java s3 client that generates a pre-signed url and is able to
 * use a custom s3 service solution such as minio as a backend.
 */
public class S3Client {
    /**
     * The path separator that is used by s3 services.
     */
    public static final String S3_PATH_SEPARATOR = "/";

    /**
     * Logging.
     */
    private final Logger logger = LoggerFactory.getLogger(S3Client.class);

    /**
     * The aws s3 client instance.
     */
    private final AmazonS3 awsS3Client;

    /**
     * The desired lifetime of a pre-signed url.
     */
    private final Duration presignedUrlLifetime;

    /**
     * Configuration properties needed to configure this s3 client example.
     */
    private final ConfigProperties configurationProperties;

    public S3Client( //
            final ConfigProperties configProperties, //
            final Duration presignedUrlLifetime //
    ) {
        this.configurationProperties = configProperties;
        this.awsS3Client = new S3ClientBuilder().build(configProperties);
        this.presignedUrlLifetime = presignedUrlLifetime;
    }

    public S3Client( //
            final ConfigProperties configProperties, //
            final AmazonS3 awsS3Client, //
            final Duration presignedUrlLifetime //
    ) {
        this.configurationProperties = configProperties;
        this.awsS3Client = awsS3Client;
        this.presignedUrlLifetime = presignedUrlLifetime;
    }

    /**
     * Method to showcase the upload of a s3 object and the creation of a corresponding pre-signed url.
     *
     * @param stream The inputStream that will be uploaded.
     * @param mimeType The mimeType of the inputStream that shall be uploaded.
     * @param filename The filename of the file.
     * @return The presignedUrl for accessing the s3 object without separate authentication.
     */
    public URI putObjectAndCreatePsUri(final InputStream stream, final String mimeType, final String filename) {
        return this.putObjectAndCreatePsUri(stream, mimeType, filename, null);
    }

    /**
     * Method to showcase the upload of a s3 object and the creation of a corresponding pre-signed url.
     *
     * @param stream The inputStream that will be uploaded.
     * @param mimeType The mimeType of the inputStream that shall be uploaded.
     * @param filename The filename of the file.
     * @param contentLength The content length of the file that shall be uploaded to s3.
     * @return The presignedUrl for accessing the s3 object without separate authentication.
     */
    public URI putObjectAndCreatePsUri(final InputStream stream, final String mimeType, final String filename, final Long contentLength) {
        final String bucket = configurationProperties.getBucket();
        final Date expiration = computeExpirationDate(this.presignedUrlLifetime);
        final ObjectMetadata metadata = new ObjectMetadata();
        if (contentLength != null) {
            metadata.setContentLength(contentLength);
        }
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

    /**
     * Method to showcase the upload of a s3 object without the creation of a corresponding pre-signed url.
     *
     * @param stream The inputStream that will be uploaded.
     * @param mimeType The mimeType of the inputStream that shall be uploaded.
     * @param filename The filename of the file.
     * @return the url to the uploaded s3 file.
     */
    public URI putObject(final InputStream stream, final String mimeType, final String filename) {
        return this.putObject(stream, mimeType, filename, null);
    }

    /**
     * Method to showcase the upload of a s3 object without the creation of a corresponding pre-signed url.
     *
     * @param stream The inputStream that will be uploaded.
     * @param mimeType The mimeType of the inputStream that shall be uploaded.
     * @param filename The filename of the file.
     * @param contentLength The content length of the file that will be uploaded.
     * @return the url to the uploaded s3 file.
     */
    public URI putObject(final InputStream stream, final String mimeType, final String filename, final Long contentLength) {
        final String bucket = configurationProperties.getBucket();
        final Date expiration = computeExpirationDate(this.presignedUrlLifetime);
        final String identifier = UUID.randomUUID() + "_" + filename + ".dat";
        final ObjectMetadata metadata = new ObjectMetadata();
        if (contentLength != null) {
            metadata.setContentLength(contentLength);
        }
        metadata.setExpirationTime(expiration);
        if (mimeType != null) {
            metadata.setContentType(mimeType);
        }
        final PutObjectRequest request = new PutObjectRequest( //
                bucket, //
                identifier, //
                stream, //
                metadata //
        );
        // we don't care about any kind of stupid read limit, as our streams are actually seekable. Take that, crappy InputStream hierarchy!
        request.getRequestClientOptions().setReadLimit(Integer.MAX_VALUE);
        awsS3Client.putObject(request);
        return URI.create(configurationProperties.getProtocol() + "://" + configurationProperties.getEndpoint() + "/" + configurationProperties.getBucket() + "/" + identifier);
    }

    /**
     * Method to showcase the retrieval of a s3 object via its previously generated pre-signed url.
     *
     * @param s3Url uri of the s3 object that shall be downloaded.
     * @return the s3 object that belongs to this url or IllegalStateException.
     */
    public S3Object getObject(final URI s3Url) {
        return this.getObjectViaPresignedUrl(s3Url);
    }

    /**
     * Method to showcase the retrieval of a s3 object via its previously generated pre-signed url.
     *
     * @param presignedUrl the previously generated pre-signed url.
     * @return the s3 object that belongs to this pre-signed url or IllegalStateException.
     */
    public S3Object getObjectViaPresignedUrl(final URI presignedUrl) {
        final String path = presignedUrl.getPath();
        logger.info("Handling file download {}", path);
        final String[] objectCredentials = path.split(S3_PATH_SEPARATOR);
        final GetObjectRequest req = new GetObjectRequest(objectCredentials[1], objectCredentials[2]);
        try {
            return this.awsS3Client.getObject(req);
        } catch (Exception e) {
            throw new IllegalStateException("Error while fetching s3 object: " + e.getMessage());
        }
    }

    /**
     * Method to showcase the deletion of a s3 object via its previously generated pre-signed url.
     *
     * @param presignedUri the previously generated pre-signed url.
     * @throws IllegalStateException if deletion fails.
     */
    public void deleteObject(final URI presignedUri) {
        final String path = presignedUri.getPath();
        logger.info("Handling file delete {}", path);
        String[] objectCredentials = path.split(S3_PATH_SEPARATOR);
        DeleteObjectRequest req = new DeleteObjectRequest(objectCredentials[1], objectCredentials[2]);
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
}
