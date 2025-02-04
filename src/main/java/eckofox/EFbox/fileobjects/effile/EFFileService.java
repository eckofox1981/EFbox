package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import eckofox.EFbox.fileobjects.effolder.EFFolderRepository;
import eckofox.EFbox.fileobjects.effolder.EFFolderService;
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
public class EFFileService {
    private final EFFileRepository fileRepository;
    private final EFFolderRepository folderRepository;
    private final EFFolderService folderService;

    /**
     * uploads file to a folder
     * @param file to be uploaded
     * @param user to check access rights
     * @param parentFolderID where file will be saved and to check access rights
     * @return message
     * @throws IOException
     * @throws IllegalAccessError
     */
    public String uploadFile(MultipartFile file, User user, String parentFolderID) throws IOException, IllegalAccessError {
        EFFolder parentFolder = folderRepository.findById(UUID.fromString(parentFolderID)).orElseThrow();
        System.out.println("DEBUG uploadfile service: folder user: " + parentFolder.getUser().getUsername());
        if (!user.equals(parentFolder.getUser())) {
            throw new IllegalAccessError("You are not authorized to upload a file to this folder");
        }
        try {
            EFFile efFile = new EFFile(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes(), file.getContentType(), parentFolder);
            fileRepository.save(efFile);
            return "File: " + efFile.getFileName() + " saved in " + parentFolder.getName();
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
    public EFFileDTO getFile(String fileID, User user) throws IllegalAccessException {
        EFFile efFile = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!user.getUserID().equals(efFile.getParentFolder().getUser().getUserID())) {
            throw new IllegalAccessException("You are not allowed to download this file.");
        }
        return EFFileDTO.fromEFFile(efFile);
    }

    /**
     * deltes file
     * @param fileID to find file
     * @param user to check access rights
     * @return file dto
     * @throws Exception
     */
    public String deleteFile(String fileID, User user) throws Exception {
        EFFile file = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found"));
        if (!file.getParentFolder().getUser().getUserID().equals(user.getUserID())) {
            throw new IllegalAccessException("You are not authorized to access this file.");
        }
        String fileName = file.getFileName();
        file.getParentFolder().getFiles().remove(file);
        fileRepository.delete(file);
        return "File \"" + fileName + "\" deleted.";
    }

    /**
     * changes the name of the EFFile
     * @param fileID to find file
     * @param newName self-explanatory
     * @param user to check access rights
     * @return update file dto
     * @throws Exception
     */
    public EFFileDTO changeFileName(String fileID, String newName, User user) throws Exception {
        EFFile file = fileRepository.findById(UUID.fromString(fileID)).orElseThrow(() -> new NoSuchElementException("File not found."));
        if (folderService.userIsNotFolderOwner(file.getParentFolder(), user)) {
            throw new IllegalAccessException("You are not allowed to acces this file");
        }
        file.setFilename(newName);
        fileRepository.save(file);
        return EFFileDTO.fromEFFile(file);
    }
}
