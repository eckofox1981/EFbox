package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFile;
import eckofox.EFbox.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
public class EFFolder {
    @Id
    private UUID folderID;
    @Column
    private final String name;
    @OneToMany(mappedBy = "name")
    private final List<EFFile> file = new ArrayList<>();
    @ManyToOne
    private User user;
}
