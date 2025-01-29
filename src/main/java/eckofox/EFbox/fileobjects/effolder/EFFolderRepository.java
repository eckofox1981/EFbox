package eckofox.EFbox.fileobjects.effolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EFFolderRepository extends JpaRepository<EFFolder, UUID> {

    @Query(value = "SELECT * FROM folders WHERE name ILIKE %?1% AND user_userid = ?2", nativeQuery = true)
    Optional<Collection<EFFolder>> findByNameContainingIgnoreCaseWithUserID(String pattern, UUID userID);
}
