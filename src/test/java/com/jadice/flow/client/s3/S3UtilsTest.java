package com.jadice.flow.client.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

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


  @ParameterizedTest
  @ValueSource(strings = {"subdir", "sub/subdir", "trailing/", "sub/sub/"})
  @NullAndEmptySource
  void test_getUriWithSubdirectory(String subdir) {
    final ConfigProperties configProperties = createConfigPropertiesWithEndpoint("test.endpoint.sample.com", subdir);
    final S3Client s3Client = new S3Client(configProperties, 5L);
    final URI uriWithProtocol = S3Client.getUri(configProperties, s3Client.getIdentifier("test-identifier"));
    subdir = subdir == null ? "" : subdir;
    String uri = uriWithProtocol.toString();
    System.out.println("uri: " + uri + "\nsubdir: " + subdir);
    assertTrue(uri.startsWith("test://test.endpoint.sample.com/test-bucket/" + subdir));
    assertTrue(uri.endsWith("_test-identifier.dat"));
  }

  @Test
  void test_getIdentifier() {
    final ConfigProperties configProperties = createConfigPropertiesWithEndpoint("test.endpoint.sample.com", "subdir");
    final S3Client s3Client = new S3Client(configProperties, 5L);

    final String identifier = s3Client.getIdentifier("my-file.pdf");
    System.out.println(identifier);
    assertTrue(identifier.startsWith("subdir/"));
    assertTrue(identifier.endsWith("_my-file.pdf"));
    assertTrue(identifier.matches("subdir/[a-z0-9-]+_my-file\\.pdf"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "https://test.endpoint.sample.com/test-bucket/test-identifier",
      "https://s3.us-east-1.amazonaws.com/test-bucket/test-identifier",
      "https://test-bucket.s3.us-east-1.amazonaws.com/test-identifier"
  })
  void test_getBucketNameAndKey(String uriString) {
    String[] bucketNameAndKey = S3Client.getBucketNameAndKey(
        URI.create(uriString), createConfigPropertiesWithEndpoint(uriString));
    assertEquals("test-bucket", bucketNameAndKey[0]);
    assertEquals("test-identifier", bucketNameAndKey[1]);
  }

  @ParameterizedTest
  @ValueSource(booleans = {
      true, false
  })
  void test_getBucketName_withAmazonS3URIEnabled_false(boolean setPathStyleAccessEnabled) {
    // given
    String uriString = "https://test-subdomain.s3.us-east-1.amazonaws.com/test-bucket/test-identifier";
    ConfigProperties configProperties = createConfigPropertiesWithEndpoint(uriString);
    configProperties.setAmazonS3URIEnabled(false);
    configProperties.setPathStyleAccessEnabled(setPathStyleAccessEnabled);
    // when
    String[] bucketNameAndKey = S3Client.getBucketNameAndKey(
        URI.create(uriString), configProperties);
    // then
    if (setPathStyleAccessEnabled) {
      assertEquals("test-bucket", bucketNameAndKey[0]);
      assertEquals("test-identifier", bucketNameAndKey[1]);
    } else {
      assertEquals("test-subdomain", bucketNameAndKey[0]);
      assertEquals("test-bucket/test-identifier", bucketNameAndKey[1]);
    }
  }

  private ConfigProperties createConfigPropertiesWithEndpoint(String endpoint) {
    ConfigProperties configProperties = new ConfigProperties();
    configProperties.setEndpoint(URI.create(endpoint));
    configProperties.setBucket("test-bucket");
    configProperties.setProtocol("test");
    return configProperties;
  }

  private ConfigProperties createConfigPropertiesWithEndpoint(String endpoint, String subdir) {
    return new ConfigProperties(
        URI.create(endpoint), //
        "test-bucket", //
        subdir, //
        "", //
        "", //
        "", //
        "test", //
        false, //
        false, //
        false, //
        true //
    );
  }
}
