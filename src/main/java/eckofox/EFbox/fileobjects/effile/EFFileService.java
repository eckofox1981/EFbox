package eckofox.EFbox.fileobjects.effile;

import eckofox.EFbox.fileobjects.effolder.EFFolder;
import eckofox.EFbox.fileobjects.effolder.EFFolderRepository;
import eckofox.EFbox.fileobjects.effolder.EFFolderService;
import eckofox.EFbox.user.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EFFileService {
    private final EFFileRepository fileRepository;
    private final EFFolderRepository folderRepository;

    public String uploadFile (MultipartFile file, User user, String parentFolderID) throws IOException, IllegalAccessError{
        EFFolder parentFolder = folderRepository.findById(UUID.fromString(parentFolderID)).orElseThrow();
//        System.out.println("DEBUG uploadfile service: folder user: " + parentFolder.getUser().getUsername());
//        if (!user.equals(parentFolder.getUser())){
//            throw new IllegalAccessError ("You are not authorized to upload a file to this folder");
//        }
        try {
            EFFile efFile = new EFFile(UUID.randomUUID(), file.getOriginalFilename(), file.getBytes(), file.getContentType(), parentFolder);
            fileRepository.save(efFile);
            return "File: " + efFile.getFilename() + " saved in " + parentFolder.getName();
        } catch (IOException e) {
            throw new IOException(e.getMessage());
        }
    }
}
