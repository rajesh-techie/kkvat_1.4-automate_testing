package com.kkvat.automation.service;

import com.kkvat.automation.dto.MenuItemDTO;
import com.kkvat.automation.entity.MenuItem;
import com.kkvat.automation.model.Role;
import com.kkvat.automation.model.User;
import com.kkvat.automation.repository.MenuItemRepository;
import com.kkvat.automation.repository.RoleRepository;
import com.kkvat.automation.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuItemService {
    
    @Autowired
    private MenuItemRepository menuItemRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all menu items
     */
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }
    
    /**
     * Get all active menu items
     */
    public List<MenuItem> getAllActiveMenuItems() {
        return menuItemRepository.findAll().stream()
            .filter(MenuItem::getIsActive)
            .toList();
    }
    
    /**
     * Get menu items by menu ID
     */
    public Optional<MenuItem> getMenuItemById(Long id) {
        return menuItemRepository.findById(id);
    }
    
    /**
     * Get menu items by name
     */
    public Optional<MenuItem> getMenuItemByName(String name) {
        return menuItemRepository.findByName(name);
    }
    
    /**
     * Get root level menu items (parent_menu_item_id is NULL)
     */
    public List<MenuItem> getRootMenuItems() {
        return menuItemRepository.findByParentMenuItemIsNullAndIsActiveTrueOrderByMenuOrder();
    }
    
    /**
     * Get child menu items for a parent
     */
    public List<MenuItem> getChildMenuItems(Long parentMenuItemId) {
        return menuItemRepository.findByParentMenuItemIdAndIsActiveTrueOrderByMenuOrder(parentMenuItemId);
    }
    
    /**
     * Get menu items by role ID (role-based access control)
     */
    public List<MenuItem> getMenuItemsByRoleId(Long roleId) {
        return menuItemRepository.findMenuItemsByRoleId(roleId);
    }
    
    /**
     * Get menu items by role name
     */
    public List<MenuItem> getMenuItemsByRoleName(String roleName) {
        return menuItemRepository.findMenuItemsByRoleName(roleName);
    }
    
    /**
     * Get menu items for a specific user based on their roles
     * This is the key method used at login time
     */
    public List<MenuItemDTO> getMenuItemsByUserId(Long userId) {
        // Get user and their roles
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        // Get all menu items the user's roles have access to
        Set<MenuItem> userMenuItems = new HashSet<>();
        
        // If user has enum-based role, map it to role entity
        if (user.getRole() != null) {
            List<MenuItem> menuItems = getMenuItemsByRoleName(user.getRole().name());
            userMenuItems.addAll(menuItems);
        }
        
        // Remove duplicates and sort
        List<MenuItem> sortedMenuItems = userMenuItems.stream()
            .distinct()
            .sorted(Comparator.comparingInt(MenuItem::getMenuOrder))
            .toList();
        
        // Convert to DTOs without parent/child relationships for API response
        return convertToFlatDTO(sortedMenuItems);
    }
    
    /**
     * Get menu items for user (including hierarchical structure)
     */
    public List<MenuItemDTO> getMenuItemsHierarchicalByUserId(Long userId) {
        List<MenuItemDTO> flatMenus = getMenuItemsByUserId(userId);
        
        // Build hierarchical structure: root items with their children
        Map<Long, List<MenuItemDTO>> childrenMap = new HashMap<>();
        List<MenuItemDTO> rootMenus = new ArrayList<>();
        
        for (MenuItemDTO item : flatMenus) {
            if (item.getParentMenuItemId() == null) {
                rootMenus.add(item);
            } else {
                childrenMap.computeIfAbsent(item.getParentMenuItemId(), k -> new ArrayList<>())
                    .add(item);
            }
        }
        
        // Assign children to parents
        for (MenuItemDTO root : rootMenus) {
            root.setChildMenuItems(new HashSet<>(childrenMap.getOrDefault(root.getId(), new ArrayList<>())));
        }
        
        return rootMenus;
    }
    
    /**
     * Create new menu item
     */
    public MenuItem createMenuItem(MenuItem menuItem) {
        menuItem.setCreatedAt(LocalDateTime.now());
        menuItem.setUpdatedAt(LocalDateTime.now());
        menuItem.setIsActive(true);
        return menuItemRepository.save(menuItem);
    }
    
    /**
     * Update menu item
     */
    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        return menuItemRepository.findById(id).map(menuItem -> {
            menuItem.setName(menuItemDetails.getName());
            menuItem.setDisplayName(menuItemDetails.getDisplayName());
            menuItem.setDescription(menuItemDetails.getDescription());
            menuItem.setRouteLink(menuItemDetails.getRouteLink());
            menuItem.setIconName(menuItemDetails.getIconName());
            menuItem.setParentMenuItem(menuItemDetails.getParentMenuItem());
            menuItem.setMenuOrder(menuItemDetails.getMenuOrder());
            menuItem.setIsActive(menuItemDetails.getIsActive());
            menuItem.setUpdatedAt(LocalDateTime.now());
            return menuItemRepository.save(menuItem);
        }).orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }
    
    /**
     * Delete menu item
     */
    public void deleteMenuItem(Long id) {
        menuItemRepository.deleteById(id);
    }
    
    /**
     * Deactivate menu item
     */
    public MenuItem deactivateMenuItem(Long id) {
        return menuItemRepository.findById(id).map(menuItem -> {
            menuItem.setIsActive(false);
            menuItem.setUpdatedAt(LocalDateTime.now());
            return menuItemRepository.save(menuItem);
        }).orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }
    
    /**
     * Activate menu item
     */
    public MenuItem activateMenuItem(Long id) {
        return menuItemRepository.findById(id).map(menuItem -> {
            menuItem.setIsActive(true);
            menuItem.setUpdatedAt(LocalDateTime.now());
            return menuItemRepository.save(menuItem);
        }).orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + id));
    }
    
    /**
     * Assign menu item to role
     */
    public void assignMenuItemToRole(Long roleId, Long menuItemId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + menuItemId));
        
        if (!role.getMenuItems().contains(menuItem)) {
            role.getMenuItems().add(menuItem);
            roleRepository.save(role);
        }
    }
    
    /**
     * Remove menu item from role
     */
    public void removeMenuItemFromRole(Long roleId, Long menuItemId) {
        Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with id: " + roleId));
        
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new RuntimeException("MenuItem not found with id: " + menuItemId));
        
        role.getMenuItems().remove(menuItem);
        roleRepository.save(role);
    }
    
    /**
     * Convert MenuItem entity to DTO
     */
    private MenuItemDTO convertToDTO(MenuItem menuItem) {
        MenuItemDTO dto = new MenuItemDTO();
        dto.setId(menuItem.getId());
        dto.setName(menuItem.getName());
        dto.setDisplayName(menuItem.getDisplayName());
        dto.setRouteLink(menuItem.getRouteLink());
        dto.setIconName(menuItem.getIconName());
        dto.setParentMenuItemId(menuItem.getParentMenuItem() != null ? menuItem.getParentMenuItem().getId() : null);
        dto.setMenuOrder(menuItem.getMenuOrder());
        dto.setIsActive(menuItem.getIsActive());
        dto.setCreatedAt(menuItem.getCreatedAt());
        return dto;
    }
    
    /**
     * Convert list of MenuItem entities to flat DTOs
     */
    private List<MenuItemDTO> convertToFlatDTO(List<MenuItem> menuItems) {
        return menuItems.stream()
            .map(this::convertToDTO)
            .sorted(Comparator.comparingInt(MenuItemDTO::getMenuOrder))
            .collect(Collectors.toList());
    }
}
