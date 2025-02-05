package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.fileobjects.efboxfile.EFBoxFileDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@NoArgsConstructor
@Data
public class SearchResponseDTO {
    private Collection<EFBoxFolderDTO> folders = new ArrayList<>();
    private Collection<EFBoxFileDTO> files = new ArrayList<>();
}
