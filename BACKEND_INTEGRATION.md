# Karhebti Android - Backend Integration Guide

## 🔌 Backend API Integration

Your Karhebti Android app is now **fully integrated** with the NestJS backend API!

### 📡 API Configuration

**Backend URL:** `http://localhost:3000`  
**Android Emulator URL:** `http://10.0.2.2:3000` (automatically configured)  
**Authentication:** JWT Bearer Token (24 hour expiry)

### 🏗️ Architecture Overview

```
app/
├── data/
│   ├── api/
│   │   ├── ApiConfig.kt              # Retrofit configuration
│   │   ├── ApiModels.kt              # All DTOs (Request/Response models)
│   │   └── KarhebtiApiService.kt     # API endpoints interface
│   ├── preferences/
│   │   └── TokenManager.kt           # JWT token & user data persistence
│   └── repository/
│       └── Repositories.kt           # Data layer (Auth, Car, Maintenance, etc.)
├── viewmodel/
│   └── ViewModels.kt                 # Business logic & state management
└── ui/
    └── screens/                      # UI screens (already implemented)
```

## 🚀 Implemented Features

### ✅ Complete API Layer

#### 1. **Authentication Service**
- ✅ Signup (POST /auth/signup)
- ✅ Login (POST /auth/login)
- ✅ Forgot Password (POST /auth/forgot-password)
- ✅ Reset Password (POST /auth/reset-password)
- ✅ Automatic token management
- ✅ Token persistence with SharedPreferences

#### 2. **Cars Service**
- ✅ Get my cars (GET /cars)
- ✅ Get car details (GET /cars/:id)
- ✅ Create car (POST /cars)
- ✅ Update car (PATCH /cars/:id)
- ✅ Delete car (DELETE /cars/:id)

#### 3. **Maintenance Service**
- ✅ Get all maintenances (GET /maintenances)
- ✅ Get maintenance details (GET /maintenances/:id)
- ✅ Create maintenance (POST /maintenances)
- ✅ Update maintenance (PATCH /maintenances/:id)
- ✅ Delete maintenance (DELETE /maintenances/:id)

#### 4. **Garage Service**
- ✅ Get all garages (GET /garages)
- ✅ Get garage details (GET /garages/:id)
- ✅ Create garage - Admin only (POST /garages)
- ✅ Update garage - Admin only (PATCH /garages/:id)
- ✅ Delete garage - Admin only (DELETE /garages/:id)
- ✅ Get garage recommendations (GET /ai/garage-recommendation)

#### 5. **Document Service**
- ✅ Get all documents (GET /documents)
- ✅ Get document details (GET /documents/:id)
- ✅ Create document (POST /documents)
- ✅ Update document (PATCH /documents/:id)
- ✅ Delete document (DELETE /documents/:id)

#### 6. **Parts Service**
- ✅ Get all parts (GET /parts)
- ✅ Get part details (GET /parts/:id)
- ✅ Create part (POST /parts)
- ✅ Update part (PATCH /parts/:id)
- ✅ Delete part (DELETE /parts/:id)

#### 7. **AI Features**
- ✅ Report road issue (POST /ai/report-road-issue)
- ✅ Get danger zones (GET /ai/danger-zones)
- ✅ Get maintenance recommendations (POST /ai/maintenance-recommendations)
- ✅ Get garage recommendations (GET /ai/garage-recommendation)

#### 8. **User Management**
- ✅ Get all users - Admin only (GET /users)
- ✅ Get user details (GET /users/:id)
- ✅ Create user - Admin only (POST /users)
- ✅ Update user (PATCH /users/:id)
- ✅ Delete user - Admin only (DELETE /users/:id)
- ✅ Update user role - Admin only (PATCH /users/:id/role)

#### 9. **Services**
- ✅ Get all services (GET /services)
- ✅ Get service details (GET /services/:id)
- ✅ Get services by garage (GET /services/garage/:garageId)
- ✅ Create service - Admin only (POST /services)
- ✅ Update service - Admin only (PATCH /services/:id)
- ✅ Delete service - Admin only (DELETE /services/:id)

## 🔧 How to Use

### 1. Setup Backend

Make sure your NestJS backend is running:
```bash
cd backend
npm run start:dev
```

Backend should be accessible at `http://localhost:3000`

### 2. Using ViewModels in Screens

#### Example: Login Screen with ViewModel

```kotlin
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val authState by viewModel.authState.observeAsState()
    
    // Handle auth state
    LaunchedEffect(authState) {
        when (authState) {
            is Resource.Success -> {
                onLoginSuccess()
            }
            is Resource.Error -> {
                // Show error message
                val message = (authState as Resource.Error).message
                // Display toast or snackbar
            }
            is Resource.Loading -> {
                // Show loading indicator
            }
            else -> {}
        }
    }
    
    Button(onClick = {
        viewModel.login(email, password)
    }) {
        Text("Se connecter")
    }
}
```

#### Example: Fetch Cars

```kotlin
@Composable
fun VehiclesScreen(
    viewModel: CarViewModel = viewModel()
) {
    val carsState by viewModel.carsState.observeAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getMyCars()
    }
    
    when (carsState) {
        is Resource.Loading -> {
            CircularProgressIndicator()
        }
        is Resource.Success -> {
            val cars = (carsState as Resource.Success).data ?: emptyList()
            LazyColumn {
                items(cars) { car ->
                    VehicleCard(car)
                }
            }
        }
        is Resource.Error -> {
            Text("Erreur: ${(carsState as Resource.Error).message}")
        }
        else -> {}
    }
}
```

#### Example: Create Car

```kotlin
@Composable
fun AddCarDialog(
    viewModel: CarViewModel = viewModel()
) {
    var marque by remember { mutableStateOf("") }
    var modele by remember { mutableStateOf("") }
    var annee by remember { mutableStateOf("") }
    var immatriculation by remember { mutableStateOf("") }
    var typeCarburant by remember { mutableStateOf("Essence") }
    
    val createCarState by viewModel.createCarState.observeAsState()
    
    LaunchedEffect(createCarState) {
        if (createCarState is Resource.Success) {
            // Car created successfully
            // Close dialog and refresh list
        }
    }
    
    Button(onClick = {
        viewModel.createCar(
            marque = marque,
            modele = modele,
            annee = annee.toInt(),
            immatriculation = immatriculation,
            typeCarburant = typeCarburant
        )
    }) {
        Text("Créer")
    }
}
```

## 🔐 Authentication Flow

### 1. **Login/Signup**
```kotlin
// In LoginScreen
val viewModel: AuthViewModel = viewModel()

viewModel.login(email, password)
// or
viewModel.signup(nom, prenom, email, password, telephone)
```

### 2. **Token Management**
The `TokenManager` automatically:
- ✅ Saves JWT token to SharedPreferences
- ✅ Adds token to all API requests via Retrofit interceptor
- ✅ Persists user data locally
- ✅ Checks if user is logged in
- ✅ Checks if user is admin

### 3. **Logout**
```kotlin
viewModel.logout() // Clears token and user data
```

### 4. **Check Login Status**
```kotlin
val tokenManager = TokenManager.getInstance(context)

if (tokenManager.isLoggedIn()) {
    // User is logged in
    val user = tokenManager.getUser()
    val isAdmin = tokenManager.isAdmin()
}
```

## 📱 API Response Models

### Authentication Response
```kotlin
data class AuthResponse(
    val accessToken: String,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val nom: String,
    val prenom: String,
    val email: String,
    val telephone: String?,
    val role: String // "utilisateur" or "admin"
)
```

### Car Response
```kotlin
data class CarResponse(
    val id: String,
    val marque: String,
    val modele: String,
    val annee: Int,
    val immatriculation: String,
    val typeCarburant: String,
    val user: UserResponse?,
    val createdAt: Date,
    val updatedAt: Date
)
```

### Maintenance Response
```kotlin
data class MaintenanceResponse(
    val id: String,
    val type: String, // vidange, révision, réparation
    val date: Date,
    val cout: Double,
    val garage: GarageResponse?,
    val voiture: CarResponse?,
    val createdAt: Date,
    val updatedAt: Date
)
```

## 🎯 Resource Wrapper

All API calls return a `Resource<T>` wrapper:

```kotlin
sealed class Resource<T> {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String) : Resource<T>(message = message)
    class Loading<T> : Resource<T>()
}
```

**Usage:**
```kotlin
when (val result = repository.getMyCars()) {
    is Resource.Success -> {
        val cars = result.data
        // Update UI with cars
    }
    is Resource.Error -> {
        val error = result.message
        // Show error to user
    }
    is Resource.Loading -> {
        // Show loading indicator
    }
}
```

## 🌐 Network Configuration

### For Android Emulator
```kotlin
BASE_URL = "http://10.0.2.2:3000/"
```

### For Physical Device
Update `RetrofitClient.kt`:
```kotlin
private const val BASE_URL = "http://192.168.1.XXX:3000/" 
// Replace with your computer's IP address
```

### Enable Internet Permission
Already configured in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 🔍 Error Handling

The app handles all HTTP error codes from the backend:

- **400 Bad Request** - Validation errors
- **401 Unauthorized** - Token expired/invalid (auto logout)
- **403 Forbidden** - Permission denied
- **404 Not Found** - Resource not found
- **409 Conflict** - Duplicate data (e.g., email already exists)
- **429 Too Many Requests** - Rate limit exceeded
- **500 Internal Server Error** - Server error

## 📊 Available ViewModels

1. **AuthViewModel** - Authentication & user management
2. **CarViewModel** - Vehicle CRUD operations
3. **MaintenanceViewModel** - Maintenance CRUD operations
4. **GarageViewModel** - Garage listing & recommendations
5. **DocumentViewModel** - Document CRUD operations
6. **AIViewModel** - AI features (danger zones, recommendations)

## 🔒 Permission System

### User Roles
- **utilisateur** - Standard user (can manage own data)
- **admin** - Administrator (full access)

### Admin-Only Endpoints
- User management (GET /users, DELETE /users/:id)
- Garage management (POST/PATCH/DELETE /garages)
- Service management (POST/PATCH/DELETE /services)
- Role updates (PATCH /users/:id/role)

Check admin status:
```kotlin
val tokenManager = TokenManager.getInstance(context)
if (tokenManager.isAdmin()) {
    // Show admin features
}
```

## 🚦 API Rate Limiting

Backend enforces:
- **100 requests/minute** per IP
- Returns `429 Too Many Requests` if exceeded
- Automatic reset after 1 minute

## 📝 Testing Checklist

### Backend Integration Tests
- [ ] Signup new user
- [ ] Login existing user
- [ ] Forgot password flow
- [ ] Create car
- [ ] Get cars list
- [ ] Update car
- [ ] Delete car
- [ ] Create maintenance
- [ ] Get maintenances list
- [ ] Get garages list
- [ ] Get danger zones
- [ ] Get maintenance recommendations
- [ ] Upload document
- [ ] Token persistence after app restart
- [ ] Auto logout on 401 error
- [ ] Admin features (if admin user)

## 🎨 Integration with Existing UI

All screens are already created! You just need to:

1. **Add ViewModel to screens**
2. **Replace sample data with API calls**
3. **Add loading states**
4. **Handle errors gracefully**

Example integration for VehiclesScreen is ready in the next update!

## 🔄 Next Steps

1. ✅ Backend API fully integrated
2. ✅ All DTOs and models created
3. ✅ Repositories implemented
4. ✅ ViewModels ready
5. ✅ Token management configured
6. 🔜 Update UI screens to use ViewModels
7. 🔜 Add loading indicators
8. 🔜 Add error handling UI
9. 🔜 Implement offline caching (optional)
10. 🔜 Add push notifications (optional)

---

**Backend Documentation:** See backend README for complete API reference  
**Swagger UI:** `http://localhost:3000/api` when backend is running  
**Current Version:** 1.0.0 with full backend integration

