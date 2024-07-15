import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.example.OAuth.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;

public class AuthTests {

    private static final String WEBID_SOLID_COMMUNITY = "https://podofkai.solidcommunity.net/profile/card#me";
    private static final String ISSUER_URI = "https://solidcommunity.net";
    private static final String ISSUER_METADATA_ENDPOINT = "/.well-known/openid-configuration";
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final OpenIdClientMetadata metadata = new OpenIdClientMetadata();
    private static final ObjectMapper mapper =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Basic tests
     */
    @Test
    public void createUserIdProviders() throws IOException, InterruptedException {
        OpenIdProviders openIdProviders = new OpenIdProviders();

        // start with empty providers
        Assertions.assertTrue(openIdProviders.getProviderMap().isEmpty());

        // use provider will create new provider
        var obj1 = openIdProviders.useProvider(URI.create(ISSUER_URI));
        Assertions.assertEquals(1, openIdProviders.getProviderMap().size());

        // use provider should return same provider if exists already
        var obj2 = openIdProviders.useProvider(URI.create(ISSUER_URI));
        Assertions.assertEquals(1, openIdProviders.getProviderMap().size());
        Assertions.assertSame(obj1, obj2);

        Assertions.assertEquals(
                ISSUER_URI,
                obj1.getIssuer()
        );
        Assertions.assertEquals(
                ISSUER_URI,
                obj1.getMetadata().getIssuer().toString()
        );
    }

    @Test
    public void testGetIssuerFromWebID() throws IOException, InterruptedException {
        String issuer = OpenIdProviders.getIssuerFromWebID(WEBID_SOLID_COMMUNITY);
        Assertions.assertEquals(
                ISSUER_URI,
                issuer
        );
    }

    /**
     * Expectations when querying the /.well-known/openid-configuration
     */
    @Test
    public void validIssuerReturnsMetadata() throws IOException, InterruptedException {
        final String endpoint = ISSUER_URI + ISSUER_METADATA_ENDPOINT;

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(endpoint)).GET().build();

        // specifications in json of the issuer
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(
                200,
                response.statusCode()
        );

        try {
            var data = mapper.readValue(response.body(), OpenIdProviderMetadata.class);
            Assertions.assertEquals(
                    URI.create(ISSUER_URI),
                    data.getIssuer()
            );
        } catch (Exception e) {
            Assertions.fail();
        }
    }

    @Test
    public void dynamicRegistrationWithValidIssuer() throws IOException, InterruptedException, IllegalAccessException {
        OpenIdProvider provider = new OpenIdProvider(ISSUER_URI);

        URI registrationEndpoint = provider.getMetadata().getRegistration_endpoint();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(registrationEndpoint)
                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                .POST(
                        HttpRequest.BodyPublishers.ofString(
                                metadata.getInstantiatedFieldsAsJson()
                        )
                ).build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());


        try {
            var openIdClient = new OpenIdClient(mapper.readValue(response.body(), OpenIdClientMetadata.class));
            Assertions.assertNotNull(
                    openIdClient
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetAuthenticationUrlSucceeds() throws IOException, InterruptedException, IllegalAccessException,
            NoSuchAlgorithmException {
        OpenIdProviders providers = new OpenIdProviders();

        OpenIdProvider provider = providers.useProvider(URI.create(ISSUER_URI));

        Assertions.assertEquals(
                ISSUER_URI,
                provider.getIssuer()
        );

        var state = OAuthUtils.generateRandomState();
        var verifier = OAuthUtils.generateCodeVerifier();

        provider.register();
        var LOGIN_URL = provider.getAuthUrl(state, OAuthUtils.generateCodeChallenge(verifier));

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(LOGIN_URL)).GET().build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        Assertions.assertEquals(
                302,
                response.statusCode()
        );
    }
}
