# KKVat Automation Platform - Frontend Setup

## Angular 17 + TypeScript + Angular Material

### Prerequisites

- Node.js 18+ and npm
- Angular CLI 17

### Quick Setup

Run this PowerShell script to create the Angular project:

```powershell
# Navigate to project root
cd d:\python_programs_rajesh\kkvat_1.4-automate_testing

# Install Angular CLI globally (if not installed)
npm install -g @angular/cli@17

# Create Angular application
cd frontend
ng new kkvat-frontend --routing --style=scss --skip-git

# Navigate into the project
cd kkvat-frontend

# Install dependencies
npm install @angular/material @angular/cdk @angular/animations
npm install @ngrx/store @ngrx/effects @ngrx/store-devtools
npm install rxjs
npm install --save-dev @types/node

# Start the development server
ng serve
```

The application will be available at `http://localhost:4200`

### Project Structure (To Be Created)

```
src/
├── app/
│   ├── core/                    # Singleton services, guards, interceptors
│   │   ├── auth/
│   │   │   ├── auth.service.ts
│   │   │   ├── auth.guard.ts
│   │   │   └── token.interceptor.ts
│   │   ├── models/
│   │   └── services/
│   ├── shared/                  # Reusable components, directives, pipes
│   │   ├── components/
│   │   ├── directives/
│   │   └── pipes/
│   ├── features/
│   │   ├── dashboard/          # Dashboard module
│   │   ├── auth/               # Login/logout
│   │   ├── users/              # User management
│   │   ├── groups/             # Group management
│   │   ├── test-cases/         # Test case management
│   │   ├── reports/            # Reports
│   │   └── audit/              # Audit logs
│   └── layouts/                # Layout components
│       ├── header/
│       ├── sidebar/
│       └── footer/
├── assets/
├── environments/
└── styles/
```

### Features to Implement

#### Phase 1 - Authentication
- ✅ Login page
- ✅ JWT token management
- ✅ Auth guard
- ✅ HTTP interceptor
- ✅ Session handling

#### Phase 2 - User Management
- User CRUD operations
- Role management
- Password reset

#### Phase 3 - Test Automation
- Test case recording
- Test case management
- Test execution
- Results viewing

#### Phase 4 - Reporting
- Dynamic report builder
- Pre-defined templates
- Export functionality

### Running the Frontend

```bash
# Development server
ng serve

# Build for production
ng build --configuration production

# Run tests
ng test
```

### API Configuration

Update `src/environments/environment.ts`:

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api'
};
```

### Next Steps

After running the setup script above, we'll create:
1. Authentication module with login component
2. Core services and guards
3. HTTP interceptors
4. Material theme configuration
5. Basic layout structure

**Would you like me to proceed with creating the Angular project structure now?**
