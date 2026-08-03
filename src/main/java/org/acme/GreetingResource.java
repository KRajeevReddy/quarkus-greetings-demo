package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/hello")
public class GreetingResource {

    @ConfigProperty(name = "greeting.message", defaultValue = "Hello")
    String message;

    @ConfigProperty(name = "app.environment", defaultValue = "local")
    String environment;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            name = "DevOps Engineer";
        }
        return message + " " + name + "! (environment: " + environment + ")";
    }

    @GET
    @Path("/info")
    @Produces(MediaType.APPLICATION_JSON)
    public Info info() {
        return new Info(message, environment);
    }

    public record Info(String message, String environment) {}
}