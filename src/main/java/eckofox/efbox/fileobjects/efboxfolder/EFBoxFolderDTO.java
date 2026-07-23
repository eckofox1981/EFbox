package eckofox.efbox.fileobjects.efboxfolder;

import eckofox.efbox.fileobjects.efboxfile.EFBoxFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Data
public class EFBoxFolderDTO {
    private UUID folderID;
    private String name;
    private String parentID;
    private List<UUID> folderIDs;
    private HashMap<UUID, String> fileHashes;

    /**
     * converts model to DTO
     *
     * @param folder to be converted
     * @return folder dto
     */
    public static EFBoxFolderDTO fromEFBoxFolder(EFBoxFolder folder) {
        EFBoxFolder parent = folder.getParentFolder();

        return new EFBoxFolderDTO(
                folder.getFolderID(),
                folder.getName(),
                parent == null ? "" : parent.getFolderID().toString(),
                folder.getFolders()
                        .stream()
                        .map(EFBoxFolder::getFolderID)
                        .toList(),
                (HashMap<UUID, String>) folder.getFiles()
                        .stream()
                        .collect(Collectors.toMap(
                                EFBoxFile::getFileID,
                                EFBoxFile::getFileName
                        ))
        );
    }
}
