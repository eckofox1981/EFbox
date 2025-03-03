package eckofox.EFbox.user;

import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "efbox_users")
@NoArgsConstructor(force = true)
@Getter
@Setter
public class User implements UserDetails {
    @Id
    private UUID userID;
    @Column(unique = true)
    private String username;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String password;
    @Column
    private String openIDconnectID = null;
    @Column
    private String openIDconnectProvider = null;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true) //makes sure no files are left if not owned (ex. user removal)
    private final List<EFBoxFolder> RootFolder;

    public User(UUID userID, String username, String firstName, String lastName, String password) {
        this.userID = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.RootFolder = new ArrayList<>();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }
}
