package eckofox.EFbox.fileobjects.effolder;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;

@AllArgsConstructor
@Data
public class SearchResponseDTO {
    private HashMap<String, String> folderAndParentFolder;
    private HashMap<String, String> fileAndParentfolder;
}
