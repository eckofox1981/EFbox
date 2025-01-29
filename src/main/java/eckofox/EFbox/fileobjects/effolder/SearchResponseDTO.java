package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFileDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
@NoArgsConstructor
@Data
public class SearchResponseDTO {
    private Collection<EFFolderDTO> folders = new ArrayList<>();
    private Collection<EFFileDTO> files = new ArrayList<>();
}
