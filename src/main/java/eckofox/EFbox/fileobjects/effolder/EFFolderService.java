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

    /**
     * creates EFFolder, if in "root folder" (User.EFFolder.rootfolder) the parentID is set to "0" and the folder is
     * created directly in the arrayList. parent_folderid will show blank in database.
     * @param folderName to be set
     * @param user if root folder -> to add the folder to User.EFFolder.rootfolder else -> check for access rights
     * @param parentFolderID -> to set parent folder
     * @return message
     * @throws IllegalAccessException
     * @throws NoSuchElementException
     */
    public String createFolder(String folderName, User user, String parentFolderID) throws IllegalAccessException, NoSuchElementException {
        if (parentFolderID.equals("0")) {
            EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, user);
            folderRespository.save(folder);
            return folder.getName() + " created at root level";
        }

        EFFolder parentFolder = folderRespository.findById(UUID.fromString(parentFolderID)).orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (userIsNotFolderOwner(parentFolder, user)) {
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        EFFolder folder = new EFFolder(UUID.randomUUID(), folderName, parentFolder, user);
        folderRespository.save(folder);

        return "Folder \"" + folder.getName() + "\" saved in parent folder \"" + parentFolder.getName() + "\"";
    }

    /**
     * show content of folder (EFFolders or EFFIles)
     * @param folderID of folder to be shown
     * @param user to check access rights
     * @return folder dto
     * @throws IllegalAccessException
     */
    public EFFolderDTO seeFolderContent(String folderID, User user) throws IllegalAccessException {
        EFFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("Folder not found"));
        if (userIsNotFolderOwner(folder, user)) {
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        return EFFolderDTO.fromEFFolder(folder);
    }

    /**
     * search in database based on the query string
     * @param query used for the search
     * @param user to check if found files/folders belong to the user making the requests
     * @return searchresponseDTO (list of folder and list of files)
     */
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

    /**
     * deletes a folder and it's content
     * @param folderID of folder to be deleted
     * @param user to check access rights
     * @return message
     * @throws IllegalAccessException
     */
    public String deleteFolder(String folderID, User user) throws IllegalAccessException {
        EFFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("Folder not found"));
        if (!folder.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to delete this folder");
        }
        String folderName = folder.getName();
        folder.getParentFolder().getFolders().remove(folder);
        folderRespository.delete(folder);
        return "Folder \"" + folderName + "\" deleted.";
    }

    /**
     * changes the name of the folder
     * @param folderID to find folder
     * @param newName self-explanatory
     * @param user to check for access-right
     * @return updated folder dto
     * @throws Exception
     */
    public EFFolderDTO changeFolderName(String folderID, String newName, User user) throws Exception {
        EFFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("File not found."));
        if (userIsNotFolderOwner(folder, user)) {
            throw new IllegalAccessException("You are not allowed to access this file");
        }
        folder.setName(newName);
        folderRespository.save(folder);
        return EFFolderDTO.fromEFFolder(folder);
    }

    /**
     * checks user access right to folder. Made public since accessed in EFFileService (through injection) where the
     * parent folder is checked before accessing a file. "Inverted boolean" since more logical that way when using the method
     * see usage line 116 above
     * @param folder of user
     * @param user of folder
     * @return boolean
     */
    public boolean userIsNotFolderOwner(EFFolder folder, User user) {
        return !folder.getUser().getUserID().equals(user.getUserID());
    }
}
