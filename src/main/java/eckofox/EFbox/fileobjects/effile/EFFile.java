package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "files")
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class EFFile {
    @Id
    private final UUID fileID;
    @Column
    private final String filename;
    @Lob
    private final byte[] content;
    @Column
    private final String type;
    @ManyToOne
    private final EFFolder parentFolder;
}
