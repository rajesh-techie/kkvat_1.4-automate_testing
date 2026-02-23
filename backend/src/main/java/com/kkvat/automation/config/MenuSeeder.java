package com.kkvat.automation.config;

import com.kkvat.automation.entity.MenuItem;
import com.kkvat.automation.model.Role;
import com.kkvat.automation.repository.MenuItemRepository;
import com.kkvat.automation.repository.RoleRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MenuSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuSeeder.class);

    private final MenuItemRepository menuItemRepository;
    private final RoleRepository roleRepository;

    public MenuSeeder(MenuItemRepository menuItemRepository, RoleRepository roleRepository) {
        this.menuItemRepository = menuItemRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        try {
            Optional<MenuItem> existing = menuItemRepository.findByName("entity-management");
            if (existing.isPresent()) {
                log.debug("'entity-management' menu already present (id={})", existing.get().getId());
                return;
            }

            MenuItem menu = new MenuItem();
            menu.setName("entity-management");
            menu.setDisplayName("Entity Management");
            menu.setDescription("Manage dynamic entity generator configurations");
            menu.setRouteLink("/entity-management");
            menu.setIconName("admin_panel_settings");
            menu.setMenuOrder(5);
            menu.setIsActive(true);

            // Try to assign parent menu item id 4 if it exists
            menuItemRepository.findById(4L).ifPresent(menu::setParentMenuItem);

            MenuItem saved = menuItemRepository.save(menu);
            log.info("Inserted menu item 'entity-management' with id {}", saved.getId());

            // Grant access to ADMIN role if present
            roleRepository.findByName("ADMIN").ifPresent(role -> {
                role.getMenuItems().add(saved);
                roleRepository.save(role);
                log.info("Assigned 'entity-management' menu to role ADMIN (role id={})", role.getId());
            });
        } catch (Exception ex) {
            log.error("MenuSeeder failed", ex);
        }
    }
}
