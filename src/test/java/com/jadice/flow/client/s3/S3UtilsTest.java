package com.jadice.flow.client.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.net.URI;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class S3UtilsTest {

  static Stream<Arguments> dataProvider() {
    return Stream.of(arguments("document.pdf", "document.pdf"), arguments("document", "document.dat"));
  }

  @ParameterizedTest
  @MethodSource("dataProvider")
  void test_getIdentifier(String filename, String expected) {
    final String identifier = S3Client.getIdentifier(filename);
    System.out.println(identifier);
    assertTrue(identifier.endsWith(expected));
  }

  @Test
  void test_getUri(){
    final ConfigProperties configProperties = createConfigPropertiesWithEndpoint("test.endpoint.sample.com");
    final URI uriWithProtocol = S3Client.getUri(configProperties, "test-identifier");
    assertEquals("test://test.endpoint.sample.com/test-bucket/test-identifier", uriWithProtocol.toString());
    final ConfigProperties configProperties2 = createConfigPropertiesWithEndpoint("https://test.endpoint.sample.com");
    final URI uriWithSchema= S3Client.getUri(configProperties2, "test-identifier");
    assertEquals("https://test.endpoint.sample.com/test-bucket/test-identifier", uriWithSchema.toString());
  }

  private ConfigProperties createConfigPropertiesWithEndpoint(String endpoint){
    ConfigProperties configProperties = new ConfigProperties();
    configProperties.setEndpoint(URI.create(endpoint));
    configProperties.setBucket("test-bucket");
    configProperties.setProtocol("test");
    return configProperties;
  }
}
