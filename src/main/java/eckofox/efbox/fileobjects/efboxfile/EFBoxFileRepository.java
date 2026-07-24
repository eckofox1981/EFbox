package eckofox.efbox.fileobjects.efboxfile;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;
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
    @Query(value = "SELECT * FROM files f " +
            "JOIN folders fo ON f.parent_folder_folderid = fo.folderid " +
            "WHERE f.filename ILIKE CONCAT('%', ?1, '%') AND fo.user_userid = ?2",
            nativeQuery = true)
    List<EFBoxFile> findByFilenameContainingIgnoreCaseWithUserID(String pattern, UUID userID);
}
