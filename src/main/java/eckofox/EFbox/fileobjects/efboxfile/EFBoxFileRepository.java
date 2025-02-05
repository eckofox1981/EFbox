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
     * custom query since I couldn't find inbuild query (easier)
     */
    //NOTE: ILIKE is postgres specific in spring
    @Query(value = "SELECT * FROM files WHERE filename ILIKE %?1%", nativeQuery = true)
    Optional<Collection<EFBoxFile>> findByFilenameContainingIgnoreCaseWithUserID(String pattern);
}
