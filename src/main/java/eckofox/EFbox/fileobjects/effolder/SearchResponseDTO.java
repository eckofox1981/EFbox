package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFileDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@NoArgsConstructor
@Data
public class SearchResponseDTO {
    private Collection<EFFolderDTO> folders = new ArrayList<>();
    private Collection<EFFileDTO> files = new ArrayList<>();
}
