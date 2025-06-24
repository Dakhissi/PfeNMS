package com.farukgenc.boilerplate.springboot.controller;

import com.farukgenc.boilerplate.springboot.dto.*;
import com.farukgenc.boilerplate.springboot.model.User;
import com.farukgenc.boilerplate.springboot.service.browser.MibService;
import com.farukgenc.boilerplate.springboot.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/mib")
@RequiredArgsConstructor
@Tag(name = "MIB Management", description = "APIs for managing SNMP MIB files and objects")
public class MibController {

    private final MibService mibService;

    @PostMapping("/files/upload")
    @Operation(summary = "Upload MIB file", description = "Upload and parse a MIB file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "MIB file uploaded successfully",
                    content = @Content(schema = @Schema(implementation = MibFileDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid file or parsing error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "409", description = "File already exists")
    })
    public ResponseEntity<MibFileDto> uploadMibFile(
            @Parameter(description = "MIB file to upload") @RequestParam("file") MultipartFile file) {

        User user = SecurityUtils.getCurrentUser();
        MibFileDto mibFileDto = mibService.uploadMibFile(file, user);
        return new ResponseEntity<>(mibFileDto, HttpStatus.CREATED);
    }

    @GetMapping("/files")
    @Operation(summary = "Get MIB files", description = "Get all MIB files for the user")
    @ApiResponse(responseCode = "200", description = "MIB files retrieved successfully")
    public ResponseEntity<List<MibFileDto>> getMibFiles() {
        User user = SecurityUtils.getCurrentUser();
        List<MibFileDto> mibFiles = mibService.getMibFilesByUser(user);
        return ResponseEntity.ok(mibFiles);
    }

    @GetMapping("/files/{id}")
    @Operation(summary = "Get MIB file by ID", description = "Get a specific MIB file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB file found",
                    content = @Content(schema = @Schema(implementation = MibFileDto.class))),
            @ApiResponse(responseCode = "404", description = "MIB file not found")
    })
    public ResponseEntity<MibFileDto> getMibFileById(
            @Parameter(description = "MIB file ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        MibFileDto mibFile = mibService.getMibFileById(id, user);
        return ResponseEntity.ok(mibFile);
    }

    @DeleteMapping("/files/{id}")
    @Operation(summary = "Delete MIB file", description = "Delete a MIB file and all its objects")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "MIB file deleted successfully"),
            @ApiResponse(responseCode = "404", description = "MIB file not found")
    })
    public ResponseEntity<Void> deleteMibFile(
            @Parameter(description = "MIB file ID") @PathVariable Long id) {

        User user = SecurityUtils.getCurrentUser();
        mibService.deleteMibFile(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tree")
    @Operation(summary = "Get MIB tree", description = "Get the complete MIB object tree for the user")
    @ApiResponse(responseCode = "200", description = "MIB tree retrieved successfully")
    public ResponseEntity<List<MibObjectDto>> getMibTree() {
        User user = SecurityUtils.getCurrentUser();
        List<MibObjectDto> mibTree = mibService.getMibTree(user);
        return ResponseEntity.ok(mibTree);
    }

    @GetMapping("/tree/file/{fileId}")
    @Operation(summary = "Get MIB tree by file", description = "Get MIB object tree for a specific file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB tree retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "MIB file not found")
    })
    public ResponseEntity<List<MibObjectDto>> getMibTreeByFile(
            @Parameter(description = "MIB file ID") @PathVariable Long fileId) {

        User user = SecurityUtils.getCurrentUser();
        List<MibObjectDto> mibTree = mibService.getMibTreeByFile(fileId, user);
        return ResponseEntity.ok(mibTree);
    }

    @GetMapping("/objects/oid/{oid}")
    @Operation(summary = "Get MIB object by OID", description = "Get a specific MIB object by its OID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "MIB object found",
                    content = @Content(schema = @Schema(implementation = MibObjectDto.class))),
            @ApiResponse(responseCode = "404", description = "MIB object not found")
    })
    public ResponseEntity<MibObjectDto> getMibObjectByOid(
            @Parameter(description = "Object Identifier") @PathVariable String oid) {

        User user = SecurityUtils.getCurrentUser();
        MibObjectDto mibObject = mibService.getMibObjectByOid(oid, user);
        return ResponseEntity.ok(mibObject);
    }

    @GetMapping("/objects/search")
    @Operation(summary = "Search MIB objects", description = "Search MIB objects by name or OID")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<List<MibObjectDto>> searchMibObjects(
            @Parameter(description = "Search query") @RequestParam String query) {

        User user = SecurityUtils.getCurrentUser();
        List<MibObjectDto> results = mibService.searchMibObjects(query, user);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/browse")
    @Operation(summary = "Browse SNMP OID", description = "Test an OID against a target device")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP query completed",
                    content = @Content(schema = @Schema(implementation = MibBrowserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<MibBrowserResponse> browseOid(
            @Valid @RequestBody MibBrowserRequest request) {

        MibBrowserResponse response = mibService.browseOid(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/walk")
    @Operation(summary = "Walk SNMP OID tree", description = "Walk the OID tree starting from the specified OID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SNMP walk completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
    public ResponseEntity<List<MibBrowserResponse>> walkOidTree(
            @Valid @RequestBody MibBrowserRequest request) {

        List<MibBrowserResponse> responses = mibService.walkOidTree(request);
        return ResponseEntity.ok(responses);
    }
}
