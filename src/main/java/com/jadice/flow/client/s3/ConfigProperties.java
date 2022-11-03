package com.jadice.flow.client.s3;

import com.amazonaws.services.s3.model.Region;

import java.net.URI;

/**
 * Configuration object that contains all the necessary connection information for accessing the s3 storage.
 */
public class ConfigProperties {
    URI endpoint;
    String bucket;
    Region region;
    String accessKey;
    String secretKey;
    String protocol;

    public ConfigProperties() {}

    public ConfigProperties( //
        final URI endpoint, //
        final String bucket, //
        final Region region, //
        final String accessKey, //
        final String secretKey, //
        final String protocol //
    ) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.protocol = protocol;
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

    public Region getRegion() {
        return region;
    }

    public void setRegion(final Region region) {
        this.region = region;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
