package org.acme.resteasy.resource;

import io.smallrye.jwt.build.Jwt;
import org.acme.resteasy.model.Authority;
import org.acme.resteasy.model.User;
import org.eclipse.microprofile.jwt.Claims;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

@Path("/auth")
public class AuthResources {
    @POST
    @Path("/signIn")
    @Consumes("application/x-www-form-urlencoded")
    public Response auth(@FormParam("username") String username, @FormParam("password") String password) {
        try {
            Optional<User> optionalUser = User
                    .find("username = ?1", username)
                    .singleResultOptional();
            User user = optionalUser
                    .orElseThrow((Supplier<Throwable>) () -> new RuntimeException("User Not Found"));
            if(BCrypt.checkpw(password, user.getPassword())) {
                return Response.ok().header("Authorization",
                        Jwt
                                .issuer("bruno.al")
                                .upn(user.getEmail())
                                .expiresAt(Instant.ofEpochMilli(3600000))
                                .issuedAt(Instant.now())
                                .groups(transform(user.getAuthorities()))
                                .claim(Claims.birthdate.name(), user.getEmploymentDate().format(DateTimeFormatter.ISO_DATE_TIME))
                                .preferredUserName(user.getUsername())
                                .sign()
                ).build();
            }
            return Response.status(401, "Username or Password is Incorrect").build();
        } catch (Throwable throwable) {
            return Response.status(401, throwable.getMessage()).build();
        }
    }

    private Set<String>transform(Collection<Authority> authorities) {
        Set<String> authority = new HashSet<>();
        for (Authority a : authorities) {
            authority.add(a.getAuthority());
        }
        return authority;
    }
}
