# Karhebti Android - Complete Backend Integration Guide

## 🎯 Overview

This document provides a comprehensive guide for the complete backend integration of the Karhebti Android application with the NestJS REST API.

## 📁 Architecture Overview

### Layer Architecture
```
UI Layer (Screens)
    ↓
ViewModel Layer (State Management)
    ↓
Repository Layer (Business Logic)
    ↓
API Service Layer (Network Calls)
    ↓
Backend (NestJS REST API)
```

## 🔐 Authentication System

### Implementation Status: ✅ COMPLETE

#### Components:
1. **TokenManager** - Secure token storage using SharedPreferences
2. **AuthRepository** - Authentication business logic
3. **AuthViewModel** - State management for auth screens
4. **Updated Screens**: LoginScreen, SignUpScreen, ForgotPasswordScreen

#### Features:
- JWT token storage and automatic header injection
- User session management
- Automatic token initialization on app start
- Role-based access control (admin/user)

#### Usage Example:

```kotlin
// Login
val authViewModel: AuthViewModel = viewModel()
authViewModel.login(email, password)

// Observe state
val authState by authViewModel.authState.observeAsState()
when (authState) {
    is Resource.Loading -> // Show loading
    is Resource.Success -> // Navigate to home
    is Resource.Error -> // Show error message
}

// Check if logged in
if (authViewModel.isLoggedIn()) {
    // User is authenticated
}

// Logout
authViewModel.logout()
```

## 🚗 Vehicles Module

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `GET /cars` - Get user's vehicles
- `GET /cars/:id` - Get single vehicle
- `POST /cars` - Create vehicle
- `PATCH /cars/:id` - Update vehicle
- `DELETE /cars/:id` - Delete vehicle

#### Usage Example:

```kotlin
val carViewModel: CarViewModel = viewModel()

// Load vehicles
LaunchedEffect(Unit) {
    carViewModel.getMyCars()
}

// Observe vehicles
val carsState by carViewModel.carsState.observeAsState()
when (carsState) {
    is Resource.Loading -> CircularProgressIndicator()
    is Resource.Success -> {
        val cars = carsState.data
        // Display cars in LazyColumn
    }
    is Resource.Error -> Text("Erreur: ${carsState.message}")
}

// Create vehicle
carViewModel.createCar(
    marque = "Renault",
    modele = "Clio",
    annee = 2020,
    immatriculation = "AB-123-CD",
    typeCarburant = "Essence"
)

// Delete vehicle
carViewModel.deleteCar(vehicleId)
```

## 🔧 Maintenance Module

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `GET /maintenances` - Get all maintenances
- `POST /maintenances` - Create maintenance
- `PATCH /maintenances/:id` - Update maintenance
- `DELETE /maintenances/:id` - Delete maintenance

#### Usage Example:

```kotlin
val maintenanceViewModel: MaintenanceViewModel = viewModel()

// Load maintenances
maintenanceViewModel.getMaintenances()

// Create maintenance
maintenanceViewModel.createMaintenance(
    type = "vidange",
    date = "2024-01-15T10:00:00.000Z",
    cout = 120.0,
    garage = garageId,
    voiture = carId
)
```

## 🏢 Garages Module

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `GET /garages` - Get all garages
- `GET /ai/garage-recommendation` - Get AI recommendations

#### Usage Example:

```kotlin
val garageViewModel: GarageViewModel = viewModel()

// Load garages
garageViewModel.getGarages()

// Get AI recommendations
garageViewModel.getRecommendations(
    typePanne = "vidange",
    latitude = 48.8566,
    longitude = 2.3522,
    rayon = 10.0
)

// Observe state
val garagesState by garageViewModel.garagesState.observeAsState()
```

## 📄 Documents Module

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `GET /documents` - Get all documents
- `POST /documents` - Create document
- `DELETE /documents/:id` - Delete document

#### Usage Example:

```kotlin
val documentViewModel: DocumentViewModel = viewModel()

// Load documents
documentViewModel.getDocuments()

// Create document
documentViewModel.createDocument(
    type = "assurance",
    dateEmission = "2024-01-01",
    dateExpiration = "2025-01-01",
    fichier = "url-to-file",
    voiture = carId
)
```

## 🔩 Parts Module

### Implementation Status: ✅ COMPLETE

#### Features:
- Track part installation dates
- Monitor recommended replacement mileage
- Part history per vehicle

## 🤖 AI Features Module

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `POST /ai/report-road-issue` - Report road anomaly
- `GET /ai/danger-zones` - Get danger zones
- `POST /ai/maintenance-recommendations` - Get AI maintenance recommendations

#### Usage Example:

```kotlin
val aiViewModel: AIViewModel = viewModel()

// Report road issue
aiViewModel.reportRoadIssue(
    latitude = 48.8566,
    longitude = 2.3522,
    typeAnomalie = "nid-de-poule",
    description = "Large pothole on main road"
)

// Get danger zones
aiViewModel.getDangerZones(
    latitude = 48.8566,
    longitude = 2.3522,
    rayon = 5.0
)

// Get maintenance recommendations
aiViewModel.getMaintenanceRecommendations(carId)
```

## 👥 User Management (Admin)

### Implementation Status: ✅ COMPLETE

#### API Endpoints:
- `GET /users` - Get all users (admin only)
- `PATCH /users/:id` - Update user
- `PATCH /users/:id/role` - Update user role
- `DELETE /users/:id` - Delete user

## 🔄 State Management Pattern

### Resource Wrapper
All API responses are wrapped in a `Resource` sealed class:

```kotlin
sealed class Resource<T> {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String) : Resource<T>(message = message)
    class Loading<T> : Resource<T>()
}
```

### Benefits:
- Unified error handling
- Automatic loading state management
- Type-safe data access

## 🛡️ Error Handling

### Network Error Handling
```kotlin
try {
    val response = apiService.getMyCars()
    if (response.isSuccessful && response.body() != null) {
        Resource.Success(response.body()!!)
    } else {
        Resource.Error("Erreur: ${response.message()}")
    }
} catch (e: Exception) {
    Resource.Error("Erreur réseau: ${e.localizedMessage}")
}
```

### UI Error Display
```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(state) {
    if (state is Resource.Error) {
        snackbarHostState.showSnackbar(
            message = state.message ?: "Erreur",
            duration = SnackbarDuration.Short
        )
    }
}

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
) { ... }
```

## 🔒 Security Best Practices

### 1. Token Storage
- Tokens stored in SharedPreferences (MODE_PRIVATE)
- Automatic token injection via Interceptor
- Token cleared on logout

### 2. Password Handling
- Passwords never stored locally
- Minimum 6 characters validation
- Visual feedback for password strength

### 3. Network Security
- HTTPS enforced in production
- 30-second timeouts
- Proper error sanitization

## 📱 Loading States

### Implementation Pattern
```kotlin
Button(
    onClick = { viewModel.performAction() },
    enabled = state !is Resource.Loading
) {
    if (state is Resource.Loading) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = Color.White,
            strokeWidth = 2.dp
        )
    } else {
        Text("Submit")
    }
}
```

## 🔄 Data Refresh Pattern

### Automatic Refresh After Actions
```kotlin
fun deleteItem(id: String) {
    viewModelScope.launch {
        val result = repository.deleteItem(id)
        if (result is Resource.Success) {
            getItems() // Automatic refresh
        }
    }
}
```

### Pull-to-Refresh
```kotlin
// TODO: Implement SwipeRefresh for screens
```

## 📊 API Configuration

### Base URLs
```kotlin
// Development (Android Emulator)
const val BASE_URL = "http://10.0.2.2:3000/"

// Production
const val BASE_URL = "https://api.karhebti.com/"

// Physical Device (Same Network)
const val BASE_URL = "http://192.168.x.x:3000/"
```

### Retrofit Configuration
- Connection timeout: 30s
- Read timeout: 30s
- Write timeout: 30s
- Logging enabled (debug mode)
- GSON date format: ISO 8601

## 🧪 Testing Recommendations

### Unit Tests
```kotlin
// Repository Tests
@Test
fun `login with valid credentials returns success`() = runTest {
    // Mock API service
    val mockService = mock<KarhebtiApiService>()
    whenever(mockService.login(any())).thenReturn(
        Response.success(AuthResponse(...))
    )
    
    val repository = AuthRepository(mockService)
    val result = repository.login("test@test.com", "password")
    
    assertTrue(result is Resource.Success)
}
```

### Integration Tests
```kotlin
// ViewModel Tests
@Test
fun `getMyCars updates state correctly`() = runTest {
    val viewModel = CarViewModel(application)
    viewModel.getMyCars()
    
    // Assert loading state
    assertEquals(Resource.Loading::class, viewModel.carsState.value::class)
}
```

## 🚀 Next Steps

### Screens to Update (In Progress)
1. ✅ LoginScreen - DONE
2. ✅ SignUpScreen - DONE  
3. ✅ ForgotPasswordScreen - DONE
4. 🔄 VehiclesScreen - Update to fetch from API
5. 🔄 EntretiensScreen - Update to fetch from API
6. 🔄 GaragesScreen - Update to fetch from API
7. 🔄 DocumentsScreen - Update to fetch from API
8. 🔄 HomeScreen - Add dashboard data
9. 🔄 SettingsScreen - Add profile management

### Features to Implement
- [ ] File upload for documents
- [ ] Image upload for vehicles
- [ ] Push notifications
- [ ] Offline mode with local caching
- [ ] Pull-to-refresh on all lists
- [ ] Search and filter implementations
- [ ] Pagination for large lists

## 📝 Code Examples

### Complete Screen Integration Example

```kotlin
@Composable
fun VehiclesScreen(
    carViewModel: CarViewModel = viewModel()
) {
    // Load data on screen launch
    LaunchedEffect(Unit) {
        carViewModel.getMyCars()
    }
    
    // Observe state
    val carsState by carViewModel.carsState.observeAsState()
    
    Scaffold(
        topBar = { /* AppBar */ },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add car */ }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        when (carsState) {
            is Resource.Loading -> {
                Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        Modifier.align(Alignment.Center)
                    )
                }
            }
            is Resource.Success -> {
                val cars = (carsState as Resource.Success).data ?: emptyList()
                LazyColumn(Modifier.padding(padding)) {
                    items(cars) { car ->
                        VehicleCard(
                            car = car,
                            onDelete = { carViewModel.deleteCar(car.id) }
                        )
                    }
                }
            }
            is Resource.Error -> {
                ErrorView(
                    message = (carsState as Resource.Error).message,
                    onRetry = { carViewModel.getMyCars() }
                )
            }
            else -> {}
        }
    }
}
```

## 🎓 Learning Resources

### Retrofit
- Official Docs: https://square.github.io/retrofit/
- Type-safe HTTP client for Android

### Coroutines
- Official Docs: https://kotlinlang.org/docs/coroutines-overview.html
- For async operations

### Jetpack Compose
- Official Docs: https://developer.android.com/jetpack/compose
- Modern Android UI toolkit

## 🐛 Troubleshooting

### Common Issues

**Issue**: "Unable to resolve host"
**Solution**: Check BASE_URL, ensure backend is running, check network permissions

**Issue**: "401 Unauthorized"
**Solution**: Token expired or invalid, logout and login again

**Issue**: "Network timeout"
**Solution**: Increase timeout values or check internet connection

**Issue**: "Unresolved reference: observeAsState"
**Solution**: Add dependency: `implementation "androidx.compose.runtime:runtime-livedata:1.5.4"`

## 📞 Support

For issues or questions:
1. Check this documentation
2. Review backend API documentation
3. Check network logs in Logcat
4. Verify backend is running and accessible

## ✅ Completion Checklist

### Backend Integration
- ✅ API Service interface defined
- ✅ Repository layer implemented
- ✅ ViewModels created
- ✅ Token management setup
- ✅ Error handling standardized
- ✅ Loading states implemented
- ✅ Auth screens connected
- 🔄 Feature screens in progress

### Security
- ✅ JWT authentication
- ✅ Secure token storage
- ✅ Auth interceptor
- ✅ Input validation
- ✅ Error sanitization

### UX/UI
- ✅ Loading indicators
- ✅ Error messages (Snackbar)
- ✅ Form validation
- ✅ Success feedback
- 🔄 Pull-to-refresh
- 🔄 Empty states

---

**Last Updated**: November 4, 2025
**Status**: Backend integration 60% complete
**Next Priority**: Update VehiclesScreen, EntretiensScreen, GaragesScreen with API data

