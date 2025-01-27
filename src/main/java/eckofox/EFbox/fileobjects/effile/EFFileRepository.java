package eckofox.EFbox.fileobjects.effile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EFFileRepository extends JpaRepository<EFFile, UUID> {
}
