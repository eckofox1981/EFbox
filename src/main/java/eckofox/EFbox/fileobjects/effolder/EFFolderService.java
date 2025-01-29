package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EFFolderService {
    private EFFolderRepository folderRespository;
    private UserService userService;
    private UserRepository userRepository;

    public String createFolder(String folderName, User user, String parentFolderID) throws IllegalAccessException, NoSuchElementException {
        if (parentFolderID.equals("0")) {
            EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, user);
            folderRespository.save(folder);
            return folder.getName() + " created at root level";
        }

        EFFolder parentFolder = folderRespository.findById(UUID.fromString(parentFolderID)).orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (userIsNotFolderOwner(parentFolder, user)){
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, parentFolder, user);
        folderRespository.save(folder);

        return "Folder \"" + folder.getName() + "\" saved in parent folder \"" + parentFolder.getName() + "\"";
    }

    public EFFolderDTO seeFolderContent(String folderID, User user) throws IllegalAccessException {
        EFFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("Folder not found"));
        if (userIsNotFolderOwner(folder, user)){
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        return EFFolderDTO.fromEFFolder(folder);
    }

    public void searchInAllFolders(String query, User user) {

    }

    private void searchInFolder(EFFolder folder, String query) {

    }

    private boolean userIsNotFolderOwner(EFFolder folder, User user) {
        if (folder.getUser().getUserID().equals(user.getUserID())) {
            return false;
        }
        return true;
    }
}
