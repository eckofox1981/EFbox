package eckofox.EFbox.fileobjects.efboxfolder;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EFBoxFolderRepository extends JpaRepository<EFBoxFolder, UUID> {

    /**
     * custom SQLquery which looks for foldernames like (ILIKE for postgres) the string pattern with the specific
     * user attached to (through user id)
     */
    @Query(value = "SELECT * FROM folders WHERE name ILIKE %?1% AND user_userid = ?2", nativeQuery = true)
    Optional<Collection<EFBoxFolder>> findByNameContainingIgnoreCaseWithUserID(String pattern, UUID userID);

    /**
     * for some reason I haven't been able to debug, hibernate stopped deleting folder and their descendants.
     * This method does remove the folder with the folderID given. User ownership is checked before calling the method.
     * @param folderID to removed from database
     * @return int of rows deleted (not used, but void is apparently unacceptable)
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM folders WHERE folderid = ?1 OR parent_folderid = ?1", nativeQuery = true)
    int customFolderDeletion(UUID folderID);
}
