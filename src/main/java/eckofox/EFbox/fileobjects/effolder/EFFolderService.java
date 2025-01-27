package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class EFFolderService {
    private EFFolderRepository folderRespository;
    private UserService userService;
    private UserRepository userRepository;

    public String createFolder(String folderName, User user, String parentFolderID) {
        if (parentFolderID.equals("0")) {
            EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, user);
            folderRespository.save(folder);
            return folder.getName() + " created in on firstlevel";
        }

        EFFolder parentFolder = folderRespository.findById(UUID.fromString(parentFolderID)).orElseThrow();
        EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, parentFolder, user);
        //System.out.println("DEBUG createfolder, parentfolderID:" + folder.getParentFolder().getFolderID());
        folderRespository.save(folder);

        return "Folder \"" + folder.getName() + "\" saved in parent folder \"" + parentFolder.getName() + "\"";
    }
}
