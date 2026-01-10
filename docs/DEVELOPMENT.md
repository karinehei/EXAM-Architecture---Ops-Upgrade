# Development Guide

This guide provides information for developers working on the EXAM system.

## Development Workflow

### Getting Started

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd "EXAM Architecture & Ops Upgrade"
   ```

2. **Set up environment**:
   ```bash
   cp .env.example .env
   # Edit .env with your settings
   ```

3. **Start development environment**:
   ```bash
   make dev
   ```

4. **Verify everything is running**:
   - Frontend: http://localhost:4200
   - Backend: http://localhost:9000
   - Health check: http://localhost:9000/health

### Development Mode

The system runs in development mode with:
- Hot reload for frontend (Angular dev server)
- Backend restarts on code changes (Play Framework)
- Dev authentication enabled (if configured)
- Detailed error messages and stack traces

## Code Style Guidelines

### Backend (Scala/Play Framework)

- **Formatting**: Follow Scala style guide conventions
- **Naming**: Use camelCase for variables, PascalCase for classes
- **Imports**: Organize imports (standard library, third-party, local)
- **Documentation**: Add Scaladoc comments for public APIs
- **Error Handling**: Use `Future` and proper error recovery
- **Dependency Injection**: Use Play's dependency injection (Guice)

**Example**:
```scala
package controllers

import javax.inject._
import play.api.mvc._

/**
 * Health check controller
 * Provides liveness and readiness endpoints
 */
@Singleton
class HealthController @Inject()(
  val controllerComponents: ControllerComponents
) extends BaseController {
  // Implementation
}
```

### Frontend (Angular/TypeScript)

- **Formatting**: Use Angular CLI default formatting
- **Naming**: Use camelCase for variables, PascalCase for classes/components
- **File Structure**: One component per file, related files in same directory
- **Documentation**: Add JSDoc comments for public methods
- **Type Safety**: Use TypeScript types, avoid `any`
- **Components**: Keep components focused and small

**Example**:
```typescript
import { Component, OnInit } from '@angular/core';

/**
 * Report list component
 * Displays a paginated list of reports
 */
@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css'],
  standalone: false
})
export class ReportListComponent implements OnInit {
  // Implementation
}
```

## Testing Practices

### Backend Testing

- **Unit Tests**: Test individual methods and functions
- **Integration Tests**: Test controller endpoints with test database
- **Test Database**: Use H2 in-memory database for tests
- **Test Coverage**: Aim for >80% code coverage

**Running Tests**:
```bash
cd backend
sbt test
```

**Example Test**:
```scala
package controllers

import org.scalatestplus.play._
import play.api.test._
import play.api.test.Helpers._

class ReportControllerSpec extends PlaySpec with OneAppPerTest {
  "ReportController" should {
    "return paginated list of reports" in {
      val request = FakeRequest(GET, "/api/v1/reports")
      val response = route(app, request).get
      status(response) mustBe OK
    }
  }
}
```

### Frontend Testing

- **Unit Tests**: Test component logic and services
- **Accessibility Tests**: Use axe-core for a11y testing
- **E2E Tests**: Use Angular testing utilities

**Running Tests**:
```bash
cd frontend
npm test
```

**Example Test**:
```typescript
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReportListComponent } from './report-list.component';

describe('ReportListComponent', () => {
  let component: ReportListComponent;
  let fixture: ComponentFixture<ReportListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ReportListComponent]
    }).compileComponents();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
```

## Git Workflow

### Branching Strategy

- **main**: Production-ready code
- **feature/**: New features
- **fix/**: Bug fixes
- **docs/**: Documentation updates

### Commit Messages

Follow conventional commits format:
```
type(scope): subject

body (optional)

footer (optional)
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation
- `style`: Code style changes
- `refactor`: Code refactoring
- `test`: Test additions/changes
- `chore`: Build/tooling changes

**Examples**:
```
feat(api): add pagination to reports endpoint

fix(security): correct rate limit header calculation

docs(readme): update project structure section
```

### Pull Request Process

1. **Create feature branch**:
   ```bash
   git checkout -b feature/my-feature
   ```

2. **Make changes and commit**:
   ```bash
   git add .
   git commit -m "feat(scope): description"
   ```

3. **Push and create PR**:
   ```bash
   git push origin feature/my-feature
   # Create PR on GitHub
   ```

4. **Ensure CI passes**:
   - All tests must pass
   - Code must build successfully
   - Linter checks must pass

5. **Code review**:
   - Address review comments
   - Update PR as needed

6. **Merge**:
   - Squash and merge (preferred)
   - Or merge commit

## Code Review Guidelines

### What to Review

- **Functionality**: Does it work as intended?
- **Code Quality**: Is it readable and maintainable?
- **Tests**: Are there adequate tests?
- **Documentation**: Is it documented?
- **Security**: Any security concerns?
- **Performance**: Any performance issues?

### Review Checklist

- [ ] Code follows style guidelines
- [ ] Tests are included and passing
- [ ] Documentation is updated
- [ ] No security vulnerabilities introduced
- [ ] No breaking changes (or documented)
- [ ] CI pipeline passes

## Debugging

### Backend Debugging

**View logs**:
```bash
make logs-backend
# Or
docker compose logs -f backend
```

**Debug in IDE**:
- Attach debugger to running Play application
- Set breakpoints in Scala code
- Use Play's development mode for hot reload

**Database debugging**:
```bash
make shell-db
# Or
docker compose exec postgres psql -U exam_user -d exam
```

### Frontend Debugging

**View logs**:
```bash
make logs-frontend
# Or browser DevTools console
```

**Debug in browser**:
- Use browser DevTools
- Angular DevTools extension
- Network tab for API calls
- Console for errors

## Performance Considerations

### Backend

- **Database Queries**: Use indexes, avoid N+1 queries
- **Caching**: Consider caching for frequently accessed data
- **Connection Pooling**: Configured via Slick
- **Async Operations**: Use `Future` for I/O operations

### Frontend

- **Lazy Loading**: Use Angular lazy loading for routes
- **Change Detection**: Components use OnPush strategy where possible
- **Bundle Size**: Monitor bundle size, use tree-shaking
- **API Calls**: Minimize API calls, use pagination

## Security Best Practices

### Backend

- **Input Validation**: Validate all user input
- **SQL Injection**: Use parameterized queries (Slick handles this)
- **XSS Prevention**: Sanitize output
- **Authentication**: Never use dev auth in production
- **Secrets**: Never commit secrets to version control

### Frontend

- **XSS Prevention**: Angular's built-in sanitization
- **HTTPS**: Always use HTTPS in production
- **CSP**: Content Security Policy headers (configured in backend)
- **Dependencies**: Regularly update and audit dependencies

## Common Tasks

### Adding a New API Endpoint

1. **Add route** in `backend/conf/routes`
2. **Create controller method** in appropriate controller
3. **Add DTOs** if needed in `backend/app/dto/`
4. **Update repository** if database access needed
5. **Add tests** in `backend/test/controllers/`
6. **Update OpenAPI spec** in `docs/api/openapi.yaml`

### Adding a New Frontend Component

1. **Generate component**:
   ```bash
   cd frontend
   ng generate component component-name
   ```

2. **Add to module** in `app.module.ts`
3. **Add routing** if needed
4. **Add tests** in component spec file
5. **Add accessibility** attributes

### Database Migration

1. **Create migration** in `database/migrations/`
2. **Add evolution** in `backend/conf/evolutions/default/`
3. **Test migration** on local database
4. **Document changes** in migration file

## Troubleshooting

### Common Issues

**Backend won't start**:
- Check database connection
- Verify environment variables
- Check for RUNNING_PID file (use `make clean-pid`)

**Frontend build fails**:
- Clear node_modules: `rm -rf node_modules && npm install`
- Check TypeScript errors
- Verify Angular version compatibility

**Database connection issues**:
- Verify PostgreSQL is running: `docker compose ps`
- Check connection string in `.env`
- Verify database exists

**Port conflicts**:
- Change ports in `.env`
- Or stop conflicting services

## Resources

- [Play Framework Documentation](https://www.playframework.com/documentation)
- [Angular Documentation](https://angular.io/docs)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Docker Documentation](https://docs.docker.com/)
- [OpenAPI Specification](https://swagger.io/specification/)

## Getting Help

- Check [QUICKSTART.md](QUICKSTART.md) for setup issues
- Review [API Documentation](api/README.md) for API questions
- Check [Security Documentation](security/README.md) for security questions
- Review logs: `make logs`
