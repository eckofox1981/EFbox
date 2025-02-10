package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.fileobjects.efboxfile.EFBoxFile;
import eckofox.EFbox.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "folders")
@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor
public class EFBoxFolder {
    @Id
    private UUID folderID;
    @Column
    private String name;
    @ManyToOne
    @JoinColumn(name = "parent_folderID")
    private EFBoxFolder parentFolder;
    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<EFBoxFolder> folders;
    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<EFBoxFile> files;
    @ManyToOne
    private User user;

    public EFBoxFolder(UUID folderID, String name, User user) {
        this.folderID = folderID;
        this.name = name;
        this.user = user;
        this.folders = new ArrayList<>();
        this.files = new ArrayList<>();
    }

    public EFBoxFolder(UUID folderID, String name, EFBoxFolder parentFolder, User user) {
        this.folderID = folderID;
        this.name = name;
        this.parentFolder = parentFolder;
        this.user = user;
        this.folders = new ArrayList<>();
        this.files = new ArrayList<>();
    }

}


