package com.trackingpath.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.APISuccessReponse;
import com.trackingpath.dtos.SharePositionBean;
import com.trackingpath.entities.Users;
import com.trackingpath.exceptions.GenericException;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.SharePositionService;

@RestController
@RequestMapping("/share-positions")
public class SharePositionController {

    @Autowired
    private SharePositionService shareService;

    @Autowired
    private AuthenticationService authenticationService;

    // CREATE
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createShare(@RequestBody SharePositionBean bean) {
        Users user = authenticationService.getUser();
        Long id = shareService.createSharePosition(bean, user);
        return ResponseEntity.ok("Share created successfully, ID = " + id);
    }

    // UPDATE
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> updateShare(@RequestBody SharePositionBean bean) {
        Users user = authenticationService.getUser();
        shareService.updateShare(bean, user);
        return ResponseEntity.ok("Share updated successfully");
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteShare(@PathVariable Long id) {
        shareService.deleteShare(id);
        return ResponseEntity.ok("Share deleted successfully");
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<Page<SharePositionBean>> getAllShares(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String search
    ) {

        Users user = authenticationService.getUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                shareService.getAllShares(user, pageable, search)
        );
    }

    // READ BY ID
    @GetMapping("/{id}")
    public ResponseEntity<SharePositionBean> getShareById(@PathVariable Long id) {
        Users user = authenticationService.getUser();
        SharePositionBean bean = shareService.getShareById(id, user);
        return ResponseEntity.ok(bean);
    }


    // LIVE VIEW (SPECIAL RESOURCE)
    @GetMapping("/weblive")
    public ResponseEntity<APISuccessReponse> getWebLive(
            @RequestParam("uc") String uniqueCode) throws GenericException {

        APISuccessReponse response = shareService.getWebLiveData(uniqueCode);
        return ResponseEntity.ok(response);
    }
}
