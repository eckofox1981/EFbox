package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.fileobjects.efboxfile.EFBoxFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class EFBoxFolderDTO {
    private UUID folderID;
    private String name;
    private List<String> folderNames;
    private List<String> fileNames;

    /**
     * converts model to DTO
     *
     * @param folder to be converted
     * @return folder dto
     */
    public static EFBoxFolderDTO fromEFBoxFolder(EFBoxFolder folder) {
        return new EFBoxFolderDTO(
                folder.getFolderID(),
                folder.getName(),
                folder.getFolders()
                        .stream()
                        .map(EFBoxFolder::getName)
                        .toList(),
                folder.getFiles()
                        .stream()
                        .map(EFBoxFile::getFileName)
                        .toList()
        );
    }
}
