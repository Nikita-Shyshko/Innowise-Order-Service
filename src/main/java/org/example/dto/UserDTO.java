    package org.example.dto;

    import lombok.Getter;
    import lombok.Setter;
    import java.util.Date;

    @Getter
    @Setter
    public class UserDTO
    {
        private Long id;
        private String name;
        private String surname;
        private String email;
        private String active;
        private Date createdAt;
        private Date updatedAt;
    }