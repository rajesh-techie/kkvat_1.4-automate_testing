package com.kkvat.automation.repository;

import com.kkvat.automation.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    
    Optional<MenuItem> findByName(String name);
    
    List<MenuItem> findByParentMenuItemIsNullAndIsActiveTrueOrderByMenuOrder();
    
    List<MenuItem> findByParentMenuItemIdAndIsActiveTrueOrderByMenuOrder(Long parentMenuItemId);
    
    @Query("SELECT DISTINCT m FROM MenuItem m " +
           "JOIN m.roles r " +
           "WHERE r.id = :roleId AND m.isActive = true " +
           "ORDER BY m.parentMenuItem.id, m.menuOrder")
    List<MenuItem> findMenuItemsByRoleId(@Param("roleId") Long roleId);
    
    @Query("SELECT DISTINCT m FROM MenuItem m " +
           "JOIN m.roles r " +
           "WHERE r.name = :roleName AND m.isActive = true " +
           "ORDER BY m.parentMenuItem.id, m.menuOrder")
    List<MenuItem> findMenuItemsByRoleName(@Param("roleName") String roleName);
}
