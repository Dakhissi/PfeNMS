package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.MibFile;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MibFileRepository extends JpaRepository<MibFile, Long> {

    List<MibFile> findByUser(User user);

    List<MibFile> findByUserAndStatus(User user, MibFile.MibFileStatus status);

    Optional<MibFile> findByIdAndUser(Long id, User user);

    boolean existsByFilenameAndUser(String filename, User user);

    @Query("SELECT mf FROM MibFile mf WHERE mf.user = :user AND mf.name LIKE %:name%")
    List<MibFile> findByUserAndNameContaining(@Param("user") User user, @Param("name") String name);    long countByUser(User user);

    Optional<MibFile> findByFileHashAndUser(String fileHash, User user);
}
