package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Data
public class EFFolderDTO {
    private UUID folderID;
    private String name;
    private List<String> folderNames;
    private List<String> fileNames;
    
    public static EFFolderDTO fromEFFolder(EFFolder folder) {
        return new EFFolderDTO(
                folder.getFolderID(),
                folder.getName(),
                folder.getFolders()
                        .stream()
                        .map(EFFolder::getName)
                        .toList(),
                folder.getFiles()
                        .stream()
                        .map(EFFile::getFileName)
                        .toList()
        );
    }
}
