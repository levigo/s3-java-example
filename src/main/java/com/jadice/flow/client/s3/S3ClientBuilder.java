package com.jadice.flow.client.s3;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;

import com.amazonaws.ApacheHttpClientConfig;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Region;

public class S3ClientBuilder {

    public AmazonS3 build(final ConfigProperties configProperties) {
        final String region = determineRegion(configProperties);
        final AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(configProperties.getEndpoint().toString(), region)
            )
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                configProperties.getAccessKey(),
                configProperties.getSecretKey()
            )));
        // prefer the path style access, as minio uses that mode
        builder.setPathStyleAccessEnabled(true);

        final ClientConfiguration clientConfiguration = new ClientConfiguration();

        // enforce HTTP
        if("http".equalsIgnoreCase(configProperties.getProtocol())){
            builder.withClientConfiguration(clientConfiguration.withProtocol(Protocol.HTTP));
        } else if (configProperties.isTrustSelfSigned()) {
            // trust self-signed certificates
            SSLContext sslContext = null;
            try {
                sslContext = new SSLContextBuilder().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build();
            } catch (NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
                throw new IllegalStateException("Failed to initialize SSLContext with TrustSelfSignedStrategy", e);
            }
            final ApacheHttpClientConfig apacheHttpClientConfig = clientConfiguration.getApacheHttpClientConfig();
            apacheHttpClientConfig.setSslSocketFactory(new SSLConnectionSocketFactory(sslContext));
            builder.withClientConfiguration(clientConfiguration);
        }
        return builder.build();
    }

    protected String determineRegion(final ConfigProperties configProperties) {
        if (configProperties.getRegion() != null) {
            return configProperties.getRegion().getFirstRegionId();
        }
        return Region.EU_Frankfurt.getFirstRegionId();
    }
}
