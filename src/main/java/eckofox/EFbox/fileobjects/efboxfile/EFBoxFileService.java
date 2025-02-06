package eckofox.EFbox.fileobjects.efboxfile;

import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolder;
import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolderRepository;
import eckofox.EFbox.fileobjects.efboxfolder.EFBoxFolderService;
import eckofox.EFbox.user.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
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
     * uploads file to a folder (adds it to list EFBoxFolder.files)
     * @param file to be uploaded
     * @param user to check access rights
     * @param parentFolderID where file will be saved and to check access rights
     * @return message
     * @throws IOException
     * @throws IllegalAccessError
     */
    public EFBoxFile uploadFile(MultipartFile file, User user, String parentFolderID) throws IOException, IllegalAccessError {
        EFBoxFolder parentFolder = folderRepository.findById(UUID.fromString(parentFolderID)).orElseThrow();
        System.out.println("DEBUG uploadfile service: folder user: " + parentFolder.getUser().getUsername());
        if (!user.equals(parentFolder.getUser())) {
            throw new IllegalAccessError("You are not authorized to upload a file to this folder");
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
     * gets file to be downloaded
     * @param fileID to find file
     * @param user to check access rights
     * @return file dto
     * @throws IllegalAccessException
     */
    public EFBoxFile getFile(String fileID, User user) throws IllegalAccessException {
        EFBoxFile efBoxFile = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!user.getUserID().equals(efBoxFile.getParentFolder().getUser().getUserID())) {
            throw new IllegalAccessException("You are not allowed to download this file.");
        }
        return efBoxFile;
    }

    /**
     * deltes file
     * @param fileID to find file
     * @param user to check access rights
     * @return file dto
     * @throws Exception
     */
    public EFBoxFile deleteFile(String fileID, User user) throws Exception {
        EFBoxFile efBoxFile = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!efBoxFile.getParentFolder().getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not authorized to access this efBoxFile.");
        }
        String fileName = efBoxFile.getFileName();
        efBoxFile.getParentFolder().getFiles().remove(efBoxFile);
        fileRepository.delete(efBoxFile);
        return efBoxFile;
    }

    /**
     * changes the name of the EFBoxFile
     * @param fileID to find file
     * @param newName self-explanatory
     * @param user to check access rights
     * @return update file dto
     * @throws Exception
     */
    public EFBoxFileDTO changeFileName(String fileID, String newName, User user) throws Exception {
        EFBoxFile file = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found."));
        if (folderService.userIsNotFolderOwner(file.getParentFolder(), user)) {
            throw new IllegalAccessException("You are not allowed to acces this file");
        }
        file.setFilename(newName);
        fileRepository.save(file);
        return EFBoxFileDTO.fromEFBoxFile(file);
    }
}
