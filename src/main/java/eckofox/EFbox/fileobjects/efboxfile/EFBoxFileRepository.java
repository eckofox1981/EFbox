package eckofox.EFbox.fileobjects.efboxfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EFBoxFileRepository extends JpaRepository<EFBoxFile, UUID> {

    /**
     * custom query since I couldn't find inbuild query (this turned out to be easier than to look for it)
     * checks if the filename is like the query AND if the parentfolder is owned by the userID (through JOIN)
     * the ILIKE is postgres specific
     * works but "RESULTS MAY VARY".
     */
    //NOTE: ILIKE is postgres specific in spring
    @Query(value = "SELECT * FROM files JOIN folders ON files.parent_folder_folderid = folders.folderid " +
            "WHERE files.filename ILIKE %?1% AND folders.user_userid = ?2", nativeQuery = true)
    Optional<Collection<EFBoxFile>> findByFilenameContainingIgnoreCaseWithUserID(String pattern, UUID userID);
}
