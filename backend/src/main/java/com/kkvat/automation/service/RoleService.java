package com.kkvat.automation.service;

import com.kkvat.automation.dto.RoleRequest;
import com.kkvat.automation.dto.RoleResponse;
import com.kkvat.automation.exception.BadRequestException;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Role;
import com.kkvat.automation.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        return roleRepository.findAll().stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<RoleResponse> getAllRoles(Pageable pageable) {
        log.debug("Fetching roles with pagination: {}", pageable);
        return roleRepository.findAll(pageable).map(RoleResponse::from);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        log.debug("Fetching role by id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return RoleResponse.from(role);
    }

    @Transactional(readOnly = true)
    public RoleResponse getRoleByName(String name) {
        log.debug("Fetching role by name: {}", name);
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request, Long createdBy) {
        log.debug("Creating new role: {}", request.getName());

        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Role already exists with name: " + request.getName());
        }

        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(createdBy)
                .build();

        Role saved = roleRepository.save(role);

        auditService.logSuccess(
                "CREATE_ROLE",
                "Role",
                saved.getId(),
                "Created role: " + saved.getName()
        );

        log.info("Role created successfully: {}", saved.getName());
        return RoleResponse.from(saved);
    }

    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request, Long updatedBy) {
        log.debug("Updating role id {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (!role.getName().equals(request.getName())) {
            if (roleRepository.findByName(request.getName()).isPresent()) {
                throw new BadRequestException("Role already exists with name: " + request.getName());
            }
            role.setName(request.getName());
        }

        role.setDescription(request.getDescription());
        if (request.getIsActive() != null) role.setIsActive(request.getIsActive());
        role.setUpdatedBy(updatedBy);

        Role saved = roleRepository.save(role);

        auditService.logSuccess(
                "UPDATE_ROLE",
                "Role",
                saved.getId(),
                "Updated role: " + saved.getName()
        );

        log.info("Role updated successfully: {}", saved.getName());
        return RoleResponse.from(saved);
    }

    @Transactional
    public void deleteRole(Long id, Long deletedBy) {
        log.debug("Deleting role id: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        String name = role.getName();
        roleRepository.delete(role);

        auditService.logSuccess(
                "DELETE_ROLE",
                "Role",
                id,
                "Deleted role: " + name
        );

        log.info("Role deleted successfully: {}", name);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> searchRoles(String keyword) {
        log.debug("Searching roles with keyword: {}", keyword);
        return roleRepository.findByNameContaining(keyword).stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getActiveRoles() {
        log.debug("Fetching active roles");
        return roleRepository.findByIsActive(true).stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }
}
