package eckofox.EFbox.user;

import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    private final UUID userID;
    @Column(unique = true)
    private final String username;
    @Column
    private final String firstName;
    @Column
    private final String lastName;
    @Column
    private final String password;
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true) //makes sure no files are left if not owned (ex. user removal)
    private final List<EFBoxFolder> rootFolder;
    @Column
    private final List<UserRole> roles;
    @Column
    private final List<GrantedAuthorities> grantedAuthorities;

    public User(
            UUID userID,
            String username,
            String firstName,
            String lastName,
            String password,
            List<UserRole> roles,
            List<GrantedAuthorities> grantedAuthorities
            ) {
        this.userID = userID;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.rootFolder = new ArrayList<>();
        this.roles = roles;
        this.grantedAuthorities = grantedAuthorities;
    }

    //adapted from https://www.javaguides.net/2024/04/spring-security-granted-authority.html
    // and https://www.baeldung.com/role-and-privilege-for-spring-security-registration
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getGrantedAuthorities(getPrivileges());
    }


    private List<String> getPrivileges() {
        List<String> authorities = new ArrayList<>();
        for (UserRole role : getRoles()) {
            authorities.add(role.toString());
        }
        List<GrantedAuthorities> authorityList = new ArrayList<>(getGrantedAuthorities());
        for (GrantedAuthorities item : authorityList) {
            authorities.add(item.toString());
        }
        return authorities;
    }

    private List<GrantedAuthority> getGrantedAuthorities(List<String> grantedAuthorities) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (String authority : grantedAuthorities) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        return authorities;
    }
}
