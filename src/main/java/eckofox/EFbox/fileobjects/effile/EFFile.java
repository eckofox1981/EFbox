package eckofox.EFbox.fileobjects.effile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
public class EFFile {
    @Id
    private final UUID fileID;
    @Column
    private final String name;
    @Lob
    private final byte[] content;

}
