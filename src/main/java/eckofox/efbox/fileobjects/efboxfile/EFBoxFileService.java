package eckofox.efbox.fileobjects.efboxfile;

import eckofox.efbox.fileobjects.efboxfolder.EFBoxFolder;
import eckofox.efbox.fileobjects.efboxfolder.EFBoxFolderRepository;
import eckofox.efbox.fileobjects.efboxfolder.EFBoxFolderService;
import eckofox.efbox.logger.LogEventType;
import eckofox.efbox.logger.LoggerService;
import eckofox.efbox.user.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.rmi.AccessException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class EFBoxFileService {
    private final EFBoxFileRepository fileRepository;
    private final EFBoxFolderRepository folderRepository;
    private final EFBoxFolderService folderService;
    private final LoggerService loggerService;

    /**
     * first the method checks if the user is the parent folder's owner.
     * After (if) it is established, the file content is added to the new EfBoxFile and saved in the database.
     *
     * @param file           to be uploaded
     * @param user           to check access rights
     * @param parentFolderID where file will be saved and to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessException(through IOException), IOException
     */
    public EFBoxFile uploadFile(MultipartFile file, User user, String parentFolderID)
            throws NoSuchElementException, IOException {
        EFBoxFolder parentFolder = folderRepository
                .findById(UUID.fromString(parentFolderID))
                .orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (!user.getUserID().equals(parentFolder.getUser().getUserID())) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + parentFolder.getName()
                            + "/"
                            + parentFolder.getFolderID());
        }


        EFBoxFile efBoxFile = new EFBoxFile(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes(), file.getContentType(), parentFolder);
        fileRepository.save(efBoxFile);

        loggerService.saveInfoLogg(LogEventType.INFO_FILE, "File uploaded. \n" + efBoxFile.getFileID(), user);

        return efBoxFile;

    }

    /**
     * looks for fileID in database then checks parentFolder is accessible by user. If the check passes it sends the
     * EFBoxFile back to the controller.
     *
     * @param fileID to find file
     * @param user   to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessException
     */
    public EFBoxFile getFile(String fileID, User user) throws NoSuchElementException, AccessException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!user.getUserID().equals(efBoxFile.getParentFolder().getUser().getUserID())) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal access attempt on file "
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        loggerService.saveInfoLogg(LogEventType.INFO_FILE, "File downloaded. \n" + efBoxFile.getFileID(), user);

        return efBoxFile;
    }

    /**
     * checks if parent folder is owned by user before deleting the file from the database
     *
     * @param fileID to find file
     * @param user   to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessException
     */
    public EFBoxFile deleteFile(String fileID, User user) throws NoSuchElementException, AccessException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found"));

        if (!efBoxFile.getParentFolder().getUser().getUserID().equals(user.getUserID())) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal deletion attempt on file"
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        efBoxFile.getParentFolder().getFiles().remove(efBoxFile);
        fileRepository.delete(efBoxFile);

        loggerService.saveInfoLogg(LogEventType.INFO_FILE, "File deleted. \n" + efBoxFile.getFileID(), user);

        return efBoxFile;
    }

    /**
     * checks if parent folder is owned by user before changing the name of the EFBoxFile and updating it in the database
     *
     * @param fileID  to find file
     * @param newName self-explanatory
     * @param user    to check access rights
     * @return updated EFBoxFile
     * @throws AccessException, AccessException
     */
    public EFBoxFile changeFileName(String fileID, String newName, User user)
            throws NoSuchElementException, AccessException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        if (folderService.userIsNotFolderOwner(efBoxFile.getParentFolder(), user)) {
            throw new AccessException(
                    user.getUsername()
                            + ": illegal renaming attempt on file"
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        efBoxFile.setFilename(newName);

        EFBoxFile fileWithNewName = fileRepository.save(efBoxFile);

        loggerService.saveInfoLogg(LogEventType.INFO_FILE, "File named changed. \n" + efBoxFile.getFileID(), user);

        return fileWithNewName;
    }
}
