/**
 * 
 */
package org.sample.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.sample.execution.HttpRequester;
import org.sample.util.CommonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @author SankyS
 *
 */
@DisplayName("Cosy Analysis Test")
public class CosyFlowTest {

  private HttpRequester httpRequestor;

  private final static String TEST_XML_URL = "cosy/test";
  private final static String COSY_ANALYSIS_URL = "cosy/analysis";
  private final static String COSY_INFORMATION_IMAGE_URL = "cosy/information/image";
  private final static String TEST_DOCUMENT_ID = "/"
      + CommonUtils.getEnvValue("INFORMATION_DOCUMENT_ID", "a4def9d3-1361-46c5-8f81-491700dbfaab");
  private final static String SERVER_DOMAIN =
      CommonUtils.getEnvValue("SERVER_DOMAIN", "https://red.ai4bd.org:8087/");

  private static String AUTHENTICATION_TOKEN = CommonUtils.getEnvValue("COSY_AUTHENTICATION_TOKEN");
  private static DocumentBuilder XML_PARSER;
  private static XPath XPATH_READER;

  @BeforeAll
  static void setup() throws ParserConfigurationException {
    if (StringUtils.isBlank(AUTHENTICATION_TOKEN)) {
      // arrange the credentials and url
      String username = CommonUtils.getEnvValue("AUTHORIZATION_CREDS_ADMIN_USR");
      String password = CommonUtils.getEnvValue("AUTHORIZATION_CREDS_ADMIN_PSW");
      String authenticationEndpoint =
          CommonUtils.getEnvValue("AUTHORIZATION_URL", "https://sso.ai4bd.org/login");

      // assertion on username, password, endpoint
      assertTrue(StringUtils.isNoneBlank(username, password, authenticationEndpoint),
          "Credentials for SSO login should not be blank.");

      // execute rest client call
      HttpResponse<String> authenticationResponse = new HttpRequester().postWithParam(
          authenticationEndpoint, Map.of("username", username, "password", password));
      // assertion on login response
      assertNotNull(authenticationResponse, "Authentication endpoint response should not be null.");
      assertEquals(201, authenticationResponse.statusCode(),
          "Authentication Server should return status code '201'");

      // setup token
      AUTHENTICATION_TOKEN = authenticationResponse.body();

    }
    // setup data for testing
    XML_PARSER = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    XPATH_READER = XPathFactory.newInstance().newXPath();
  }

  @BeforeEach
  void init() {
    httpRequestor = new HttpRequester();
  }

  @AfterAll
  static void cleanUp() {
    // TODO: Delete/Remove all test data collection from COSY environment.

  }

  @Test
  @Disabled
  @DisabledOnOs({OS.WINDOWS})
  @DisplayName("Test Cosy Rest for unauthorized access. No Authorization token passed.")
  void testCosyRestForbiddenResponse() {
    // Rest Client call execution for forbidden response code
    HttpResponse<String> responseFromCosy =
        httpRequestor.postWithFile(SERVER_DOMAIN + COSY_ANALYSIS_URL, "test.xml",
            Map.of("Content-Type", "application/xml",
                "Cosy-Filename", "test.xml"));

    // assertion for not null response and response code 403
    assertNotNull(responseFromCosy, "Should not be null.");
    assertEquals(403, responseFromCosy.statusCode(), "Response status code must be '403' ");
  }

  @Test
  @Disabled
  @DisabledOnOs({OS.WINDOWS})
  @DisplayName("Test Cosy Rest for not supported MIME Type.")
  void testCosyRestBadRequest() {
    // Rest Client call for bad request response code
    HttpResponse<String> responseFromCosy =
        httpRequestor.postWithFile(SERVER_DOMAIN + COSY_ANALYSIS_URL, "test.xml",
            Map.of("Authorization", "Bearer " + AUTHENTICATION_TOKEN, 
                "Content-Type", "application/xml", 
                "Cosy-Filename", "test.xml", 
                "Cosy-Synchronous-Processing", "true"));

    // assertion for not null response and response code 400
    assertNotNull(responseFromCosy, "Should not be null.");
    assertEquals(400, responseFromCosy.statusCode(), "Response status code must be '400' ");
  }

  @Test
  @Disabled
  @DisabledOnOs({OS.WINDOWS})
  @DisplayName("Test Cosy Rest Juzo data analysis.")
  void testJuzoAnalysis() throws SAXException, IOException, XPathExpressionException {

    // Rest client call for cosy analysis.
    HttpResponse<String> responseFromCosy =
        httpRequestor.postWithFile(SERVER_DOMAIN + COSY_ANALYSIS_URL, "Bein_LY_5_1.png",
            Map.of("Authorization", "Bearer " + AUTHENTICATION_TOKEN,
                "Content-Type", "image/png",
                "Cosy-Filename", "Bein_LY_5_1.png", 
                "Cosy-Synchronous-Processing", "true",
                "Cosy-Target-Format", "xml"));


    // assertion for basic response
    assertNotNull(responseFromCosy, "Should not be null.");
    assertEquals(200, responseFromCosy.statusCode(), "Response status code must be '200' ");

    // assertion checks for response body as xml.
    Document xmlDocument =
        XML_PARSER.parse(new ByteArrayInputStream(responseFromCosy.body().getBytes()));
    Node customerIdNode = (Node) XPATH_READER.evaluate("/order/orderOptions/customerAccountId",
        xmlDocument, XPathConstants.NODE);
    assertNotNull(customerIdNode, "Node : customerAccountId should not be null.");
    assertEquals("2126800", customerIdNode.getFirstChild().getNodeValue(),
        "The customer id must be same.");
    Node itemConsignmentName = (Node) XPATH_READER.evaluate(
        "/order/items/item[@page='1']/textFeature[@name='consignment']", xmlDocument,
        XPathConstants.NODE);
    assertNotNull(itemConsignmentName, "Node should not be null.");
    assertEquals("Molons Gabnel", itemConsignmentName.getFirstChild().getNodeValue(),
        "The consignment name must be same.");


  }


  @Test
  @Disabled("Because shacl processor fails to recognize @id")
  @DisabledOnOs({OS.WINDOWS})
  @DisplayName("Test Cosy Rest for Aisee Analysis.")
  void testAiseeAnalysis() throws SAXException, IOException {

    // Rest client call for cosy aisee analysis.
    HttpResponse<String> responseFromCosy =
        httpRequestor.postWithFile(SERVER_DOMAIN + COSY_ANALYSIS_URL, "example_invoice_aisee.pdf",
            Map.of("Authorization", "Bearer " + AUTHENTICATION_TOKEN, 
                "Content-Type", "application/pdf", 
                "Cosy-Filename", "example_invoice_aisee.pdf",
                "Cosy-Synchronous-Processing", "true"));
    // assertion for basic response
    assertNotNull(responseFromCosy, "Should not be null.");
    assertEquals(200, responseFromCosy.statusCode(), "Response status code must be '200' ");

  }

  @Test
  @Disabled("Since REST Gatekeeper processor check is wrong")
  @DisabledOnOs({OS.WINDOWS})
  @DisplayName("Test Cosy Rest for Get Cropped Image")
  void testCosyCropImage() {
    // Rest client for cosy information
    HttpResponse<String> responseFromCosy =
        httpRequestor.getRequest(SERVER_DOMAIN + COSY_INFORMATION_IMAGE_URL + TEST_DOCUMENT_ID,
            Map.of("Authorization", "Bearer " + AUTHENTICATION_TOKEN, 
                "Cosy-Resource", "http://ai4bd.com/resource/cdm/ai4bd/alpha/firstname"));

    // assertion for basic response
    assertNotNull(responseFromCosy, "Should not be null.");
    assertEquals(200, responseFromCosy.statusCode(), "Response status code must be '200' ");

  }
}
