package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFile;
import eckofox.EFbox.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity(name = "folders")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class EFFolder {
    @Id
    private UUID folderID;
    @Column
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_folderID")
    private EFFolder parentFolder;
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EFFolder> folders;
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EFFile> files;
    @ManyToOne
    private User user;

    public EFFolder(UUID folderID, String name, User user) {
        this.folderID = folderID;
        this.name = name;
        this.user = user;
    }

    public EFFolder(UUID folderID, String name, EFFolder parentFolder, User user) {
        this.folderID = folderID;
        this.name = name;
        this.parentFolder = parentFolder;
        this.user = user;
    }

    public String getName(EFFolder efFolder) {
        return efFolder.getName();
    }
}


