# Dynamic Entity Generator — Plan

## Overview
Build a generator that creates a Menu Item, frontend pages, backend APIs, DB tables/procedures/triggers, and wiring from a single `entity_management` landing page form. Inputs include entity metadata, columns, workflow/archive/view settings, parent menu and roles. The generator will perform DB DDL, scaffold backend Java sources, scaffold Angular frontend files, create stored procedures/triggers/views, and wire permissions and audit logging.

---

## High-level steps
1. Add `entity_management` menu item and grant Admin permission.
2. Persist generator configuration (table `entity_management`) and provide CRUD APIs.
3. Landing page UI: form + table view + Clear + Generate buttons.
4. Backend CRUD and orchestration endpoints.
5. Generation pipeline: DDL, stored procedures, views, triggers, workflow/audit tables.
6. Code generation: backend (entity/repo/service/controller/dto) + frontend (components/services/routes).
7. Wire pagination, search, role-based permissions and audit logging.
8. Tests, docs, idempotence and rollback scripts.

---

## Detailed Plan

### 1) Add Menu Item & Admin Permission
- Insert a `menu_items` row: `Entity Management` (route `/entity-management`), parent `Administration` (or provided parent).
- Insert into `role_menu_items` for admin role (lookup `roles` table by name).
- Implement as a small SQL migration and optional runtime endpoint.

### 2) Persist Generator Config
- Create DB table `entity_management` with columns:
  - `id`, `created_by`, `created_at`, `entity_name`, `entity_table_name`, `columns_json`, `workflow_config_json`, `archive_config_json`, `view_config_json`, `parent_menu`, `allowed_roles_json`, `ui_options_json`, `status`, `updated_at`.
- Backend: `EntityManagement` JPA entity + `EntityManagementRepository`.
- Provide CRUD endpoints: GET list, GET/{id}, POST, PUT, DELETE.

### 3) Landing Page UI
- Route: `/entity-management`.
- Component: `entity-management.component.ts/.html/.scss` with:
  - Form for all inputs you listed (entity_name, table_name, column blocks, workflow flags, archive/view config, parent_menu, roles).
  - Table to list/add/edit columns (editable rows with column properties & JSON answers field).
  - Buttons: `Clear` (reset form) and `Generate Entity` (POST config to backend `/api/entity-management/generate`).
  - Preview pane showing SQL and files that will be created.
- Service: `entity-management.service.ts` for CRUD + generate API.

### 4) Backend CRUD & Generate API
- Controller: `EntityManagementController` with endpoints
  - `GET /api/entity-management` (list)
  - `POST /api/entity-management` (create)
  - `PUT /api/entity-management/{id}` (update)
  - `DELETE /api/entity-management/{id}` (delete)
  - `POST /api/entity-management/generate` (trigger generation)
- Service: `EntityManagementService` orchestrates validation, persistence and generation.
- Generation returns a task id; service records status (PENDING → IN_PROGRESS → COMPLETED/FAILED).

### 5) Generation Actions (pipeline)
For a validated config, perform actions (idempotent checks and produce rollback script):
A. Create main table: `CREATE TABLE <entity_table_name> (...)` using column definitions (types, lengths, PKs, indexes).
B. Create archive table (if requested).
C. Create stored procedure(s):
   - `sp_move_to_archive_<entity>`: moves rows from main → archive using configured criteria.
   - `sp_delete_from_archive_<entity>`: deletes rows from archive older than policy.
D. Create DB view(s) per config.
E. Create workflow tables when enabled: `{{entity}}_workflow_instance`, `{{entity}}_workflow_status`, `{{entity}}_workflow_history`; seed status values.
F. Create audit table and trigger to copy final-state rows into audit on workflow completion.
G. Insert menu item + role permission.

Notes:
- Use prepared statements for DDL where possible and escape/validate inputs strictly.
- Save generated SQL and created-file manifest to the generation record for audit and rollback.

### 6) Generate Backend Code
- Use server-side templates (Freemarker or simple string templates) to produce Java source files under `backend/src/main/java/com/kkvat/automation/generated/<entity>/`:
  - `Generated<Entity>.java` (JPA entity)
  - `Generated<Entity>Dto.java`, `Create/Update DTOs`
  - `Generated<Entity>Repository extends JpaRepository`
  - `Generated<Entity>Service` (CRUD + pagination)
  - `Generated<Entity>Controller` (REST endpoints for CRUD + pagination)
- Generation strategy options:
  - Simple: write files and instruct developer to rebuild (`mvn package`) to pick up generated code.
  - Advanced: produce a separate module (or dynamic loader) — more complex.

### 7) Generate Frontend Code
- Create Angular module and components under `frontend/kkvat-frontend/src/app/modules/<entity>/`:
  - `list` component (table with pagination + search)
  - `edit` component (form for create/edit)
  - service `generated-<entity>.service.ts` to call backend endpoints
  - route entries added to `app.routes.ts`
- Reuse existing styles and components (users UI) to maintain consistent theme.
- Wire JWT auth header and role-based UI control.

### 8) Pagination & Search
- Backend: use Spring Data `Pageable` and a `Specification`/criteria builder for dynamic filters (based on `is_part_of_search`).
- Frontend: server-side pagination controls sending page/size/filter params to backend.

### 9) Workflow & Triggers
- If `do_we_need_workflow` enabled, create workflow instance and status tables and endpoints to advance workflow.
- Triggers: add DB trigger to insert into audit table when workflow status becomes `COMPLETED` (or final statuses).

### 10) Stored Procedures & Scheduled Jobs
- Create SPs using `criteria_fields` / `criteria_values` for moving and deleting data.
- Optionally add a scheduler entry (DB or application-level scheduled job) to invoke SPs periodically (monthly or per configured months).

### 11) Security, Audit & Validation
- Validate all inputs on backend to avoid injection and invalid DDL.
- Add entries to `audit_logs` for generation operations with created artifacts and user.
- Insert `role_menu_items` for roles specified in `which_role_is_eligible`.

### 12) Safety & Rollback
- Always check existence of tables/files and require explicit `force=true` to overwrite.
- Produce rollback SQL and file-delete script and attach to generation record.

### 13) Tests & Docs
- Unit tests for the DDL builder, JSON parsing and action executor.
- Integration test: generate a test entity, run CRUD sequence, run SPs, validate archive/audit.
- Documentation: README describing generator UI, manual rebuild steps and safety notes.

---

## Deliverables
- `docs/entity-generator-plan.md` (this file)
- DB migration SQL for `entity_management` table and menu insert
- Backend: `EntityManagement` entity + repository + controller + service + generation engine
- Frontend: `entity-management` component + service + route
- Example generated artifact (SQL + sample generated entity backend + frontend)
- Rollback scripts, tests and README

---

## Phases & Estimates (rough)
- Phase A (menu + generator CRUD + landing page stub): 1–2 days
- Phase B (generation engine: DB DDL, stored procs, menu insertion, basic backend scaffolding): 3–5 days
- Phase C (frontend scaffolding, pagination, workflow/audit, triggers, tests & docs): 3–5 days
- Total: ~1–2 weeks depending on polish and automation level

---

## Next Step
If you confirm, I will start with Step 1: add `entity_management` menu item, grant admin permissions, and create the `entity_management` DB table + backend CRUD API and landing page stub. I will commit and push incremental changes as I progress.

Please confirm or provide preferred `parent_menu` and `admin role name` (defaults: `Administration` and `admin`).
