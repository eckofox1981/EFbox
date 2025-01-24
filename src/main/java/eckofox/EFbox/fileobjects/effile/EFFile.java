package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Entity(name = "files")
@Data
@AllArgsConstructor
public class EFFile {
    @Id
    private final UUID fileID;
    @Column
    private final String filename;
    @Lob
    private final byte[] content;
    @ManyToOne
    @JoinColumn(name = "parent_folderID")
    private EFFolder parentFolder;
}
