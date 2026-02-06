package com.blockchainbiz.app.client_api.controller;


import com.blockchainbiz.app.client_api.dto.CertificateDto;
import com.blockchainbiz.app.client_api.dto.CertificateRequest;
import com.blockchainbiz.app.client_api.dto.UserDto;
import com.blockchainbiz.app.client_api.model.Certificate;
import com.blockchainbiz.app.client_api.model.CertificateDetails;
import com.blockchainbiz.app.client_api.model.User;
import com.blockchainbiz.app.client_api.service.CertificateService;
import com.blockchainbiz.app.client_api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CertificateService certificateService;


    @GetMapping("/{id}")
    public EntityModel<UserDto> getUser(@PathVariable Long id) {
        User user = userService.getUserById(id);

        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());


        Link updateLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .updateUser(id, user)).withRel("updateUser");

        return EntityModel.of(userDto, updateLink);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.getUserById(id);
        user.setEmail(updatedUser.getEmail());
        return userService.registerUser(user.getUsername(), user.getEmail(), updatedUser.getPassword());
    }

    @GetMapping("/certificate/{userId}")
    public EntityModel<CertificateDto> getCertificate(@PathVariable Long userId){
        Certificate certificate = certificateService.getCertificateByUserId(userId);

        User user = certificate.getUser();
        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail());

        CertificateDto certificateDto = new CertificateDto(
                certificate.getId(),
                userDto,
                certificate.getCertificate(),
                certificate.getCreatedAt()
        );

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getCertificate(userId)).withSelfRel();

        Link deleteLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .deleteCertificate(userId)).withRel("deleteCertificate");

        return EntityModel.of(certificateDto, selfLink, deleteLink);
    }

    @DeleteMapping("/certificate/{userId}")
    public ResponseEntity<?> deleteCertificate(@PathVariable Long userId) {
        certificateService.deleteCertificate(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/certificate/{userId}")
    public EntityModel<Certificate> addCertificate(@PathVariable Long userId, @RequestBody CertificateRequest request) {
        Certificate certificate = certificateService.addCertificate(userId, request.getCertificatePem());

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .getCertificate(userId)).withSelfRel();

        return EntityModel.of(certificate, selfLink);
    }

    @GetMapping("/{userId}/verify")
    public EntityModel<Map<String, Object>> verifyMessage(
            @PathVariable Long userId,
            @RequestParam String message,
            @RequestParam String signature) {
        boolean isValid = certificateService.verifyMessage(userId, message, signature);

        Map<String, Object> response = Map.of(
                "isValid", isValid,
                "message", isValid ? "Weryfikacja powiodła się" : "Nieprawidłowy podpis"
        );

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(UserController.class)
                .verifyMessage(userId, message, signature)).withSelfRel();

        return EntityModel.of(response, selfLink);
    }

    @GetMapping("/certificate/details/{userId}")
    public EntityModel<CertificateDetails> getCertificateDetails(@PathVariable Long userId) {
        Certificate certificate = certificateService.getCertificateByUserId(userId);

        CertificateDetails details = certificateService.extractCertificateDetails(certificate.getCertificate());

        return EntityModel.of(details);
    }
}
