package eckofox.efbox.fileobjects.efboxfolder;

import eckofox.efbox.exception.IllegalRegexException;
import eckofox.efbox.fileobjects.efboxfile.EFBoxFile;
import eckofox.efbox.fileobjects.efboxfile.EFBoxFileDTO;
import eckofox.efbox.fileobjects.efboxfile.EFBoxFileRepository;
import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.security.validation.InputValidationService;
import eckofox.efbox.security.validation.Validation;
import eckofox.efbox.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.rmi.AccessException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class EFBoxFolderService {
    private final EFBoxFolderRepository folderRespository;
    private final EFBoxFileRepository fileRepository;
    private final LoggerService loggerService;
    private final InputValidationService inputValidationService;

    /**
     * creates EFBoxFolder, if in "root folder" (User.EFBoxFolder.rootfolder) the parentID is set to "0" and the folder is
     * created directly in the arrayList. parent_folderid will show blank in database.
     *
     * @param folderName     to be set
     * @param user           if root folder -> to add the folder to User.EFBoxFolder.rootfolder else -> check for access rights
     * @param parentFolderID -> to set parent folder (set to 0 if root, the list is initialized during user account creation)
     * @return created folder
     */
    public EFBoxFolder createFolder(String folderName, User user, String parentFolderID)
            throws NoSuchElementException, AccessException {
        validateUserInput(folderName);

        if (parentFolderID.equals("0")) {
            EFBoxFolder folder = new EFBoxFolder(UUID.randomUUID(), folderName, user);
            return folderRespository.save(folder);
        }

        EFBoxFolder parentFolder = folderRespository
                .findById(UUID.fromString(parentFolderID))
                .orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (userIsNotFolderOwner(parentFolder, user)) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + parentFolder.getName()
                            + "/"
                            + parentFolder.getFolderID());
        }

        EFBoxFolder folder = new EFBoxFolder(UUID.randomUUID(), folderName, parentFolder, user);

        loggerService.saveInfoLogg(LogEventType.INFO_FOLDER, "Folder created. \n" + folder.getFolderID(), user);

        return folderRespository.save(folder);
    }

    /**
     * show content of folder (EFBoxFolders or EFBoxFiles)
     *
     * @param folderID of folder to be shown
     * @param user     to check access rights
     * @return folder dto
     */
    public EFBoxFolder seeFolderContent(String folderID, User user)
            throws AccessException, NoSuchElementException {
        EFBoxFolder folder = folderRespository
                .findById(UUID.fromString(folderID))
                .orElseThrow(() -> new NoSuchElementException("Folder not found"));

        if (userIsNotFolderOwner(folder, user)) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + folder.getName()
                            + "/"
                            + folder.getFolderID());
        }

        loggerService.saveInfoLogg(LogEventType.INFO_FOLDER, "Folder accessed. \n" + folder.getFolderID(), user);

        return folder;
    }

    /**
     * search in database based on the query string
     *
     * @param query used for the search
     * @param user  to check if found files/folders belong to the user making the requests
     * @return searchresponseDTO (list of folder and list of files)
     */
    public SearchResponseDTO searchInAllFolders(String query, User user) {
        validateUserInput(query);

        Collection<EFBoxFolder> folders = folderRespository
                .findByNameContainingIgnoreCaseWithUserID(query, user.getUserID())
                .orElse(new ArrayList<>());
        Collection<EFBoxFile> files = fileRepository
                .findByFilenameContainingIgnoreCaseWithUserID(query, user.getUserID())
                .orElse(new ArrayList<>());

        SearchResponseDTO responseDTO = new SearchResponseDTO();
        folders.stream()
                .map(EFBoxFolderDTO::fromEFBoxFolder)
                .forEach(folder -> responseDTO.getFolders().add(folder));
        files.stream()
                .map(EFBoxFileDTO::fromEFBoxFile)
                .forEach(file -> responseDTO.getFiles().add(file));

        loggerService.saveInfoLogg(LogEventType.INFO_FOLDER, "Search performed.", user);

        return responseDTO;
    }

    /**
     * since Hibernate wouldn't take into account my cascading settings in EFBoxFolder anymore and I wasn't able to
     * debug the issue I made a recursive deletion of the Folders and their content from the bottom up
     * (see recursiveDeletionOfFolders) folder ownership is checked before calling for folder deletion, but it is also
     * checked at repository level through a custom query.
     *
     * @param folderID of folder to be deleted
     * @param user     to check access rights
     * @return message
     */
    public EFBoxFolder deleteFolder(String folderID, User user)
            throws AccessException, NoSuchElementException {
        EFBoxFolder folder = folderRespository
                .findById(UUID.fromString(folderID))
                .orElseThrow(() -> new NoSuchElementException("Folder not found"));

        if (!folder.getUser().getUserID().equals(user.getUserID())) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + folder.getName()
                            + "/"
                            + folder.getFolderID());
        }

        recursiveDeletionOfFolders(folder, user);

        loggerService.saveInfoLogg(LogEventType.INFO_FOLDER, "Folder deleted. \n" + folder.getFolderID(), user);

        return folder;
    }

    /**
     * looks for the folder in the database then checks ownership.
     * When checks are passed it changes the name and updates
     * the database.
     *
     * @param folderID to find folder
     * @param newName  self-explanatory
     * @param user     to check for access-right
     * @return updated folder dto
     */
    public EFBoxFolder changeFolderName(String folderID, String newName, User user)
            throws NoSuchElementException, AccessException {
        validateUserInput(newName);

        EFBoxFolder folder = folderRespository
                .findById(UUID.fromString(folderID))
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        if (userIsNotFolderOwner(folder, user)) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + folder.getName()
                            + "/"
                            + folder.getFolderID());
        }

        folder.setName(newName);

        EFBoxFolder folderWithNewName = folderRespository.save(folder);

        loggerService.saveInfoLogg(LogEventType.INFO_FOLDER, "Folder deleted. \n" + folder.getFolderID(), user);

        return folderWithNewName;
    }

    /**
     * checks user access right to folder. Made public since accessed in EFBoxFileService (through injection) where the
     * parent folder is checked before accessing a file. "Inverted boolean" since more logical when using the method
     * see usage line 118 above
     *
     * @param folder of user
     * @param user   of folder
     * @return boolean
     */
    public boolean userIsNotFolderOwner(EFBoxFolder folder, User user) {
        return !folder.getUser().getUserID().equals(user.getUserID());
    }

    /**
     * checks if the folder has a folder and if it has it sends the folder back to itself to check again
     * Eventually, no folder will be found and the folder and its files are deleted instead.
     * Then it goes back to its "parent method" which will continue deleting the files and then the initial folder.
     *
     * @param folder to be deleted (with its content)
     */
    private void recursiveDeletionOfFolders(EFBoxFolder folder, User user) {
        if (!folder.getFolders().isEmpty()) {
            for (EFBoxFolder subFolder : folder.getFolders()) {
                recursiveDeletionOfFolders(subFolder, user);
            }

        }

        folder.getFiles().forEach(fileRepository::delete);
        folderRespository.customFolderDeletion(folder.getFolderID(), user.getUserID());
    }

    private void validateUserInput(String input) {
        Validation validation = inputValidationService.isUserInputValidated(input);
        switch (validation) {
            case Validation.SQL_INJECTION_SUSPECTED, Validation.OTHER_INJECTION_SUSPECTED
                    -> throw new IllegalRegexException(validation + ": " + input);
            case Validation.NOT_AUTHORIZED ->
                    throw new IllegalRegexException(validation + ": not shared for user privacy");
        }
    }
}
