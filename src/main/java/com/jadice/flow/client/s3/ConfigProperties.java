package com.jadice.flow.client.s3;

import java.net.URI;
import java.util.Objects;

import com.amazonaws.services.s3.model.Region;

/**
 * Configuration object that contains all the necessary connection information for accessing the s3 storage.
 */
public class ConfigProperties {

    /**
     * The endpoint url to the s3 storage.
     */
    URI endpoint;
    String bucket;
    String region;
    String accessKey;
    String secretKey;
    String protocol;
    boolean trustSelfSigned = false;
    boolean trustAll = false;

    public ConfigProperties() {}

    public ConfigProperties( //
        final URI endpoint, //
        final String bucket, //
        final String region, //
        final String accessKey, //
        final String secretKey, //
        final String protocol ,//
        final boolean trustSelfSigned, //
        final boolean trustAll //
    ) {
        this.endpoint = endpoint;
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.protocol = protocol;
        this.trustSelfSigned = trustSelfSigned;
        this.trustAll = trustAll;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ConfigProperties that = (ConfigProperties) o;
        return trustSelfSigned == that.trustSelfSigned && trustAll == that.trustAll && Objects.equals(endpoint,
            that.endpoint) && Objects.equals(bucket, that.bucket) && region == that.region && Objects.equals(accessKey,
            that.accessKey) && Objects.equals(secretKey, that.secretKey) && Objects.equals(protocol, that.protocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endpoint, bucket, region, accessKey, secretKey, protocol, trustSelfSigned, trustAll);
    }
}
