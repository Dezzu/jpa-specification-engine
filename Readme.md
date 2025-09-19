# JPA Specification Engine
A powerful Spring Boot library for building dynamic JPA queries with a fluent and intuitive API. Create complex database filters with ease using a flexible specification pattern that supports nested conditions, multiple operators, and automatic type handling.

## ❣️ Version and Compatibility
The current compatible version are: 
- Java `21`
- Spring Boot `3.5.5`

## 🚀 Features
- 🎯 Fluent API: Intuitive utility methods for creating filter criteria
- 🔧 Auto-Configuration: Seamless Spring Boot integration with zero configuration
- 📊 Advanced Operations: Support for 20+ filter operations including string matching, date ranges, and collections
- 🎪 Complex Queries: Multi-level grouping with AND/OR operators at different levels
- 🔍 Nested Field Access: Dot notation support for navigating entity relationships
- ⚡ Type Safety: Automatic type conversion and validation
- 🌐 Database Agnostic: Works with all JPA-supported databases
- 📈 Performance Optimized: Efficient query generation using JPA Criteria API

## 📦 Installation
Always check the latest available version.

### Maven
Add the dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>it.fabiodezuani.utils</groupId>
    <artifactId>jpa-specification-engine</artifactId>
    <version>LATEST-VERSION</version>
</dependency>
```

### Gradle
Add to your `build.gradle`:
```gradle
implementation 'it.fabiodezuani.utils:jpa-specification-engine:LATEST-VERSION'
```

## 🔧 Quick Start
### Prepare Your Repository
Extend your repository with `JpaSpecificationExecutor`:
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
}
```
### Basic Usage
This code filter all your `users` by `status` having value `ACTIVE`. The SQL translation for this code is like `select * from users where status = 'ACTIVE'`
```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final SpecificationEngine specificationEngine;
    
    public List<User> findActiveUsers() {
        // Simple equality filter
        SpecificationRequest request = SpecificationUtils.createAndRequest(
            SpecificationUtils.equals("status", "ACTIVE")
        );
        
        Specification<User> spec = specificationEngine.createSpecification(request);
        return userRepository.findAll(spec);
    }
}
```

### 🛠️ Core Concepts - `SpecificationUtils` Your Primary Tool
The `SpecificationUtils` class is your main entry point for creating filters. It provides factory methods for all common operations:

```java
import java.util.Arrays;

// Equality filters
FilterCriteria activeFilter = SpecificationUtils.equals("status", "ACTIVE");
FilterCriteria activeFilter1 = FilterCriteria.builder()
        .field("status")
        .operation(FilterOperation.EQUALS)
        .value("ACTIVE")
        .build();

FilterCriteria adminFilter = SpecificationUtils.equals("role", "ADMIN");
FilterCriteria adminFilter1 = FilterCriteria.builder()
        .field("role")
        .operation(FilterOperation.EQUALS)
        .value("ADMIN")
        .build();

// String matching
FilterCriteria nameFilter = SpecificationUtils.like("name", "%John%");
FilterCriteria nameFilter1 = FilterCriteria.builder()
        .field("name")
        .operation(FilterOperation.LIKE)
        .value("%John%")
        .build();

FilterCriteria emailFilter = SpecificationUtils.ilike("email", "%@company.com%"); // Case-insensitive
FilterCriteria emailFilter1 = FilterCriteria.builder()
        .field("email")
        .operation(FilterOperation.ILIKE)
        .value("%@company.com%")
        .build();

// Collection operations  
FilterCriteria departmentFilter = SpecificationUtils.in("department", "IT", "FINANCE", "HR");
FilterCriteria departmentFilter1 = FilterCriteria.builder()
        .field("department")
        .operation(FilterOperation.IN)
        .values(Arrays.asList("IT", "FINANCE", "HR"))
        .build();

FilterCriteria salaryFilter = SpecificationUtils.between("salary", 50000, 100000);
FilterCriteria departmentFilter1 = FilterCriteria.builder()
        .field("salary")
        .operation(FilterOperation.BETWEEN)
        .values(List.of(50000, 100000))
        .build();

// Null checks
FilterCriteria hasEmailFilter = SpecificationUtils.isNotNull("email");
FilterCriteria hasEmailFilter1 = FilterCriteria.builder()
        .field("email")
        .operation(FilterOperation.IS_NOT_NULL)
        .build();

FilterCriteria noPhoneFilter = SpecificationUtils.isNull("phoneNumber");
FilterCriteria noPhoneFilter1 = FilterCriteria.builder()
        .field("phoneNumber")
        .operation(FilterOperation.IS_NULL)
        .build();
```
You can combine one or more inside the `SpecificationUtils.createAndRequest()` method. That create a concatenation of criteria all in `AND`. If you wish to use the `OR` operator, just use the `SpecificationUtils.createOrRequest()` method. 
This method create the `SpecificationRequest` and the meaning is the following:
- `SpecificationUtils.createAndRequest()`: this will create the following conditions on the entities `Condition1 AND Condition2 AND Condition3 ...`
- `SpecificationUtils.createOrRequest()`: this will create the following conditions on the entities `Condition1 OR Condition2 OR Condition3 ...`

## 📚 Comprehensive Examples
Let's say you want to create dynamic filtering based on some conditions of your choice. Following you'll find a simple filtering example.
```java
@RestController
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/users")
    public List<User> getUsers(@RequestParam(required = false) String department,
                              @RequestParam(required = false) String status) {
        // Build filters dynamically
        List<FilterCriteria> filters = new ArrayList<>();
        
        if (department != null) {
            filters.add(SpecificationUtils.equals("department", department));
        }
        
        if (status != null) {
            filters.add(SpecificationUtils.equals("status", status));
        }
        
        SpecificationRequest request = SpecificationRequest.builder()
            .filters(filters)
            .useAndOperator(true)  // All conditions must match
            .build();
            
        return userService.findUsers(request);
    }
}
```
Here you can see there's a different way to create the `SpecificationRequest`. This code is equivalent to use the `SpecificationUtils.createAndRequest()` method.

### 🧠 Advanced Usage

```java
public List<User> searchUsers(String searchTerm) {
    // Create OR group for flexible name searching
    FilterGroup nameSearch = SpecificationUtils.createOrGroup(
        SpecificationUtils.like("firstName", "%" + searchTerm + "%"),
        SpecificationUtils.like("lastName", "%" + searchTerm + "%"),
        SpecificationUtils.like("username", "%" + searchTerm + "%"),
        SpecificationUtils.like("email", "%" + searchTerm + "%")
    );
    
    // Combine with active status requirement
    SpecificationRequest request = SpecificationRequest.builder()
        .filters(Arrays.asList(
            SpecificationUtils.equals("isActive", true)
        ))
        .filterGroups(Arrays.asList(nameSearch))
        .useAndOperator(true)           // Individual filters use AND
        .useAndOperatorForGroups(true)  // Groups combined with AND
        .build();
        
    Specification<User> spec = specificationEngine.createSpecification(request);
    return userRepository.findAll(spec);
}
```
The above code will generate a more complex SQL query. The result is `select * from users WHERE is_active = true AND (first_name LIKE '%john%' OR last_name LIKE '%john%' OR username LIKE '%john%' OR email LIKE '%john%')`

### Advanced Usage - Tips
Notice you can play around with SpecificationUtils and simple FilterCriteria in the best way you prefer. You can combine them to create the best combination of filter creation. The final object you must use is `SpecificationRequest` which will be used by the `SpecificationEngine` to create the final `Specification`.

### Complex Multi-Level Filtering
Here's another example for complex filtering.
```java
public List<Employee> findEmployees(EmployeeSearchCriteria criteria) {
    
    // Group 1: Personal information (OR logic)
    FilterGroup personalInfo = SpecificationUtils.createOrGroup(
        SpecificationUtils.like("firstName", "%" + criteria.getName() + "%"),
        SpecificationUtils.like("lastName", "%" + criteria.getName() + "%")
    );
    
    // Group 2: Job requirements (AND logic)  
    FilterGroup jobRequirements = SpecificationUtils.createAndGroup(
        SpecificationUtils.in("department", "IT", "ENGINEERING"),
        SpecificationUtils.between("salary", 60000, 120000),
        SpecificationUtils.equals("employmentType", "FULL_TIME")
    );
    
    // Group 3: Experience and skills (OR logic)
    FilterGroup experience = SpecificationUtils.createOrGroup(
        SpecificationUtils.between("yearsExperience", 3, 10),
        SpecificationUtils.like("skills", "%Java%"),
        SpecificationUtils.like("skills", "%Spring%")
    );
    
    // Combine all groups with AND logic
    // Other simple Filters will use default AND logic as well. If you wish to change, change it in the request object.
    SpecificationRequest request = SpecificationUtils.createGroupRequest(
        true,  // Use AND between groups
        personalInfo, 
        jobRequirements, 
        experience
    );
    
    // Add individual filters
    request.addFilter(SpecificationUtils.equals("isActive", true));
    request.addFilter(SpecificationUtils.isNotNull("email"));
    
    Specification<Employee> spec = specificationEngine.createSpecification(request);
    return employeeRepository.findAll(spec);
}
```
## Date Range Filtering
Example code to filter Dates:
```java
public List<Order> findOrdersByDateRange(LocalDate startDate, LocalDate endDate, String status) {
    
    SpecificationRequest request = SpecificationUtils.createAndRequest(
        // Date range
        SpecificationUtils.createFilter("createdDate", FilterOperation.DATE_BETWEEN, 
            Arrays.asList(startDate, endDate)),
            
        // Status filter
        SpecificationUtils.equals("status", status),
        
        // Recent orders (last 30 days)
        SpecificationUtils.createFilter("updatedDate", FilterOperation.DATE_AFTER, 
            LocalDate.now().minusDays(30))
    );
    
    Specification<Order> spec = specificationEngine.createSpecification(request);
    return orderRepository.findAll(spec);
}
```

## Nested Entity Filtering
Example code to filter entities with joins:
```java
public List<User> findUsersByProfileAndAddress(String city, String company) {
    
    SpecificationRequest request = SpecificationUtils.createAndRequest(
        // Navigate through nested relationships using dot notation
        SpecificationUtils.equals("profile.address.city", city),
        SpecificationUtils.equals("profile.company.name", company),
        SpecificationUtils.isNotNull("profile.email"),
        
        // Direct user fields
        SpecificationUtils.equals("isActive", true)
    );
    
    Specification<User> spec = specificationEngine.createSpecification(request);
    return userRepository.findAll(spec);
}

// SQL Generated:
// SELECT u.* FROM users u 
// JOIN user_profiles p ON u.id = p.user_id
// JOIN addresses a ON p.address_id = a.id  
// JOIN companies c ON p.company_id = c.id
// WHERE a.city = 'Milano' AND c.name = 'TechCorp' AND p.email IS NOT NULL AND u.is_active = true
```

## Custom Filter Operations
For operations not covered by the utility methods, create custom filters:
```java
public List<User> findUsersWithCustomLogic() {
    
    // Custom filter with specific operation
    FilterCriteria customAgeFilter = FilterCriteria.builder()
        .field("birthDate") 
        .operation(FilterOperation.DATE_BEFORE)
        .value(LocalDate.now().minusYears(18))  // Adults only
        .build();
    
    // Custom string filter with negation
    FilterCriteria notTestUserFilter = FilterCriteria.builder()
        .field("email")
        .operation(FilterOperation.ENDS_WITH)
        .value("@test.com")
        .negate(true)  // NOT ending with @test.com
        .caseSensitive(false)
        .build();
        
    SpecificationRequest request = SpecificationUtils.createAndRequest(
        customAgeFilter,
        notTestUserFilter,
        SpecificationUtils.equals("status", "ACTIVE")
    );
    
    Specification<User> spec = specificationEngine.createSpecification(request);
    return userRepository.findAll(spec);
}
```

## 📃 Pagination Integration
By using `Specification` under the hood you get all the benefits of `Pagination` provided by JPA.
```java
public Page<User> findUsersWithPagination(SpecificationRequest request, 
                                         int page, int size, String sortBy) {
    
    Specification<User> spec = specificationEngine.createSpecification(request);
    
    Pageable pageable = PageRequest.of(page, size, 
        Sort.by(Sort.Direction.ASC, sortBy));
        
    return userRepository.findAll(spec, pageable);
}
```

## 🚨 Error Handling
The library provides meaningful error messages:
```java
try {
    Specification<User> spec = specificationEngine.createSpecification(request);
    return userRepository.findAll(spec);
} catch (SpecificationException e) {
    log.error("Filter creation failed: {}", e.getMessage());
    // Handle gracefully - return empty list or error response
    return Collections.emptyList();
}
```

## 💅🏻 Very Advanced Configuration
If you want to extend or create a custom behavior of the `SpecificationBuilder` you can play around as you want.
```java
@Component
public class CustomSpecificationBuilder<T> implements SpecificationBuilder<T> {
    
    @Override
    public Specification<T> build(FilterCriteria criteria) {
        // Custom logic here
        return (root, query, criteriaBuilder) -> {
            // Your custom predicate logic
        };
    }
}
```

## 🤝 Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

`Happy Querying! 🎯`

