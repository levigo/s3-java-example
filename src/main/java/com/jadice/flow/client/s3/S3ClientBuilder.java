package com.jadice.flow.client.s3;

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
        if("http".equalsIgnoreCase(configProperties.getProtocol())){
            builder.withClientConfiguration(new ClientConfiguration().withProtocol(Protocol.HTTP));
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
