package eckofox.EFbox.fileobjects.effile;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EFFileService {
    private final EFFileRepository fileRepository;
}
