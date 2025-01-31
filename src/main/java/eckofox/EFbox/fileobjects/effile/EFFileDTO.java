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

    /**
     * converts model to dto
     * @param efFile to be converted
     * @return dto
     */
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
