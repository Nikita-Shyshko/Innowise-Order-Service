package org.example.service;

import org.example.dto.UserDTO;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceClient>
{
    @Override
    public UserServiceClient create(Throwable cause)
    {
        return new UserServiceClient()
        {
            @Override
            public UserDTO getUserById(Long id)
            {
                UserDTO fallbackUser = new UserDTO();
                fallbackUser.setId(id);
                fallbackUser.setName("Unknown");
                fallbackUser.setSurname("Unknown");
                fallbackUser.setEmail("unknown@example.com");
                fallbackUser.setActive("UNKNOWN");
                return fallbackUser;
            }
        };
    }
}