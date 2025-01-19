package eckofox.EFbox.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "api_users")
@RequiredArgsConstructor
@Data
public class User {
    @Id
    private final UUID userID;
    @Column
    private final String username;
    @Column
    private final String firstName;
    @Column
    private final String lastName;
    @Column
    private final String password;
    @OneToMany
    private final List<?> folders;
}
