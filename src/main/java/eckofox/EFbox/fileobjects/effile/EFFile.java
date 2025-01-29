package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity(name = "files")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class EFFile {
    @Id
    private UUID fileID;
    @Column
    private String filename;
    @Lob
    @Column(name = "content")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] content;
    @Column
    private String type;
    @ManyToOne
    private EFFolder parentFolder;

    public String getFileName() {
        return filename;
    }
}
