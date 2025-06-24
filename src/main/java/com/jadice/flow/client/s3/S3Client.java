package com.jadice.flow.client.s3;

import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

/**
 * This class showcases how to create a java s3 client that generates a pre-signed url and is able to
 * use a custom s3 service solution such as minio as a backend.
 */
public class S3Client {

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

  public S3Client(ConfigProperties configProperties, long presignedUrlLifetimeInMinutes) {
    this.configurationProperties = configProperties;
    this.awsS3Client = new S3ClientBuilder().build(configProperties);
    this.presignedUrlLifetime = Duration.ofMinutes(presignedUrlLifetimeInMinutes);
  }

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
    final String identifier = getIdentifier(filename);
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
    final String identifier = this.getIdentifier(filename);
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
    return getUri(configurationProperties, identifier);
  }

  /**
   * Method to showcase the retrieval of a s3 object via its url.
   *
   * @param s3Url uri of the s3 object that shall be downloaded.
   * @return the s3 object that belongs to this url or IllegalStateException.
   */
  public S3Object getObject(final URI s3Url) {
    logger.info("Handling file download {}", s3Url.toString());
    final String[] bucketNameAndKey = getBucketNameAndKey(s3Url, configurationProperties);
    logger.debug("Creating GetObjectRequest with bucket={} and key={}", bucketNameAndKey[0], bucketNameAndKey[1]);
    final GetObjectRequest req = new GetObjectRequest(bucketNameAndKey[0], bucketNameAndKey[1]);
    try {
      return this.awsS3Client.getObject(req);
    } catch (Exception e) {
      throw new IllegalStateException("Error while fetching s3 object: " + e.getMessage());
    }
  }

  /**
   * Method to showcase the retrieval of a s3 object via its previously generated pre-signed url.
   * It just passes the URL on to the default {@link #getObject(URI)} method. 
   *
   * @param presignedUrl the previously generated pre-signed url.
   * @return the s3 object that belongs to this pre-signed url or IllegalStateException.
   */
  public S3Object getObjectViaPresignedUrl(final URI presignedUrl) {
    return getObject(presignedUrl);
  }

  /**
   * Method to showcase the deletion of a s3 object via its previously generated pre-signed url.
   *
   * @param presignedUri the previously generated pre-signed url.
   * @throws IllegalStateException if deletion fails.
   */
  public void deleteObject(final URI presignedUri) {
    logger.info("Handling file delete {}", presignedUri.toString());
    final String[] bucketNameAndKey = getBucketNameAndKey(presignedUri, configurationProperties);
    logger.debug("Creating DeleteObjectRequest with bucket={} and key={}", bucketNameAndKey[0], bucketNameAndKey[1]);
    DeleteObjectRequest req = new DeleteObjectRequest(bucketNameAndKey[0], bucketNameAndKey[1]);
    try {
      awsS3Client.deleteObject(req);
    } catch (Exception e) {
      throw new IllegalStateException("Error while deleting s3 object: " + e.getMessage());
    }
  }

  /**
   * Method to showcase the retrieval of s3 object metadata for a url.
   *
   * @param s3Url uri of the s3 object whose metadata shall be retrieved.
   * @return the s3 object metadata that belongs to this url or IllegalStateException.
   */
  public ObjectMetadata getObjectMetadata(final URI s3Url) {
    logger.info("Handling get ObjectMetadata {}", s3Url);
    final String[] bucketNameAndKey = getBucketNameAndKey(s3Url, configurationProperties);
    logger.debug("Creating GetObjectMetadataRequest with bucket={} and key={}", bucketNameAndKey[0], bucketNameAndKey[1]);
    final GetObjectMetadataRequest req = new GetObjectMetadataRequest(bucketNameAndKey[0], bucketNameAndKey[1]);
    try {
      return this.awsS3Client.getObjectMetadata(req);
    } catch (Exception e) {
      throw new IllegalStateException("Error while fetching s3 object metadata: " + e.getMessage());
    }
  }

  protected static Date computeExpirationDate(final Duration lifetime) {
    final LocalDateTime expiry = LocalDateTime.now().plus(lifetime);
    return Date.from(expiry.atZone(ZoneId.systemDefault()).toInstant());
  }

  protected URI createS3URI(final String bucket, final String fileName, final Date expiration) {
    return URI.create(awsS3Client.generatePresignedUrl(bucket, fileName, expiration).toString());
  }

  protected String getIdentifier(final String filename) {
    if (filename.matches(".*\\.[a-z]+")) {
      return configurationProperties.getSubdir() + UUID.randomUUID() + "_" + filename;
    } else {
      return configurationProperties.getSubdir() + UUID.randomUUID() + "_" + filename + ".dat";
    }
  }

  protected static URI getUri(ConfigProperties configProperties, String identifier) {
    final String scheme = configProperties.getEndpoint().getScheme() != null
        ? ""
        : configProperties.getProtocol() + "://";
    return URI.create(String.format("%s%s/%s/%s", //
        scheme, //
        configProperties.getEndpoint(), //
        configProperties.getBucket(), //
        identifier));
  }

  protected static String[] getBucketNameAndKey(final URI uri, ConfigProperties configurationProperties) {
    if (configurationProperties.isAmazonS3URIEnabled()) {
      try {
        final AmazonS3URI amazonS3URI = new AmazonS3URI(uri);
        return new String[]{amazonS3URI.getBucket(), amazonS3URI.getKey()};
      } catch (Exception e) {
        return parseURI(uri, configurationProperties.isPathStyleAccessEnabled());
      }
    } else {
      return parseURI(uri, configurationProperties.isPathStyleAccessEnabled());
    }
  }

  private static String[] parseURI(URI uri, boolean pathStyleAccessEnabled) {
    String path = uri.getPath();
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    if (pathStyleAccessEnabled) {
      int index = path.indexOf("/");
      if (index != -1) {
        // everything up to the first '/' is considered the bucket, everything after that is part of the key (including an optional directory structure)
        return new String[]{path.substring(0, index), path.substring(index + 1)};
      } else {
        throw new IllegalArgumentException("Expected bucket in URI path because pathStyleAccessEnabled=true");
      }
    } else {
      // the first subdomain is the bucket, the whole path is the key
      String[] split = uri.getHost().split("\\.");
      return new String[]{split[0], path};
    }
  }
}
