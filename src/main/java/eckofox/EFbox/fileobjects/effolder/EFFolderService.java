package eckofox.EFbox.fileobjects.effolder;

import eckofox.EFbox.fileobjects.effile.EFFile;
import eckofox.EFbox.fileobjects.effile.EFFileDTO;
import eckofox.EFbox.fileobjects.effile.EFFileRepository;
import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class EFFolderService {
    private EFFolderRepository folderRespository;
    private EFFileRepository fileRepository;
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

    public SearchResponseDTO searchInAllFolders(String query, User user) {
        Collection<EFFolder> folders = folderRespository.findByNameContainingIgnoreCaseWithUserID(query, user.getUserID()).orElse(new ArrayList<>());
        Collection<EFFile> files = fileRepository.findByFilenameContainingIgnoreCaseWithUserID(query).orElse(new ArrayList<>());

        SearchResponseDTO responseDTO = new SearchResponseDTO();
        folders.stream()
                .map(EFFolderDTO::fromEFFolder)
                .forEach(folder -> responseDTO.getFolders().add(folder));
        files.stream()
                .filter(file -> userIsNotFolderOwner(file.getParentFolder(), user)) //since files aren't directly connected to their user
                .map(EFFileDTO::fromEFFile)
                .forEach(file -> responseDTO.getFiles().add(file));
        return responseDTO;
    }

    public String deleteFolder(String folderID, User user) throws IllegalAccessException {
        EFFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(()-> new NoSuchElementException("Folder not found"));
        if (!folder.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to delete this folder");
        }
        String folderName = folder.getName();
        folder.getParentFolder().getFolders().remove(folder);
        folderRespository.delete(folder);
        return "Folder \"" + folderName + "\" deleted.";
    }


    public boolean userIsNotFolderOwner(EFFolder folder, User user) {
        return !folder.getUser().getUserID().equals(user.getUserID());
    }
}
