package com.jadice.flow.client.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.net.URI;

import org.junit.jupiter.api.Test;

class S3UtilsTest {

  @Test
  void test_getScheme() {
    ConfigProperties configProperties = new ConfigProperties();
    configProperties.setEndpoint(URI.create("s3.levigo.net"));
    String scheme = configProperties.getEndpoint().getScheme();
    assertNull(scheme);
  }

  @Test
  void test_getUri() {
    final ConfigProperties configProperties = createConfigPropertiesWithEndpoint("test.endpoint.sample.com");
    final URI uriWithProtocol = S3Client.getUri(configProperties, "test-identifier");
    assertEquals("test://test.endpoint.sample.com/test-bucket/test-identifier", uriWithProtocol.toString());

    final ConfigProperties configProperties2 = createConfigPropertiesWithEndpoint("https://test.endpoint.sample.com");
    final URI uriWithSchema = S3Client.getUri(configProperties2, "test-identifier");
    assertEquals("https://test.endpoint.sample.com/test-bucket/test-identifier", uriWithSchema.toString());

    final ConfigProperties configProperties3 = createConfigPropertiesWithEndpoint("test.endpoint.sample.com");
    configProperties3.setProtocol("https");
    final URI uriWithProtocol3 = S3Client.getUri(configProperties3, "test-identifier");
    assertEquals("https://test.endpoint.sample.com/test-bucket/test-identifier", uriWithProtocol3.toString());
  }

  private ConfigProperties createConfigPropertiesWithEndpoint(String endpoint) {
    ConfigProperties configProperties = new ConfigProperties();
    configProperties.setEndpoint(URI.create(endpoint));
    configProperties.setBucket("test-bucket");
    configProperties.setProtocol("test");
    return configProperties;
  }
}
