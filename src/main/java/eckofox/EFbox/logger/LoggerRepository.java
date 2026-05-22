package eckofox.EFbox.logger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoggerRepository extends JpaRepository<LogMessage, UUID> {

}
