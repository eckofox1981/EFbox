package eckofox.EFbox.fileobjects.effolder;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class EFFolderController {
    private EFFolderService folderService;
}

@AllArgsConstructor
@Data
class EFFolderDTO {
    private UUID folderID;
    private String name;
    private List<EFFolder> folder;
}
