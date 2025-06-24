package com.jadice.flow.client.s3;

import java.net.URI;
import java.util.Objects;

/**
 * Configuration object that contains all the necessary connection information for accessing the s3 storage.
 */
public class ConfigProperties {

    /**
     * The endpoint url to the s3 storage.
     */
    URI endpoint;
    String bucket;
    String subdir;
    String region;
    String accessKey;
    String secretKey;
    String protocol;
    boolean trustSelfSigned = false;
    boolean trustAll = false;
    // prefer the path style access, as minio uses that mode
    boolean pathStyleAccessEnabled = true;
    // try to parse URI as AmazonS3URI first
    boolean amazonS3URIEnabled = true;

    public ConfigProperties() {}

    public ConfigProperties( //
        final URI endpoint, //
        final String bucket, //
        final String region, //
        final String accessKey, //
        final String secretKey, //
        final String protocol, //
        final boolean trustSelfSigned, //
        final boolean trustAll //
    ) {
      this(endpoint, bucket, "", region, accessKey, secretKey, protocol, trustSelfSigned, trustAll, true, true);
    }

    // full-args constructor
    public ConfigProperties( //
        final URI endpoint, //
        final String bucket, //
        final String subdir, //
        final String region, //
        final String accessKey, //
        final String secretKey, //
        final String protocol, //
        final boolean trustSelfSigned, //
        final boolean trustAll, //
        final boolean pathStyleAccessEnabled, //
        final boolean amazonS3URIEnabled //
    ) {
      this.endpoint = endpoint;
      this.bucket = bucket;
      this.subdir = sanitizePath(subdir);
      this.region = region;
      this.accessKey = accessKey;
      this.secretKey = secretKey;
      this.protocol = protocol;
      this.trustSelfSigned = trustSelfSigned;
      this.trustAll = trustAll;
      this.pathStyleAccessEnabled = pathStyleAccessEnabled;
      this.amazonS3URIEnabled = amazonS3URIEnabled;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(final URI endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(final String bucket) {
        this.bucket = bucket;
    }

    public String getSubdir() {
        return subdir;
    }

    public void setSubdir(String subdir) {
        this.subdir = sanitizePath(subdir);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean isTrustSelfSigned() {
        return trustSelfSigned;
    }

    public void setTrustSelfSigned(boolean trustSelfSigned) {
        this.trustSelfSigned = trustSelfSigned;
    }

    public boolean isTrustAll() {
        return trustAll;
    }

    public void setTrustAll(boolean trustAll) {
        this.trustAll = trustAll;
    }

    public boolean isPathStyleAccessEnabled() {
        return pathStyleAccessEnabled;
    }

    public void setPathStyleAccessEnabled(boolean pathStyleAccessEnabled) {
        this.pathStyleAccessEnabled = pathStyleAccessEnabled;
    }

    public boolean isAmazonS3URIEnabled() {
        return amazonS3URIEnabled;
    }

    public void setAmazonS3URIEnabled(boolean amazonS3URIEnabled) {
        this.amazonS3URIEnabled = amazonS3URIEnabled;
    }

    public static String sanitizePath(String s) {
      if (s == null || s.isEmpty()) {
        return "";
      } else if (s.endsWith("/")) {
        return s;
      } else {
        return s + "/";
      }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigProperties that = (ConfigProperties) o;
        return trustSelfSigned == that.trustSelfSigned && trustAll == that.trustAll && pathStyleAccessEnabled == that.pathStyleAccessEnabled
            && amazonS3URIEnabled == that.amazonS3URIEnabled && Objects.equals(
            endpoint, that.endpoint) && Objects.equals(bucket, that.bucket) && Objects.equals(region,
            that.region) && Objects.equals(accessKey, that.accessKey) && Objects.equals(secretKey,
            that.secretKey) && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, bucket, region, accessKey, secretKey, protocol, trustSelfSigned, trustAll,
            pathStyleAccessEnabled, amazonS3URIEnabled);
    }
}
