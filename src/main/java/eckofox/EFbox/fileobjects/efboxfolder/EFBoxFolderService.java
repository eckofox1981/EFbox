package eckofox.EFbox.fileobjects.efboxfolder;

import eckofox.EFbox.fileobjects.efboxfile.EFBoxFile;
import eckofox.EFbox.fileobjects.efboxfile.EFBoxFileDTO;
import eckofox.EFbox.fileobjects.efboxfile.EFBoxFileRepository;
import eckofox.EFbox.user.User;
import eckofox.EFbox.user.UserRepository;
import eckofox.EFbox.user.UserService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class EFBoxFolderService {
    private EFBoxFolderRepository folderRespository;
    private EFBoxFileRepository fileRepository;
    private UserService userService;
    private UserRepository userRepository;

    /**
     * creates EFBoxFolder, if in "root folder" (User.EFBoxFolder.rootfolder) the parentID is set to "0" and the folder is
     * created directly in the arrayList. parent_folderid will show blank in database.
     * @param folderName to be set
     * @param user if root folder -> to add the folder to User.EFBoxFolder.rootfolder else -> check for access rights
     * @param parentFolderID -> to set parent folder (set to 0 if root, the list is initialized during user account creation)
     * @return created folder
     * @throws IllegalAccessException
     * @throws NoSuchElementException
     */
    public EFBoxFolder createFolder(String folderName, User user, String parentFolderID) throws IllegalAccessException, NoSuchElementException {
        if (parentFolderID.equals("0")) {
            EFBoxFolder folder = new EFBoxFolder(UUID.randomUUID(), folderName, user);
            return folderRespository.save(folder);
        }

        EFBoxFolder parentFolder = folderRespository.findById(UUID.fromString(parentFolderID)).orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (userIsNotFolderOwner(parentFolder, user)) {
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        EFBoxFolder folder = new EFBoxFolder(UUID.randomUUID(), folderName, parentFolder, user);


        return folderRespository.save(folder);
    }

    /**
     * show content of folder (EFBoxFolders or EFBoxFiles)
     * @param folderID of folder to be shown
     * @param user to check access rights
     * @return folder dto
     * @throws IllegalAccessException
     */
    public EFBoxFolder seeFolderContent(String folderID, User user) throws IllegalAccessException {
        EFBoxFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("Folder not found"));
        if (userIsNotFolderOwner(folder, user)) {
            throw new IllegalAccessException("You are not authorized to create this folder here.");
        }

        return folder;
    }

    /**
     * search in database based on the query string
     * @param query used for the search
     * @param user to check if found files/folders belong to the user making the requests
     * @return searchresponseDTO (list of folder and list of files)
     */
    public SearchResponseDTO searchInAllFolders(String query, User user) {
        Collection<EFBoxFolder> folders = folderRespository.findByNameContainingIgnoreCaseWithUserID(query, user.getUserID()).orElse(new ArrayList<>());
        Collection<EFBoxFile> files = fileRepository.findByFilenameContainingIgnoreCaseWithUserID(query).orElse(new ArrayList<>());

        SearchResponseDTO responseDTO = new SearchResponseDTO();
        folders.stream()
                .map(EFBoxFolderDTO::fromEFBoxFolder)
                .forEach(folder -> responseDTO.getFolders().add(folder));
        files.stream()
                .filter(file -> userIsNotFolderOwner(file.getParentFolder(), user)) //since files aren't directly connected to their user
                .map(EFBoxFileDTO::fromEFBoxFile)
                .forEach(file -> responseDTO.getFiles().add(file));
        return responseDTO;
    }

    /**
     * deletes a folder and it's content
     * since Hibernate wouldn't take into account my cascading settings in EFBoxFolder and I wasn't able to debug the issue
     * I made a recursive deletion of the Folders from the bottom up (see recursiveDeletionOfFolders)
     * @param folderID of folder to be deleted
     * @param user to check access rights
     * @return message
     * @throws IllegalAccessException
     */
    public EFBoxFolder deleteFolder(String folderID, User user) throws IllegalAccessException {
        EFBoxFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("Folder not found"));
        if (!folder.getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not allowed to delete this folder");
        }

        recursiveDeletionOfFolders(folder);

        return folder;
    }

    /**
     * looks for the folder in the database and if found changes its name
     * @param folderID to find folder
     * @param newName self-explanatory
     * @param user to check for access-right
     * @return updated folder dto
     * @throws Exception
     */
    public EFBoxFolder changeFolderName(String folderID, String newName, User user) throws Exception {
        EFBoxFolder folder = folderRespository.findById(UUID.fromString(folderID)).orElseThrow(() -> new NoSuchElementException("File not found."));
        if (userIsNotFolderOwner(folder, user)) {
            throw new IllegalAccessException("You are not allowed to access this file");
        }

        folder.setName(newName);
        return folderRespository.save(folder);
    }

    /**
     * checks user access right to folder. Made public since accessed in EFBoxFileService (through injection) where the
     * parent folder is checked before accessing a file. "Inverted boolean" since more logical that way when using the method
     * see usage line 116 above
     * @param folder of user
     * @param user of folder
     * @return boolean
     */
    public boolean userIsNotFolderOwner(EFBoxFolder folder, User user) {
        return !folder.getUser().getUserID().equals(user.getUserID());
    }

    /**
     * checks if the folder has a folder and if it has it sends the folder back to itself to check again
     * Eventually, no folder will found and the folder is instead deleted going back to its "parent method" which will
     * continue deleting the initial folder.
     * @param folder
     */
    private void recursiveDeletionOfFolders(EFBoxFolder folder) {
        if (!folder.getFolders().isEmpty()) {
            for (EFBoxFolder subFolder : folder.getFolders()) {
                recursiveDeletionOfFolders(subFolder);
            }
        }
        folderRespository.customFolderDeletion(folder.getFolderID());
    }
}
