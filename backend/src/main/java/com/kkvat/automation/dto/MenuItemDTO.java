package com.kkvat.automation.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for MenuItem - used for API responses
 * Matches the 1.3 format: id, name, routeLink, parentMenuItemId
 */
public class MenuItemDTO {
    private Long id;
    private String name;
    private String displayName;
    private String routeLink;
    private String iconName;
    private Long parentMenuItemId;
    private Integer menuOrder;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Set<MenuItemDTO> childMenuItems;

    // Constructors
    public MenuItemDTO() {}

    public MenuItemDTO(Long id, String name, String displayName, String routeLink, 
                      String iconName, Long parentMenuItemId, Integer menuOrder, 
                      Boolean isActive) {
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.routeLink = routeLink;
        this.iconName = iconName;
        this.parentMenuItemId = parentMenuItemId;
        this.menuOrder = menuOrder;
        this.isActive = isActive;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRouteLink() {
        return routeLink;
    }

    public void setRouteLink(String routeLink) {
        this.routeLink = routeLink;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public Long getParentMenuItemId() {
        return parentMenuItemId;
    }

    public void setParentMenuItemId(Long parentMenuItemId) {
        this.parentMenuItemId = parentMenuItemId;
    }

    public Integer getMenuOrder() {
        return menuOrder;
    }

    public void setMenuOrder(Integer menuOrder) {
        this.menuOrder = menuOrder;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Set<MenuItemDTO> getChildMenuItems() {
        return childMenuItems;
    }

    public void setChildMenuItems(Set<MenuItemDTO> childMenuItems) {
        this.childMenuItems = childMenuItems;
    }
}
