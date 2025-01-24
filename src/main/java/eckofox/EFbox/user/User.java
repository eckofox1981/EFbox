package eckofox.EFbox.user;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "efbox_users")
@NoArgsConstructor(force = true)
@Data
public class User implements UserDetails {
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
    @OneToMany(mappedBy = "user")
    private List<EFFolder> folders;

    public User(UUID userID, String username, String firstName, String lastName, String password) {
        this.userID = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.folders = new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
