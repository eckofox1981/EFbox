package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFile;
import eckofox.EFbox.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "folders")
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class EFFolder {
    @Id
    private final UUID folderID;
    @Column
    private final String name;
    @ManyToOne
    @JoinColumn(name = "parent_folderID")
    private EFFolder parentFolder;
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EFFolder> folder;
    @OneToMany(mappedBy = "fileID", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EFFile> files;
    @ManyToOne
    @JoinColumn(name = "userID")
    private final User user;
}


