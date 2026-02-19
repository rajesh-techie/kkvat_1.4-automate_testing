package com.kkvat.automation.service;

import com.kkvat.automation.dto.GroupRequest;
import com.kkvat.automation.dto.GroupResponse;
import com.kkvat.automation.exception.BadRequestException;
import com.kkvat.automation.exception.ResourceNotFoundException;
import com.kkvat.automation.model.Group;
import com.kkvat.automation.repository.GroupRepository;
import com.kkvat.automation.repository.UserRepository;
import com.kkvat.automation.model.User;
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
public class GroupService {
    
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    
    @Transactional(readOnly = true)
    public List<GroupResponse> getAllGroups() {
        log.debug("Fetching all groups");
        return groupRepository.findAll().stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
    }

        @Transactional
        public GroupResponse setGroupUsers(Long id, java.util.List<Long> userIds, Long updatedBy) {
        log.debug("Setting users for group id: {}", id);

        Group group = groupRepository.findById(id)
            .orElseThrow(() -> new com.kkvat.automation.exception.ResourceNotFoundException("Group not found with id: " + id));

        java.util.List<User> users = userRepository.findAllById(userIds == null ? java.util.Collections.emptyList() : userIds);

        // Replace group's users with the provided set
        java.util.Set<User> newUsers = new java.util.HashSet<>(users);
        group.setUsers(newUsers);

        group.setUpdatedBy(updatedBy);

        Group saved = groupRepository.save(group);

        auditService.logSuccess(
            "UPDATE_GROUP_USERS",
            "Group",
            saved.getId(),
            "Updated group users for group: " + saved.getName()
        );

        log.info("Group users updated successfully for group: {}", saved.getName());
        return GroupResponse.from(saved);
        }
    
    @Transactional(readOnly = true)
    public Page<GroupResponse> getAllGroups(Pageable pageable) {
        log.debug("Fetching groups with pagination: {}", pageable);
        return groupRepository.findAll(pageable)
                .map(GroupResponse::from);
    }
    
    @Transactional(readOnly = true)
    public GroupResponse getGroupById(Long id) {
        log.debug("Fetching group by id: {}", id);
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        return GroupResponse.from(group);
    }
    
    @Transactional(readOnly = true)
    public GroupResponse getGroupByName(String name) {
        log.debug("Fetching group by name: {}", name);
        Group group = groupRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with name: " + name));
        return GroupResponse.from(group);
    }
    
    @Transactional
    public GroupResponse createGroup(GroupRequest request, Long createdBy) {
        log.debug("Creating new group: {}", request.getName());
        
        // Check if group name already exists
        if (groupRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Group already exists with name: " + request.getName());
        }
        
        Group group = Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(createdBy)
                .build();
        
        Group savedGroup = groupRepository.save(group);
        
        auditService.logSuccess(
                "CREATE_GROUP",
                "Group",
                savedGroup.getId(),
                "Created group: " + savedGroup.getName()
        );
        
        log.info("Group created successfully: {}", savedGroup.getName());
        return GroupResponse.from(savedGroup);
    }
    
    @Transactional
    public GroupResponse updateGroup(Long id, GroupRequest request, Long updatedBy) {
        log.debug("Updating group with id: {}", id);
        
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        
        // Check if name is being changed and if it already exists
        if (!group.getName().equals(request.getName())) {
            if (groupRepository.findByName(request.getName()).isPresent()) {
                throw new BadRequestException("Group already exists with name: " + request.getName());
            }
            group.setName(request.getName());
        }
        
        group.setDescription(request.getDescription());
        
        if (request.getIsActive() != null) {
            group.setIsActive(request.getIsActive());
        }
        
        group.setUpdatedBy(updatedBy);
        
        Group updatedGroup = groupRepository.save(group);
        
        auditService.logSuccess(
                "UPDATE_GROUP",
                "Group",
                updatedGroup.getId(),
                "Updated group: " + updatedGroup.getName()
        );
        
        log.info("Group updated successfully: {}", updatedGroup.getName());
        return GroupResponse.from(updatedGroup);
    }
    
    @Transactional
    public void deleteGroup(Long id, Long deletedBy) {
        log.debug("Deleting group with id: {}", id);
        
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
        
        String groupName = group.getName();
        groupRepository.delete(group);
        
        auditService.logSuccess(
                "DELETE_GROUP",
                "Group",
                id,
                "Deleted group: " + groupName
        );
        
        log.info("Group deleted successfully: {}", groupName);
    }
    
    @Transactional(readOnly = true)
    public List<GroupResponse> searchGroups(String keyword) {
        log.debug("Searching groups with keyword: {}", keyword);
        return groupRepository.findByNameContaining(keyword)
                .stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GroupResponse> getActiveGroups() {
        log.debug("Fetching active groups");
        return groupRepository.findByIsActive(true)
                .stream()
                .map(GroupResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<com.kkvat.automation.dto.UserResponse> getUsersForGroup(Long groupId) {
        log.debug("Fetching users for group id: {}", groupId);
        return userRepository.findByGroupId(groupId).stream()
                .map(com.kkvat.automation.dto.UserResponse::from)
                .collect(Collectors.toList());
    }
}
