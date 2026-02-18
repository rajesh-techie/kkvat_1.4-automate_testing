package com.kkvat.automation.service;

import com.kkvat.automation.entity.Role;
import com.kkvat.automation.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Get all roles
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Get all active roles
     */
    public List<Role> getAllActiveRoles() {
        return roleRepository.findAll().stream()
            .filter(Role::getIsActive)
            .toList();
    }
    
    /**
     * Get role by ID
     */
    public Optional<Role> getRoleById(Long id) {
        return roleRepository.findById(id);
    }
    
    /**
     * Get role by name
     */
    public Optional<Role> getRoleByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * Get role by name (active only)
     */
    public Optional<Role> getActiveRoleByName(String name) {
        return roleRepository.findByNameAndIsActiveTrue(name);
    }
    
    /**
     * Create new role
     */
    public Role createRole(Role role) {
        role.setCreatedAt(LocalDateTime.now());
        role.setUpdatedAt(LocalDateTime.now());
        role.setIsActive(true);
        return roleRepository.save(role);
    }
    
    /**
     * Update existing role
     */
    public Role updateRole(Long id, Role roleDetails) {
        return roleRepository.findById(id).map(role -> {
            role.setName(roleDetails.getName());
            role.setDescription(roleDetails.getDescription());
            role.setIsActive(roleDetails.getIsActive());
            role.setUpdatedAt(LocalDateTime.now());
            return roleRepository.save(role);
        }).orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }
    
    /**
     * Delete role by ID
     */
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
    
    /**
     * Deactivate role
     */
    public Role deactivateRole(Long id) {
        return roleRepository.findById(id).map(role -> {
            role.setIsActive(false);
            role.setUpdatedAt(LocalDateTime.now());
            return roleRepository.save(role);
        }).orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }
    
    /**
     * Activate role
     */
    public Role activateRole(Long id) {
        return roleRepository.findById(id).map(role -> {
            role.setIsActive(true);
            role.setUpdatedAt(LocalDateTime.now());
            return roleRepository.save(role);
        }).orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
    }
}
