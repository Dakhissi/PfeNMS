package com.farukgenc.boilerplate.springboot.service.browser;

import com.farukgenc.boilerplate.springboot.dto.*;
import com.farukgenc.boilerplate.springboot.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MibService {

    // MIB File management
    MibFileDto uploadMibFile(MultipartFile file, User user);

    List<MibFileDto> getMibFilesByUser(User user);

    MibFileDto getMibFileById(Long id, User user);

    void deleteMibFile(Long id, User user);

    // MIB Object operations
    List<MibObjectDto> getMibTree(User user);

    List<MibObjectDto> getMibTreeByFile(Long mibFileId, User user);

    MibObjectDto getMibObjectByOid(String oid, User user);

    List<MibObjectDto> searchMibObjects(String query, User user);    // MIB Browser operations
    MibBrowserResponse browseOid(MibBrowserRequest request);

    List<MibBrowserResponse> walkOidTree(MibBrowserRequest request);

    // SNMP operations with user context
    MibBrowserResponse performSnmpGet(MibBrowserRequest request, User user);

    List<MibBrowserResponse> performSnmpWalk(MibBrowserRequest request, User user);
}
