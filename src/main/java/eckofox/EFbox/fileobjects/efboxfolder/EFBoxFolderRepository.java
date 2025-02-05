package eckofox.EFbox.fileobjects.efboxfolder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EFBoxFolderRepository extends JpaRepository<EFBoxFolder, UUID> {

    /**
     * custom query since I couldn't find inbuild query (easier)
     */
    @Query(value = "SELECT * FROM folders WHERE name ILIKE %?1% AND user_userid = ?2", nativeQuery = true)
    Optional<Collection<EFBoxFolder>> findByNameContainingIgnoreCaseWithUserID(String pattern, UUID userID);
}
