package org.acme.resteasy.resource;

import org.acme.resteasy.dto.UserDto;
import org.acme.resteasy.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.ZonedDateTime;
import java.util.List;

@Path("/user")
public class UserResource {

    /**
     * https://docs.jboss.org/resteasy/docs/2.3.3.Final/userguide/html_single/index.html
     */

    @RolesAllowed({"EMPLOYEE"})
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<User> users() {
        return User.listAll();
    }

    @POST
    @Transactional
    public Response create(UserDto userDto) {
        User.persist(
                new User(
                        0,
                        userDto.getFirstName(),
                        userDto.getLastName(),
                        userDto.getUsername(),
                        BCrypt.hashpw("password", BCrypt.gensalt(12)),
                        userDto.getEmail(),
                        userDto.getAddress(),
                        userDto.getPhone(),
                        userDto.getAuthorities(),
                        ZonedDateTime.now(),
                        true
                )
        );
        return Response.ok().build();
    }
}