package eckofox.EFbox.fileobjects.effile;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class EFFileDTO {
    private UUID fileID;
    private String filename;
    private byte[] content;
    private String type;
    private String parentFolder;

    public static EFFileDTO fromEFFile(EFFile efFile) {
        return new EFFileDTO(
                efFile.getFileID(),
                efFile.getFileName(),
                efFile.getContent(),
                efFile.getType(),
                efFile.getParentFolder().getName()
        );
    }
}
