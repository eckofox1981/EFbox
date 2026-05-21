package eckofox.EFbox.fileobjects.efboxfile;

import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolderRepository;
import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolderService;
import eckofox.EFbox.user.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class EFBoxFileService {
    private final EFBoxFileRepository fileRepository;
    private final EFBoxFolderRepository folderRepository;
    private final EFBoxFolderService folderService;

    /**
     * first the method checks if the user is the parent folder's owner.
     * After (if) it is established, the file content is added to the new EfBoxFile and saved in the database.
     *
     * @param file           to be uploaded
     * @param user           to check access rights
     * @param parentFolderID where file will be saved and to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessDeniedException, IOException
     */
    public EFBoxFile uploadFile(MultipartFile file, User user, String parentFolderID)
            throws NoSuchElementException, AccessDeniedException, IOException {
        EFBoxFolder parentFolder = folderRepository
                .findById(UUID.fromString(parentFolderID))
                .orElseThrow(() -> new NoSuchElementException("Parent folder not found."));

        if (!user.getUserID().equals(parentFolder.getUser().getUserID())) {
            throw new AccessDeniedException(
                    user.getUsername()
                            + ": illegal upload/access attempt on folder "
                            + parentFolder.getName()
                            + "/"
                            + parentFolder.getFolderID());
        }

        try {
            EFBoxFile efBoxFile = new EFBoxFile(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes(), file.getContentType(), parentFolder);
            fileRepository.save(efBoxFile);
            return efBoxFile;
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * looks for fileID in database then checks parentFolder is accessible by user. If  the check passes it sends the
     * EFBoxFile back to the controller.
     *
     * @param fileID to find file
     * @param user   to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessDeniedException
     */
    public EFBoxFile getFile(String fileID, User user) throws NoSuchElementException, AccessDeniedException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!user.getUserID().equals(efBoxFile.getParentFolder().getUser().getUserID())) {
            throw new AccessDeniedException(
                    user.getUsername()
                            + ": illegal access attempt on file "
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        return efBoxFile;
    }

    /**
     * checks if parent folder is owned by user before deleting the file from the database
     *
     * @param fileID to find file
     * @param user   to check access rights
     * @return EFBoxFile
     * @throws NoSuchElementException, AccessDeniedException
     */
    public EFBoxFile deleteFile(String fileID, User user) throws NoSuchElementException, AccessDeniedException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found"));

        if (!efBoxFile.getParentFolder().getUser().getUserID().equals(user.getUserID())) {
            throw new AccessDeniedException(
                    user.getUsername()
                            + ": illegal deletion attempt on file"
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        efBoxFile.getParentFolder().getFiles().remove(efBoxFile);
        fileRepository.delete(efBoxFile);

        return efBoxFile;
    }

    /**
     * checks if parent folder is owned by user before changing the name of the EFBoxFile and updating it in the database
     *
     * @param fileID  to find file
     * @param newName self-explanatory
     * @param user    to check access rights
     * @return updated EFBoxFile
     * @throws AccessDeniedException, AccessDeniedException
     */
    public EFBoxFile changeFileName(String fileID, String newName, User user)
            throws NoSuchElementException, AccessDeniedException {
        EFBoxFile efBoxFile = fileRepository
                .findById(UUID.fromString(fileID))
                .orElseThrow(() -> new NoSuchElementException("File not found."));

        if (folderService.userIsNotFolderOwner(efBoxFile.getParentFolder(), user)) {
            throw new AccessDeniedException(
                    user.getUsername()
                            + ": illegal renaming attempt on file"
                            + efBoxFile.getFileName()
                            + "/"
                            + efBoxFile.getFileID());
        }

        efBoxFile.setFilename(newName);

        return fileRepository.save(efBoxFile);
    }
}
