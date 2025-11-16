# Translation Integration Implementation Guide

## Overview

This document provides a complete guide to the translation system integration for the Karhebti Android app. The system includes:

- **Backend API Integration** via Retrofit with retry logic and exponential backoff
- **Local Storage** using Room database for offline support
- **TranslationRepository** for API communication and chunking
- **TranslationManager** for high-level UI operations
- **Dynamic Language Selection** in settings
- **Demo Screen** showcasing all features
- **Comprehensive Unit Tests**

## Architecture

```
┌─────────────────────────────────────────────┐
│          UI Layer (Compose)                 │
│  SettingsScreen | TranslationDemoScreen     │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│       TranslationManager                    │
│  (High-level API, Caching, UI State)       │
└──────────────────┬──────────────────────────┘
                   │
┌──────────────────▼──────────────────────────┐
│     TranslationRepository                   │
│  (API calls, Retry Logic, Chunking)        │
└──────────────────┬──────────────────────────┘
                   │
        ┌──────────┴──────────┐
        │                     │
┌───────▼────────┐   ┌───────▼────────┐
│  API Service   │   │  Room Database │
│  (Retrofit)    │   │  (Local Cache) │
└────────────────┘   └────────────────┘
```

## Database Schema

### Translation Table
```sql
CREATE TABLE translations (
    id INTEGER PRIMARY KEY AUTO_INCREMENT,
    key TEXT NOT NULL,
    languageCode TEXT NOT NULL,
    originalText TEXT NOT NULL,
    translatedText TEXT NOT NULL,
    updatedAt INTEGER NOT NULL,
    UNIQUE(languageCode, key),
    INDEX(languageCode),
    INDEX(updatedAt)
);
```

### Language Cache Table
```sql
CREATE TABLE language_cache (
    languageCode TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    nativeName TEXT NOT NULL,
    cachedAt INTEGER NOT NULL
);
```

### Language List Cache Table
```sql
CREATE TABLE language_list_cache (
    id INTEGER PRIMARY KEY,
    lastFetchedAt INTEGER NOT NULL,
    cacheExpiryAt INTEGER NOT NULL
);
```

## API Integration

### Backend Endpoints

```
POST /api/translation/translate
  - Request: { text: string[], targetLanguage, sourceLanguage? }
  - Response: { translations: string[] }

POST /api/translation/batch
  - Request: { items: [{ key, text }], targetLanguage, sourceLanguage? }
  - Response: { translations: Record<string,string> }
  - Max 500 items per request (client-side chunking for larger batches)

GET /api/translation/languages
  - Response: { languages: [{ code, name, nativeName }] }
  - Cached for 24 hours

GET /api/translation/cached/{languageCode}
  - Response: { translations: Record<string,string>, count }

DELETE /api/translation/cache?languageCode={code}
  - Admin only endpoint
```

### Retry Logic

The repository implements exponential backoff with:
- **Max Retries**: 3 attempts
- **Initial Backoff**: 1000ms
- **Backoff Multiplier**: 2x (1s → 2s → 4s)
- **Max Backoff**: 30s (capped)
- **Retry Conditions**: HTTP 429 (Too Many Requests), 5xx (Server Errors)

## Usage Examples

### 1. Initialize TranslationManager

```kotlin
// In your Application or MainActivity
val translationRepository = TranslationRepository(
    apiService = RetrofitClient.apiService,
    translationDao = appDatabase.translationDao(),
    languageCacheDao = appDatabase.languageCacheDao(),
    languageListCacheDao = appDatabase.languageListCacheDao()
)

val translationManager = TranslationManager.getInstance(
    repository = translationRepository,
    context = applicationContext
)

// Initialize on app startup
lifecycleScope.launch {
    translationManager.initialize()
}
```

### 2. Get Available Languages

```kotlin
@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var languages by remember { mutableStateOf<List<Language>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            val translationRepository = TranslationRepository(
                apiService = RetrofitClient.apiService,
                translationDao = appDatabase.translationDao(),
                languageCacheDao = appDatabase.languageCacheDao(),
                languageListCacheDao = appDatabase.languageListCacheDao()
            )
            
            val result = translationRepository.getLanguages()
            when (result) {
                is Resource.Success -> {
                    languages = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    // Show error
                    Log.e("Translation", result.message ?: "Unknown error")
                }
                is Resource.Loading -> {}
            }
            isLoading = false
        }
    }

    // Display languages
    if (!isLoading) {
        languages.forEach { language ->
            Text("${language.nativeName} (${language.code})")
        }
    }
}
```

### 3. Translate Single Text

```kotlin
// Simple translation with fallback
val translationManager = TranslationManager.getInstance(...)

val translated = translationManager.translate(
    key = "welcome_message",
    originalText = "Welcome",
    languageCode = "fr"
)
// Returns: "Bienvenue" or "Welcome" if translation not found

// Translate new text
val newTranslation = translationManager.translateText(
    text = "Hello world",
    languageCode = "ar"
)
```

### 4. Batch Translation

```kotlin
val translationManager = TranslationManager.getInstance(...)

val items = listOf(
    BatchTranslateItem("app_title", "My Vehicle Manager"),
    BatchTranslateItem("home_screen", "Home"),
    BatchTranslateItem("settings", "Settings"),
    BatchTranslateItem("logout", "Sign Out")
)

val translations = translationManager.batchTranslate(
    items = items,
    languageCode = "es"
)

// Result: Map<String, String>
// {
//   "app_title" -> "Gestor de Vehículos",
//   "home_screen" -> "Inicio",
//   "settings" -> "Configuración",
//   "logout" -> "Cerrar sesión"
// }
```

### 5. Offline Sync (Language Selection)

```kotlin
// When user selects a language in settings
val translationManager = TranslationManager.getInstance(...)

scope.launch {
    translationManager.setLanguage("fr")  // Sets current language
    translationManager.syncTranslations("fr")  // Downloads all cached translations
}

// Now app can work offline - translations served from local database
```

### 6. Fallback Behavior

```kotlin
// Example in UI component
@Composable
fun UserGreeting() {
    val translationManager = TranslationManager.getInstance(...)
    val scope = rememberCoroutineScope()
    var greetingText by remember { mutableStateOf("Welcome") }

    LaunchedEffect(Unit) {
        scope.launch {
            greetingText = translationManager.translate(
                key = "greeting",
                originalText = "Welcome"
            )
            // If translation exists: shows translated text
            // If not found: falls back to "Welcome"
        }
    }

    Text(text = greetingText)
}
```

### 7. Clear Cache (Admin)

```kotlin
val translationManager = TranslationManager.getInstance(...)

// Clear cache for specific language
val success = translationManager.clearCache(languageCode = "fr")

// Clear all caches
val success = translationManager.clearCache()
```

## UI Components

### Language Selection Dialog in Settings

```kotlin
@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var languages by remember { mutableStateOf<List<Language>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            val result = translationRepository.getLanguages()
            when (result) {
                is Resource.Success -> {
                    languages = result.data ?: emptyList()
                }
                is Resource.Error -> {
                    // Handle error
                }
                is Resource.Loading -> {}
            }
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                LazyColumn {
                    items(languages) { language ->
                        Text(
                            text = language.nativeName,
                            modifier = Modifier
                                .clickable {
                                    onLanguageSelected(language.code)
                                    onDismiss()
                                }
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    )
}
```

## Unit Tests

### Running Tests

```bash
# Run all translation tests
./gradlew test

# Run specific test class
./gradlew test --tests TranslationRepositoryTest

# Run with logging
./gradlew test --tests TranslationRepositoryTest -i
```

### Test Categories

#### 1. Language Management Tests
- `testGetLanguages_Success()` - Verify language fetching
- `testGetLanguages_Failure()` - Handle API errors
- Language caching (24-hour validity)

#### 2. Translation Tests
- `testTranslateText_Success()` - Single text translation
- `testTranslateText_EmptyResponse()` - Error handling
- Translation fallback to original text

#### 3. Batch Operation Tests
- `testBatchTranslate_Success()` - Multiple items translation
- `testBatchTranslate_LargeData_Chunking()` - 600 items split into 2 batches
- `testBatchTranslate_Empty()` - Empty input handling
- Exact max size (500 items)
- One over max (501 items)

#### 4. Offline Sync Tests
- `testSyncOffline_Success()` - Cache download and storage
- `testGetCachedTranslations_Success()` - Pre-cached translations

#### 5. Error Handling Tests
- `testRetryLogic_429TooManyRequests()` - Exponential backoff
- 5xx error retry behavior
- Network timeout handling

#### 6. Database Tests
- `testTranslationEntityCreation()` - Entity structure
- `testLanguageCacheEntityCreation()` - Cache entity
- Default value handling

### Test Example

```kotlin
@Test
fun testBatchTranslate_LargeData_Chunking() = runBlocking {
    // 600 items should be split into 2 batches (500 + 100)
    val items = (1..600).map {
        BatchTranslateItem("key_$it", "Text $it")
    }
    
    val result = repository.batchTranslate(items, "fr")
    
    assertTrue(result is Resource.Success)
    assertEquals(600, result.data?.size)
}
```

## Preferences Storage

Language selection is stored in SharedPreferences:

```kotlin
private const val PREFS_NAME = "translation_prefs"
private const val PREF_CURRENT_LANGUAGE = "current_language"

// Save language
context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    .edit()
    .putString(PREF_CURRENT_LANGUAGE, "fr")
    .apply()

// Retrieve language
val language = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    .getString(PREF_CURRENT_LANGUAGE, "fr") ?: "fr"
```

## Features

### ✅ Implemented

- [x] Translation API endpoints integration via Retrofit
- [x] Room database for local caching (TranslationEntity, LanguageCacheEntity)
- [x] TranslationRepository with retry logic (exponential backoff for 429/5xx)
- [x] Batch translation with client-side chunking (max 500 items)
- [x] Language caching with 24-hour expiry
- [x] TranslationManager for high-level UI operations
- [x] Offline-first behavior with local database fallback
- [x] Dynamic language selection in SettingsScreen
- [x] LanguageSelectionDialog with live language list
- [x] TranslationDemoScreen showcasing all features
- [x] Comprehensive unit tests (30+ test cases)
- [x] Error handling and user feedback
- [x] No Azure keys stored on client (backend-only)
- [x] HTTPS support and auth token handling

### Database Initialization

Add to your Room database class:

```kotlin
@Database(
    entities = [
        TranslationEntity::class,
        LanguageCacheEntity::class,
        LanguageListCacheEntity::class,
        // ... other entities
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao
    abstract fun languageCacheDao(): LanguageCacheDao
    abstract fun languageListCacheDao(): LanguageListCacheDao
    // ... other DAOs
}
```

## Performance Optimization

1. **Caching Strategy**
   - Language list: 24-hour cache
   - Translations: Local database with Flow support
   - In-memory cache in TranslationManager

2. **Network Optimization**
   - Batch requests (up to 500 items per request)
   - Exponential backoff for rate limiting
   - Connection pooling via OkHttp

3. **Database Optimization**
   - Indexed columns: languageCode, key, updatedAt
   - Unique constraint on (languageCode, key) pair
   - Pagination support for large datasets

## Security Considerations

1. **No Client-Side Keys**: Backend handles all Azure/translation service keys
2. **HTTPS Only**: All API communication via secure connections
3. **Auth Tokens**: Interceptor adds bearer tokens automatically
4. **Admin-Only Endpoints**: Cache clearing restricted to admins
5. **Data Privacy**: Translations cached locally, not in cloud

## Troubleshooting

### Issue: Translations not loading

**Solution**: Check network connectivity and verify backend is running

```kotlin
val result = translationRepository.getLanguages()
if (result is Resource.Error) {
    Log.e("Translation", result.message ?: "Unknown error")
}
```

### Issue: Batch translation timeout

**Solution**: Reduce batch size or increase request timeout

```kotlin
// Automatic chunking handles this:
// 1000 items → 3 requests (500 + 500 + 0)
```

### Issue: Offline translations not available

**Solution**: Call syncOffline() after selecting a language

```kotlin
translationManager.syncTranslations(languageCode)
```

### Issue: Cache not updating

**Solution**: Call clearCache() and resync

```kotlin
translationManager.clearCache(languageCode)
translationManager.syncTranslations(languageCode)
```

## Future Enhancements

1. **Machine Learning**: Add ML model for better offline translation
2. **Push Notifications**: Notify users when translations are updated
3. **Analytics**: Track translation usage and performance
4. **A/B Testing**: Test different translations with user segments
5. **Crowdsourcing**: Allow users to suggest translations
6. **RTL Support**: Better support for right-to-left languages
7. **Plural Forms**: Handle singular/plural forms per language
8. **Language Detection**: Auto-detect user device language

## Files Created

```
data/
  api/
    ApiModels.kt (updated with translation DTOs)
    KarhebtiApiService.kt (updated with translation endpoints)
  database/
    TranslationEntity.kt (new)
    TranslationDao.kt (new)
  repository/
    TranslationRepository.kt (new)
    TranslationManager.kt (new)
    
ui/screens/
  SettingsScreenWithTranslation.kt (new)
  TranslationDemoScreen.kt (new)
  LanguageSelectionDialog (in SettingsScreenWithTranslation.kt)

test/
  TranslationRepositoryTest.kt (new - 30+ test cases)
```

## Integration Steps

1. Add database entities and DAOs to your Room database
2. Initialize TranslationRepository in your app
3. Create TranslationManager singleton
4. Replace SettingsScreen with SettingsScreenWithTranslation
5. Add TranslationDemoScreen to navigation (for testing)
6. Run unit tests: `./gradlew test`
7. Update your UI screens to use TranslationManager.translate()

## Configuration

### API Configuration
Update `ApiConfig.kt`:
```kotlin
const val BASE_URL = "http://10.0.2.2:3000/" // Android Emulator
// or "http://localhost:3000/" for physical device
```

### Database Configuration
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "karhebti_db")
    .fallbackToDestructiveMigration()
    .build()
```

## License

This translation integration is part of the Karhebti Android application.

