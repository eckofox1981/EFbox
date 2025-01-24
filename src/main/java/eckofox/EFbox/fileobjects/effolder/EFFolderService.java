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
    private EFFolderRespository folderRespository;
    private UserService userService;
    private UserRepository userRepository;

    public String createFolder(String name, String token, String parentFolderID) {
        String userID = userService.verifyAuthentication(token).get().getUserID().toString();
        User user = userRepository.findById(UUID.fromString(userID)).orElseThrow();
        EFFolder folder = new EFFolder(UUID.randomUUID(), name, user);
        if (parentFolderID.isBlank()) {
            user.getFolders().add(folder);
            userRepository.save(user);
            return "Folder \"" + folder.getName() + "\" saved in \"Main folder\"";
        }
        EFFolder parentFolder = folderRespository.findById(UUID.fromString(parentFolderID)).orElseThrow();
        parentFolder.getFolder().add(folder);
        folderRespository.save(parentFolder);

        return "Folder \"" + folder.getName() + "\" saved in parent folder \"" + parentFolder.getName() + "\"";
    }
}
