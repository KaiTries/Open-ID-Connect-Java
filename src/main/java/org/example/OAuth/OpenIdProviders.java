package org.example.OAuth;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.rio.RDFFormat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class OpenIdProviders {

    private final Map<URI, OpenIdProvider> providerMap;

    public OpenIdProviders() {
        this.providerMap = new HashMap<>();
    }

    public OpenIdProvider useProvider(URI issuer) throws IOException, InterruptedException {
        if (providerMap.containsKey(issuer)) {
            return providerMap.get(issuer);
        }
        OpenIdProvider newProvider = new OpenIdProvider(issuer.toString());
        providerMap.put(issuer, newProvider);
        return newProvider;
    }

    public Map<URI, OpenIdProvider> getProviderMap() {
        return providerMap;
    }

    public static String getIssuerFromWebID(String WebID) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(WebID)).GET().build();
        // profile of agent in rdf format
        var response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());


        Model model = RdfModelUtils.stringToModel(response.body(),RdfModelUtils.createIri(response.uri().toString()),
                RDFFormat.TURTLE
        );

        var results = model.getStatements(null,RdfModelUtils.createIri("http://www.w3.org/ns/solid/terms#oidcIssuer"),null);

        return results.iterator().next().getObject().stringValue();
    }
}
