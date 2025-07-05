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
import net.percederberg.mibble.*;
import net.percederberg.mibble.value.ObjectIdentifierValue;
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
 * Complete MIB Service implementation with Mibble parser
 * Provides full MIB functionality with hierarchical structure and SNMP operations
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

    // Mibble loader instance for parsing MIB files
    private final MibLoader mibLoader = new MibLoader();

    @Override
    public MibFileDto uploadMibFile(MultipartFile file, User user) {
        try {
            validateMibFile(file);

            /* 1️⃣ copy once, then forget MultipartFile */
            Path stored = saveFileToDisk(file);        // may throw IOException
            byte[] content = Files.readAllBytes(stored); // may throw IOException
            String checksum = calculateChecksum(content);

            // duplicate check …
            if (mibFileRepository.findByFileHashAndUser(checksum, user).isPresent()) {
                throw new IllegalArgumentException("MIB already uploaded");
            }

            /* 2️⃣ persist DB record */
            MibFile mibFile = mibFileRepository.save(
                    MibFile.builder()
                            .name(file.getOriginalFilename())
                            .filename(stored.getFileName().toString())
                            .filePath(stored.toString())
                            .fileSize(file.getSize())       // or (long) content.length
                            .fileHash(checksum)
                            .status(MibFile.MibFileStatus.LOADING)
                            .user(user)
                            .build());

            try {
                parseMibFileWithMibble(mibFile);       // works on the copy
                mibFile.setStatus(MibFile.MibFileStatus.LOADED);
            } catch (Exception ex) {
                mibFile.setStatus(MibFile.MibFileStatus.ERROR);
                mibFile.setLoadErrorMessage(ex.getMessage());
                throw ex;
            } finally {
                mibFileRepository.save(mibFile);
            }
            return mibFileMapper.toDto(mibFile);

        } catch (IOException | MibLoaderException io) {                     // <-- catches both calls
            throw new RuntimeException("Failed to store MIB file", io);
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
        log.debug("Getting hierarchical MIB tree for user: {}", user.getUsername());

        // Get root objects (objects without parent)
        List<MibObject> rootObjects = mibObjectRepository.findRootsByUserId(user.getId());
        
        // If no user-specific objects, create standard MIB-2 tree
        if (rootObjects.isEmpty()) {
            rootObjects = createStandardMibTree(user);
        }
        
        return rootObjects.stream()
                .map(this::convertToHierarchicalDto)
                .collect(Collectors.toList());
    }

    @Override
    public MibBrowserResponse performSnmpGet(MibBrowserRequest request, User user) {
        log.debug("Performing SNMP GET for OID: {} on target: {}", request.getOid(), request.getTargetIp());

        long startTime = System.currentTimeMillis();
        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget<Address> target = createSnmpTarget(request);

            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(request.getOid())));
            pdu.setType(PDU.GET);

            ResponseEvent<Address> event = snmp.send(pdu, target);
            snmp.close();

            long responseTime = System.currentTimeMillis() - startTime;

            if (event != null && event.getResponse() != null) {
                PDU response = event.getResponse();
                if (response.getErrorStatus() == 0) {
                    VariableBinding vb = response.get(0);
                    
                    // Try to get MIB object name from database
                    String objectName = getMibObjectName(request.getOid(), user);
                    
                    return MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .name(objectName)
                            .value(vb.getVariable().toString())
                            .type(vb.getVariable().getClass().getSimpleName())
                            .syntax(determineSyntax(vb.getVariable()))
                            .success(true)
                            .responseTime(responseTime)
                            .build();
                } else {
                    return MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .success(false)
                            .errorMessage("SNMP Error: " + response.getErrorStatusText())
                            .responseTime(responseTime)
                            .build();
                }
            } else {
                return MibBrowserResponse.builder()
                        .oid(request.getOid())
                        .success(false)
                        .errorMessage("No response from target")
                        .responseTime(responseTime)
                        .build();
            }

        } catch (Exception e) {
            log.error("Error performing SNMP GET", e);
            return MibBrowserResponse.builder()
                    .oid(request.getOid())
                    .success(false)
                    .errorMessage("Error: " + e.getMessage())
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build();
        }
    }

    @Override
    public List<MibBrowserResponse> performSnmpWalk(MibBrowserRequest request, User user) {
        log.debug("Performing SNMP WALK for OID: {} on target: {}", request.getOid(), request.getTargetIp());

        List<MibBrowserResponse> results = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        try {
            DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();

            CommunityTarget<Address> target = createSnmpTarget(request);
            TreeUtils treeUtils = new TreeUtils(snmp, new DefaultPDUFactory());
            List<TreeEvent> events = treeUtils.getSubtree(target, new OID(request.getOid()));

            for (TreeEvent event : events) {
                if (event != null && !event.isError()) {
                    VariableBinding[] varBindings = event.getVariableBindings();
                    if (varBindings != null) {
                        for (VariableBinding vb : varBindings) {
                            String oid = vb.getOid().toString();
                            String objectName = getMibObjectName(oid, user);
                            
                            results.add(MibBrowserResponse.builder()
                                    .oid(oid)
                                    .name(objectName)
                                    .value(vb.getVariable().toString())
                                    .type(vb.getVariable().getClass().getSimpleName())
                                    .syntax(determineSyntax(vb.getVariable()))
                                    .success(true)
                                    .responseTime(System.currentTimeMillis() - startTime)
                                    .build());
                        }
                    }
                } else if (event != null && event.isError()) {
                    results.add(MibBrowserResponse.builder()
                            .oid(request.getOid())
                            .success(false)
                            .errorMessage("SNMP Walk Error: " + event.getErrorMessage())
                            .responseTime(System.currentTimeMillis() - startTime)
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
                    .responseTime(System.currentTimeMillis() - startTime)
                    .build());
        }

        return results;
    }

    /**
     * Parse MIB file using Mibble library and create hierarchical MIB objects
     */
    private void parseMibFileWithMibble(MibFile mibFile) throws IOException, MibLoaderException {
        log.info("Parsing MIB file with Mibble: {}", mibFile.getFilename());

        // Load standard MIBs first
        loadStandardMibs();

        // Load the uploaded MIB file
        String baseDir = System.getProperty("user.dir");
        Path uploadPath = Paths.get(baseDir, mibUploadDir);
        File file = uploadPath.resolve(mibFile.getFilePath()).toFile();
        Mib mib = mibLoader.load(file);

        // Update MIB file metadata
        updateMibFileMetadata(mibFile, mib);

        // Create MIB objects with hierarchy
        Map<String, MibObject> oidToObjectMap = new HashMap<>();
        createMibObjectsFromMib(mib, mibFile, oidToObjectMap);

        // Establish parent-child relationships
        establishHierarchy(oidToObjectMap);

        log.info("Successfully parsed MIB file: {} with {} objects", 
                mibFile.getFilename(), oidToObjectMap.size());
    }

    /**
     * Load standard MIBs to resolve dependencies
     */
    private void loadStandardMibs() {
        try {
            // Load standard MIBs that are commonly referenced
            mibLoader.load("SNMPv2-SMI");
            mibLoader.load("SNMPv2-TC");
            mibLoader.load("SNMPv2-CONF");
            mibLoader.load("SNMPv2-MIB");
            mibLoader.load("IANAifType-MIB");
            mibLoader.load("IF-MIB");
        } catch (Exception e) {
            log.warn("Could not load some standard MIBs: {}", e.getMessage());
        }
    }

    /**
     * Update MIB file metadata from parsed MIB
     */
    private void updateMibFileMetadata(MibFile mibFile, Mib mib) {
        mibFile.setModuleName(mib.getName());
        
        // Extract module information if available
        if (mib.getHeaderComment() != null) {
            mibFile.setDescription(mib.getHeaderComment());
        }
        
        // Try to extract organization and contact info from MIB content
        Collection<MibSymbol> symbols = mib.getAllSymbols();
        for (MibSymbol symbol : symbols) {
            if (symbol instanceof MibValueSymbol valueSymbol) {
                if ("MODULE-IDENTITY".equals(valueSymbol.getType().getName())) {
                    // Extract organization and contact info if available
                    // This would require parsing the MODULE-IDENTITY construct
                    break;
                }
            }
        }
    }

    /**
     * Create MIB objects from parsed MIB
     */
    private void createMibObjectsFromMib(Mib mib, MibFile mibFile, Map<String, MibObject> oidToObjectMap) {
        Collection<MibSymbol> symbols = mib.getAllSymbols();
        for (MibSymbol symbol : symbols) {
            if (symbol instanceof MibValueSymbol valueSymbol) {
                MibValue value = valueSymbol.getValue();
                
                if (value instanceof ObjectIdentifierValue oidValue) {
                    String oid = oidValue.toString();
                    
                    // Check if object already exists for this MIB file
                    Optional<MibObject> existingObject = mibObjectRepository.findByOidAndMibFile(oid, mibFile);
                    
                    if (existingObject.isPresent()) {
                        // Update existing object
                        MibObject existing = existingObject.get();
                        updateMibObjectFromSymbol(existing, valueSymbol);
                        mibObjectRepository.save(existing);
                        oidToObjectMap.put(oid, existing);
                    } else {
                        // Create new object
                        MibObject mibObject = createMibObjectFromSymbol(valueSymbol, mibFile);
                        if (mibObject != null) {
                            mibObject = mibObjectRepository.save(mibObject);
                            oidToObjectMap.put(oid, mibObject);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create MIB object from MIB symbol
     */
    private MibObject createMibObjectFromSymbol(MibValueSymbol symbol, MibFile mibFile) {
        try {
            ObjectIdentifierValue oidValue = (ObjectIdentifierValue) symbol.getValue();
            String oid = oidValue.toString();
            
            MibObject.MibObjectBuilder builder = MibObject.builder()
                    .name(symbol.getName())
                    .oid(oid)
                    .mibFile(mibFile)
                    .user(mibFile.getUser());

            // Set description
            if (symbol.getComment() != null) {
                builder.description(symbol.getComment());
            }

            // Determine object type and properties
            MibType mibType = symbol.getType();
            if (mibType != null) {
                setObjectTypeProperties(builder, mibType);
            }

            return builder.build();

        } catch (Exception e) {
            log.warn("Error creating MIB object from symbol: {}", symbol.getName(), e);
            return null;
        }
    }

    /**
     * Update existing MIB object from MIB symbol
     */
    private void updateMibObjectFromSymbol(MibObject existingObject, MibValueSymbol symbol) {
        try {
            // Update name if different
            if (!existingObject.getName().equals(symbol.getName())) {
                existingObject.setName(symbol.getName());
            }

            // Update description if available
            if (symbol.getComment() != null && !symbol.getComment().equals(existingObject.getDescription())) {
                existingObject.setDescription(symbol.getComment());
            }

            // Update type properties if needed
            MibType mibType = symbol.getType();
            if (mibType != null) {
                String typeName = mibType.getName();
                if (!typeName.equals(existingObject.getSyntaxType())) {
                    setObjectTypeProperties(existingObject, mibType);
                }
            }

        } catch (Exception e) {
            log.warn("Error updating MIB object from symbol: {}", symbol.getName(), e);
        }
    }

    /**
     * Set object type properties based on MIB type
     */
    private void setObjectTypeProperties(MibObject.MibObjectBuilder builder, MibType mibType) {
        String typeName = mibType.getName();
        
        // Map MIB types to our enum
        switch (typeName) {
            case "OBJECT-TYPE":
                builder.type(MibObject.MibType.OBJECT_TYPE);
                builder.access(MibObject.MibAccess.READ_ONLY);
                break;
            case "MODULE-IDENTITY":
                builder.type(MibObject.MibType.MODULE_IDENTITY);
                builder.access(MibObject.MibAccess.NOT_ACCESSIBLE);
                break;
            case "OBJECT-IDENTITY":
                builder.type(MibObject.MibType.OBJECT_IDENTITY);
                builder.access(MibObject.MibAccess.NOT_ACCESSIBLE);
                break;
            case "NOTIFICATION-TYPE":
                builder.type(MibObject.MibType.NOTIFICATION_TYPE);
                builder.access(MibObject.MibAccess.ACCESSIBLE_FOR_NOTIFY);
                break;
            default:
                builder.type(MibObject.MibType.OBJECT_TYPE);
                builder.access(MibObject.MibAccess.READ_ONLY);
        }

        builder.status(MibObject.MibStatus.CURRENT);
        builder.syntaxType(typeName);
    }

    /**
     * Set object type properties for existing MIB object
     */
    private void setObjectTypeProperties(MibObject mibObject, MibType mibType) {
        String typeName = mibType.getName();
        
        // Map MIB types to our enum
        switch (typeName) {
            case "OBJECT-TYPE":
                mibObject.setType(MibObject.MibType.OBJECT_TYPE);
                mibObject.setAccess(MibObject.MibAccess.READ_ONLY);
                break;
            case "MODULE-IDENTITY":
                mibObject.setType(MibObject.MibType.MODULE_IDENTITY);
                mibObject.setAccess(MibObject.MibAccess.NOT_ACCESSIBLE);
                break;
            case "OBJECT-IDENTITY":
                mibObject.setType(MibObject.MibType.OBJECT_IDENTITY);
                mibObject.setAccess(MibObject.MibAccess.NOT_ACCESSIBLE);
                break;
            case "NOTIFICATION-TYPE":
                mibObject.setType(MibObject.MibType.NOTIFICATION_TYPE);
                mibObject.setAccess(MibObject.MibAccess.ACCESSIBLE_FOR_NOTIFY);
                break;
            default:
                mibObject.setType(MibObject.MibType.OBJECT_TYPE);
                mibObject.setAccess(MibObject.MibAccess.READ_ONLY);
        }

        mibObject.setStatus(MibObject.MibStatus.CURRENT);
        mibObject.setSyntaxType(typeName);
    }

    /**
     * Establish parent-child relationships between MIB objects
     */
    private void establishHierarchy(Map<String, MibObject> oidToObjectMap) {
        for (MibObject mibObject : oidToObjectMap.values()) {
            String oid = mibObject.getOid();
            String parentOid = getParentOid(oid);
            
            if (parentOid != null && oidToObjectMap.containsKey(parentOid)) {
                MibObject parent = oidToObjectMap.get(parentOid);
                mibObject.setParent(parent);
                parent.getChildren().add(mibObject);
                mibObjectRepository.save(mibObject);
            }
        }
    }

    /**
     * Get parent OID from child OID
     */
    private String getParentOid(String oid) {
        if (oid == null || oid.isEmpty()) {
            return null;
        }
        
        int lastDotIndex = oid.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return oid.substring(0, lastDotIndex);
        }
        
        return null;
    }

    /**
     * Create standard MIB-2 tree for users without uploaded MIBs
     */
    private List<MibObject> createStandardMibTree(User user) {
        List<MibObject> rootObjects = new ArrayList<>();
        
        // Create ISO root (1)
        MibObject iso = createStandardMibObject("iso", "1", "ISO root", null, user);
        rootObjects.add(iso);
        
        // Create ORG (1.3)
        MibObject org = createStandardMibObject("org", "1.3", "Organization", iso, user);
        
        // Create DOD (1.3.6)
        MibObject dod = createStandardMibObject("dod", "1.3.6", "US Department of Defense", org, user);
        
        // Create Internet (1.3.6.1)
        MibObject internet = createStandardMibObject("internet", "1.3.6.1", "Internet", dod, user);
        
        // Create standard branches
        createStandardMibObject("directory", "1.3.6.1.1", "Directory", internet, user);
        createStandardMibObject("mgmt", "1.3.6.1.2", "Management", internet, user);
        createStandardMibObject("experimental", "1.3.6.1.3", "Experimental", internet, user);
        createStandardMibObject("private", "1.3.6.1.4", "Private", internet, user);
        createStandardMibObject("security", "1.3.6.1.5", "Security", internet, user);
        createStandardMibObject("snmpV2", "1.3.6.1.6", "SNMPv2", internet, user);
        
        // Create MIB-2 (1.3.6.1.2.1)
        MibObject mib2 = createStandardMibObject("mib-2", "1.3.6.1.2.1", "MIB-2", 
                                                 findObjectByOid("1.3.6.1.2", rootObjects), user);
        
        // Create system group (1.3.6.1.2.1.1)
        MibObject system = createStandardMibObject("system", "1.3.6.1.2.1.1", "System group", mib2, user);
        
        // Create system objects
        createStandardMibObject("sysDescr", "1.3.6.1.2.1.1.1.0", "System Description", system, user);
        createStandardMibObject("sysObjectID", "1.3.6.1.2.1.1.2.0", "System Object ID", system, user);
        createStandardMibObject("sysUpTime", "1.3.6.1.2.1.1.3.0", "System Up Time", system, user);
        createStandardMibObject("sysContact", "1.3.6.1.2.1.1.4.0", "System Contact", system, user);
        createStandardMibObject("sysName", "1.3.6.1.2.1.1.5.0", "System Name", system, user);
        createStandardMibObject("sysLocation", "1.3.6.1.2.1.1.6.0", "System Location", system, user);
        createStandardMibObject("sysServices", "1.3.6.1.2.1.1.7.0", "System Services", system, user);
        
        return rootObjects;
    }

    /**
     * Create a standard MIB object
     */
    private MibObject createStandardMibObject(String name, String oid, String description, MibObject parent, User user) {
        MibObject object = MibObject.builder()
                .name(name)
                .oid(oid)
                .description(description)
                .type(oid.endsWith(".0") ? MibObject.MibType.OBJECT_TYPE : MibObject.MibType.OBJECT_IDENTITY)
                .access(oid.endsWith(".0") ? MibObject.MibAccess.READ_ONLY : MibObject.MibAccess.NOT_ACCESSIBLE)
                .status(MibObject.MibStatus.CURRENT)
                .syntaxType(oid.endsWith(".0") ? "OCTET STRING" : "OBJECT IDENTIFIER")
                .parent(parent)
                .user(user)
                .build();
        
        object = mibObjectRepository.save(object);
        
        if (parent != null) {
            parent.getChildren().add(object);
        }
        
        return object;
    }

    /**
     * Find object by OID in a list of objects
     */
    private MibObject findObjectByOid(String oid, List<MibObject> objects) {
        return objects.stream()
                .filter(obj -> oid.equals(obj.getOid()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Convert MIB object to hierarchical DTO with children
     */
    private MibObjectDto convertToHierarchicalDto(MibObject mibObject) {
        MibObjectDto dto = mibObjectMapper.toDto(mibObject);
        
        // Convert children recursively
        List<MibObjectDto> childrenDtos = mibObject.getChildren().stream()
                .map(this::convertToHierarchicalDto)
                .collect(Collectors.toList());
        
        dto.setChildren(childrenDtos);
        return dto;
    }

    /**
     * Create SNMP target from browser request
     */
    private CommunityTarget<Address> createSnmpTarget(MibBrowserRequest request) {
        CommunityTarget<Address> target = new CommunityTarget<>();
        target.setCommunity(new OctetString(request.getCommunity()));
        target.setAddress(GenericAddress.parse("udp:" + request.getTargetIp() + "/" + request.getSnmpPort()));
        target.setRetries(request.getRetries());
        target.setTimeout(request.getTimeout());
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    /**
     * Get MIB object name from OID
     */
    private String getMibObjectName(String oid, User user) {
        if (user != null) {
            Optional<MibObject> mibObject = mibObjectRepository.findByOid(oid);
            if (mibObject.isPresent()) {
                return mibObject.get().getName();
            }
        }
        return null; // Will show only OID in response
    }

    /**
     * Determine syntax type from SNMP variable
     */
    private String determineSyntax(Variable variable) {
        if (variable instanceof Integer32) return "Integer32";
        if (variable instanceof OctetString) return "OCTET STRING";
        if (variable instanceof OID) return "OBJECT IDENTIFIER";
        if (variable instanceof Counter32) return "Counter32";
        if (variable instanceof Counter64) return "Counter64";
        if (variable instanceof Gauge32) return "Gauge32";
        if (variable instanceof TimeTicks) return "TimeTicks";
        if (variable instanceof UnsignedInteger32) return "Unsigned32";
        return variable.getClass().getSimpleName();
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

    private Path saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(System.getProperty("user.dir"), mibUploadDir);
        Files.createDirectories(uploadDir);

        String safeName = System.currentTimeMillis() + "_" +
                FilenameUtils.getName(file.getOriginalFilename());
        Path target = uploadDir.resolve(safeName);

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target);   // keeps Tomcat temp file alive once
        }
        return target;
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
}
