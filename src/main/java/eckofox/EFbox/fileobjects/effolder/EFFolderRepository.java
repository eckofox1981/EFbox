package eckofox.EFbox.fileobjects.effolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EFFolderRepository extends JpaRepository<EFFolder, UUID> {
}
