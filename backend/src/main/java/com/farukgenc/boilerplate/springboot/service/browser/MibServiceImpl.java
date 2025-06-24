package com.farukgenc.boilerplate.springboot.service.browser;

import com.farukgenc.boilerplate.springboot.dto.*;
import com.farukgenc.boilerplate.springboot.mapper.MibFileMapper;
import com.farukgenc.boilerplate.springboot.mapper.MibObjectMapper;
import com.farukgenc.boilerplate.springboot.model.MibFile;
import com.farukgenc.boilerplate.springboot.model.MibObject;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.repository.MibFileRepository;
import com.farukgenc.boilerplate.springboot.repository.MibObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TreeEvent;
import org.snmp4j.util.TreeUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Simplified MIB Service implementation without Mibble dependency
 * Provides basic MIB functionality and SNMP operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MibServiceImpl implements MibService {

    private final MibFileRepository mibFileRepository;
    private final MibObjectRepository mibObjectRepository;
    private final MibFileMapper mibFileMapper;
    private final MibObjectMapper mibObjectMapper;

    @Value("${app.mib.upload-dir:mib-files}")
    private String mibUploadDir;

    // Common SNMP OIDs for basic functionality
    private static final Map<String, String> COMMON_OIDS = new HashMap<>();
    
    static {
        COMMON_OIDS.put("1.3.6.1.2.1.1.1.0", "sysDescr");
        COMMON_OIDS.put("1.3.6.1.2.1.1.2.0", "sysObjectID");
        COMMON_OIDS.put("1.3.6.1.2.1.1.3.0", "sysUpTime");
        COMMON_OIDS.put("1.3.6.1.2.1.1.4.0", "sysContact");
        COMMON_OIDS.put("1.3.6.1.2.1.1.5.0", "sysName");
        COMMON_OIDS.put("1.3.6.1.2.1.1.6.0", "sysLocation");
        COMMON_OIDS.put("1.3.6.1.2.1.1.7.0", "sysServices");
        COMMON_OIDS.put("1.3.6.1.2.1.2.1.0", "ifNumber");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.1", "ifIndex");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.2", "ifDescr");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.3", "ifType");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.5", "ifSpeed");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.6", "ifPhysAddress");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.7", "ifAdminStatus");
        COMMON_OIDS.put("1.3.6.1.2.1.2.2.1.8", "ifOperStatus");
    }

    @Override
    public MibFileDto uploadMibFile(MultipartFile file, User user) {
        log.info("Uploading MIB file: {} for user: {}", file.getOriginalFilename(), user.getUsername());

        try {
            // Validate file
            validateMibFile(file);

            // Save file to disk
            String fileName = saveFileToDisk(file);
            String checksum = calculateChecksum(file.getBytes());            // Check for duplicates
            Optional<MibFile> existingFile = mibFileRepository.findByFileHashAndUser(checksum, user);
            if (existingFile.isPresent()) {
                throw new IllegalArgumentException("MIB file already exists: " + existingFile.get().getFilename());
            }

            // Create MIB file entity
            MibFile mibFile = MibFile.builder()
                    .name(file.getOriginalFilename())
                    .filename(file.getOriginalFilename())
                    .filePath(fileName)
                    .fileSize(file.getSize())
                    .fileHash(checksum)
                    .user(user)
                    .build();

            mibFile = mibFileRepository.save(mibFile);

            // Parse and create basic MIB objects
            parseBasicMibFile(mibFile);

            log.info("MIB file uploaded and parsed successfully: {}", mibFile.getId());
            return mibFileMapper.toDto(mibFile);

        } catch (Exception e) {
            log.error("Error uploading MIB file", e);
            throw new RuntimeException("Failed to upload MIB file: " + e.getMessage(), e);
        }
    }

    @Override
    public List<MibFileDto> getMibFilesByUser(User user) {
        List<MibFile> mibFiles = mibFileRepository.findByUser(user);
        return mibFiles.stream()
                .map(mibFileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMibFile(Long id, User user) {        log.info("Deleting MIB file ID: {} for user: {}", id, user.getUsername());

        MibFile mibFile = mibFileRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("MIB file not found"));

        // Delete associated MIB objects
        mibObjectRepository.deleteByMibFile(mibFile);

        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(mibUploadDir, mibFile.getFilePath()));
        } catch (Exception e) {
            log.warn("Failed to delete MIB file from disk: {}", mibFile.getFilePath(), e);
        }

        // Delete from database
        mibFileRepository.delete(mibFile);
        log.info("MIB file deleted successfully: {}", id);
    }

    @Override
    public MibFileDto getMibFileById(Long id, User user) {
        MibFile mibFile = mibFileRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("MIB file not found"));
        return mibFileMapper.toDto(mibFile);
    }

    @Override
    public List<MibObjectDto> getMibTreeByFile(Long mibFileId, User user) {
        MibFile mibFile = mibFileRepository.findByIdAndUser(mibFileId, user)
                .orElseThrow(() -> new IllegalArgumentException("MIB file not found"));
        
        List<MibObject> objects = mibObjectRepository.findByMibFile(mibFile);
        return objects.stream()
                .map(mibObjectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MibObjectDto getMibObjectByOid(String oid, User user) {
        Optional<MibObject> mibObject = mibObjectRepository.findByOid(oid);
        if (mibObject.isPresent() && mibObject.get().getMibFile() != null && 
            mibObject.get().getMibFile().getUser().getId().equals(user.getId())) {
            return mibObjectMapper.toDto(mibObject.get());
        }
        throw new IllegalArgumentException("MIB object not found for OID: " + oid);
    }

    @Override
    public List<MibObjectDto> searchMibObjects(String query, User user) {
        List<MibObject> objects = new ArrayList<>();
        
        // Search by name
        objects.addAll(mibObjectRepository.findByNameContainingAndUserId(query, user.getId()));
        
        // Search by OID if not already found
        List<MibObject> oidResults = mibObjectRepository.findByOidContainingAndUserId(query, user.getId());
        for (MibObject obj : oidResults) {
            if (!objects.contains(obj)) {
                objects.add(obj);
            }
        }
        
        return objects.stream()
                .map(mibObjectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<MibObjectDto> getMibTree(User user) {
        log.debug("Getting MIB tree for user: {}", user.getUsername());

        // Create a basic MIB tree with common OIDs
        List<MibObject> rootObjects = createBasicMibTree(user);
        
        return rootObjects.stream()
                .map(mibObjectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MibBrowserResponse performSnmpGet(MibBrowserRequest request, User user) {        log.debug("Performing SNMP GET for OID: {} on target: {}", request.getOid(), request.getTargetIp());

        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget<Address> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(request.getCommunity()));
            target.setAddress(GenericAddress.parse("udp:" + request.getTargetIp() + "/161"));
            target.setRetries(request.getRetries());
            target.setTimeout(request.getTimeout());
            target.setVersion(SnmpConstants.version2c);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(request.getOid())));
            pdu.setType(PDU.GET);

            ResponseEvent<Address> event = snmp.send(pdu, target);
            snmp.close();

            if (event != null && event.getResponse() != null) {
                PDU response = event.getResponse();
                if (response.getErrorStatus() == 0) {
                    VariableBinding vb = response.get(0);
                    return MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .value(vb.getVariable().toString())
                            .type(vb.getVariable().getClass().getSimpleName())
                            .success(true)
                            .build();
                } else {
                    return MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .success(false)
                            .errorMessage("SNMP Error: " + response.getErrorStatusText())
                            .build();
                }
            } else {
                return MibBrowserResponse.builder()
                        .oid(request.getOid())
                        .success(false)
                        .errorMessage("No response from target")
                        .build();
            }

        } catch (Exception e) {
            log.error("Error performing SNMP GET", e);
            return MibBrowserResponse.builder()
                    .oid(request.getOid())
                    .success(false)
                    .errorMessage("Error: " + e.getMessage())
                    .build();
        }
    }

    @Override
    public List<MibBrowserResponse> performSnmpWalk(MibBrowserRequest request, User user) {        log.debug("Performing SNMP WALK for OID: {} on target: {}", request.getOid(), request.getTargetIp());

        List<MibBrowserResponse> results = new ArrayList<>();

        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget<Address> target = new CommunityTarget<>();
            target.setCommunity(new OctetString(request.getCommunity()));
            target.setAddress(GenericAddress.parse("udp:" + request.getTargetIp() + "/161"));
            target.setRetries(request.getRetries());
            target.setTimeout(request.getTimeout());
            target.setVersion(SnmpConstants.version2c);

            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(request.getOid()));

            for (TreeEvent event : events) {
                if (event != null && !event.isError()) {
                    VariableBinding[] varBindings = event.getVariableBindings();
                    if (varBindings != null) {
                        for (VariableBinding vb : varBindings) {
                            results.add(MibBrowserResponse.builder()
                                    .oid(vb.getOid().toString())
                                    .value(vb.getVariable().toString())
                                    .type(vb.getVariable().getClass().getSimpleName())
                                    .success(true)
                                    .build());
                        }
                    }
                } else if (event != null && event.isError()) {
                    results.add(MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .success(false)
                            .errorMessage("SNMP Walk Error: " + event.getErrorMessage())
                            .build());
                }
            }

            snmp.close();

        } catch (Exception e) {
            log.error("Error performing SNMP WALK", e);
            results.add(MibBrowserResponse.builder()
                    .oid(request.getOid())
                    .success(false)
                    .errorMessage("Error: " + e.getMessage())
                    .build());
        }

        return results;
    }

    @Override
    public MibBrowserResponse browseOid(MibBrowserRequest request) {
        // Default user context - for compatibility
        return performSnmpGet(request, null);
    }

    @Override
    public List<MibBrowserResponse> walkOidTree(MibBrowserRequest request) {
        // Default user context - for compatibility
        return performSnmpWalk(request, null);
    }

    private void validateMibFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || (!extension.equalsIgnoreCase("mib") && !extension.equalsIgnoreCase("txt"))) {
            throw new IllegalArgumentException("Invalid file format. Only .mib and .txt files are allowed");
        }

        if (file.getSize() > 10 * 1024 * 1024) { // 10MB limit
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(mibUploadDir);
        Files.createDirectories(uploadPath);

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        return fileName;
    }

    private String calculateChecksum(byte[] content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    private void parseBasicMibFile(MibFile mibFile) {
        // For now, create a simple placeholder MIB object
        // In a real implementation, you would parse the MIB file content
          MibObject rootObject = MibObject.builder()
                .name("uploaded-mib")
                .oid("1.3.6.1.4.1.999." + mibFile.getId()) // Custom enterprise OID
                .description("Uploaded MIB file: " + mibFile.getFilename())
                .type(MibObject.MibType.MODULE_IDENTITY)
                .access(MibObject.MibAccess.NOT_ACCESSIBLE)
                .status(MibObject.MibStatus.CURRENT)
                .mibFile(mibFile)
                .build();

        mibObjectRepository.save(rootObject);
        log.info("Created basic MIB object for file: {}", mibFile.getFilename());
    }

    private List<MibObject> createBasicMibTree(User user) {
        List<MibObject> objects = new ArrayList<>();        // Check if basic MIB objects already exist
        List<MibObject> existingObjects = mibObjectRepository.findByMibFileUserOrderByOid(user);
        if (!existingObjects.isEmpty()) {
            return existingObjects;
        }

        // Create basic system MIB objects
        for (Map.Entry<String, String> entry : COMMON_OIDS.entrySet()) {
            MibObject object = MibObject.builder()
                    .name(entry.getValue())
                    .oid(entry.getKey())
                    .description("Standard MIB-2 object: " + entry.getValue())
                    .type(MibObject.MibType.OBJECT_TYPE)
                    .access(MibObject.MibAccess.READ_ONLY)
                    .status(MibObject.MibStatus.CURRENT)
                    .syntaxType("OCTET STRING")
                    .build();

            objects.add(mibObjectRepository.save(object));
        }

        return objects;
    }
}
