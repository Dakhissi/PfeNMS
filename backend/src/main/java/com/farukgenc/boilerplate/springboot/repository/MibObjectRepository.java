package com.farukgenc.boilerplate.springboot.repository;

import com.farukgenc.boilerplate.springboot.model.MibFile;
import com.farukgenc.boilerplate.springboot.model.MibObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MibObjectRepository extends JpaRepository<MibObject, Long> {

    List<MibObject> findByMibFile(MibFile mibFile);

    List<MibObject> findByParentIsNull(); // Root objects

    List<MibObject> findByParent(MibObject parent);

    Optional<MibObject> findByOid(String oid);

    List<MibObject> findByOidStartingWith(String oidPrefix);

    @Query("SELECT mo FROM MibObject mo WHERE mo.name LIKE %:name% AND mo.mibFile.user.id = :userId")
    List<MibObject> findByNameContainingAndUserId(@Param("name") String name, @Param("userId") Long userId);

    @Query("SELECT mo FROM MibObject mo WHERE mo.oid LIKE %:oidPart% AND mo.mibFile.user.id = :userId")
    List<MibObject> findByOidContainingAndUserId(@Param("oidPart") String oidPart, @Param("userId") Long userId);

    @Query("SELECT mo FROM MibObject mo WHERE mo.parent IS NULL AND mo.mibFile.user.id = :userId")
    List<MibObject> findRootsByUserId(@Param("userId") Long userId);    long countByMibFile(MibFile mibFile);

    void deleteByMibFile(MibFile mibFile);

    List<MibObject> findByMibFileUserOrderByOid(com.farukgenc.boilerplate.springboot.model.User user);
}
