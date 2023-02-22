package com.jadice.flow.client.s3;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Disabled
public class S3ClientTest {
    private static S3Client s3Client;

    @BeforeAll
    public static void setupClass() throws IOException {
        final Yaml yaml = new Yaml();
        try (final InputStream inputStream = S3ClientTest.class.getResourceAsStream("/application-test.yml")) {
            final Map yamlMap = yaml.load(inputStream);
            final Map publisher = (Map) yamlMap.get("publisher");
            final Map s3 = (Map) publisher.get("s3");
            final String bucket = (String) s3.get("bucket");
            final String endpoint = (String) s3.get("endpoint");
            final String accessKey = (String) s3.get("access-key");
            final String secretKey = (String) s3.get("secret-key");
            final String protocol = (String) s3.get("protocol");
            final ConfigProperties configProperties = new ConfigProperties( //
                    URI.create(endpoint), //
                    bucket, //
                    null, //
                    accessKey, //
                    secretKey, //
                    protocol //
            );
            s3Client = new S3Client(configProperties, Duration.ofHours(1));
        }
    }

    @Test
    void test() throws IOException {
        try (final InputStream resourceAsStream = this.getClass().getResourceAsStream("/test.txt")) {
            final URI presignedS3Uri = s3Client.putObjectAndCreatePsUri(resourceAsStream, "text/plain", "test.txt");
            assertNotNull(presignedS3Uri);
            final S3Object s3Object = s3Client.getObjectViaPresignedUrl(presignedS3Uri);
            final S3ObjectInputStream objectContent = s3Object.getObjectContent();
            assertNotNull(objectContent);
            s3Client.deleteObject(presignedS3Uri);
        }
    }

    @Test
    void test2() throws IOException {
        try (final InputStream resourceAsStream = this.getClass().getResourceAsStream("/test.txt")) {
            final URI s3Uri = s3Client.putObject(resourceAsStream, "text/plain", "test.txt");
            assertNotNull(s3Uri);
            final S3Object s3Object = s3Client.getObject(s3Uri);
            final S3ObjectInputStream objectContent = s3Object.getObjectContent();
            assertNotNull(objectContent);
            s3Client.deleteObject(s3Uri);
        }
    }
}
