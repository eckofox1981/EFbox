package eckofox.EFbox.fileobjects.efboxfile;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class EFBoxFileDTO {
    private UUID fileID;
    private String filename;
    private byte[] content;
    private String type;
    private String parentFolder;

    /**
     * converts model to dto
     *
     * @param efBoxFile to be converted
     * @return dto
     */
    public static EFBoxFileDTO fromEFBoxFile(EFBoxFile efBoxFile) {
        return new EFBoxFileDTO(
                efBoxFile.getFileID(),
                efBoxFile.getFileName(),
                efBoxFile.getContent(),
                efBoxFile.getType(),
                efBoxFile.getParentFolder().getName()
        );
    }
}
