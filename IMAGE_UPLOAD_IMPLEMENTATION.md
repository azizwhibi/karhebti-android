# Image Upload Functionality - Implementation Guide

## Overview
This guide documents the comprehensive image upload functionality implemented for the car management interface in the Karhebti Android application.

## Features Implemented

### 1. **Image Validation System** (`ImageUploadValidator.kt`)
- **File Type Validation**: Supports JPG, JPEG, PNG, and WebP formats
- **File Size Validation**: Maximum 5MB file size check
- **MIME Type Verification**: Validates actual file MIME type
- **Extension Verification**: Double-checks file extension
- **Detailed Error Messages**: User-friendly error messages in French
- **File Size Formatting**: Converts bytes to human-readable format (KB, MB)

**Location**: `util/ImageUploadValidator.kt`

**Key Methods**:
```kotlin
fun validateImage(context: Context, uri: Uri): ImageValidationResult
fun formatFileSize(bytes: Long): String
fun isValidImageFile(file: File): Boolean
```

### 2. **Progress Tracking** (`ProgressRequestBody.kt`)
- **Upload Progress Wrapper**: Wraps RequestBody to track upload progress
- **Progress Callback**: Real-time progress updates during upload
- **Progress State Model**: `ImageUploadProgress` data class with progress tracking

**Location**: `util/ProgressRequestBody.kt`

**Key Components**:
```kotlin
class ProgressRequestBody : RequestBody
data class ImageUploadProgress(
    val progress: Int,
    val isUploading: Boolean,
    val totalBytes: Long,
    val uploadedBytes: Long,
    val error: String?
)
```

### 3. **Enhanced Repository** (`CarImageRepository`)
- **Comprehensive Validation**: Pre-upload validation of image files
- **Multipart Upload**: Proper multipart/form-data implementation
- **Error Handling**: Detailed HTTP error code handling
  - 400: Invalid format or size
  - 401: Authentication required
  - 403: Access denied
  - 404: Vehicle not found
  - 413: File too large
  - 415: Unsupported media type
- **Temp File Management**: Automatic cleanup of temporary files
- **Logging**: Detailed logging at each step

**Location**: `data/repository/NewFeatureRepositories.kt`

**Key Methods**:
```kotlin
suspend fun uploadCarImage(
    carId: String,
    imageUri: Uri,
    context: Context
): Resource<CarResponse>

suspend fun deleteCarImage(carId: String): Resource<CarResponse>
```

### 4. **Enhanced ViewModel** (`CarImageViewModel`)
- **Upload State Management**: Tracks upload progress and results
- **Validation State**: Real-time validation feedback
- **Upload Status**: Boolean flag for upload in-progress
- **Image Validation**: Pre-selection validation

**Location**: `viewmodel/NewFeatureViewModels.kt`

**State Flows**:
```kotlin
val uploadState: StateFlow<Resource<CarResponse>?>
val validationState: StateFlow<String?>
val isUploading: StateFlow<Boolean>
```

### 5. **UI Components** (`ImageUploadComponents.kt`)
Reusable Compose components for image upload workflows:

#### **ImageUploadField**
- Image picker with tap-to-select functionality
- Image preview display
- Loading state overlay
- Validation error display
- Support text and format guidelines

#### **UploadProgressIndicator**
- Linear progress bar (0-100%)
- Progress percentage display
- Upload status text
- Responsive design

#### **CarImageDisplay**
- Image display with fallback
- Loading state
- Error state with message
- Empty state with placeholder

#### **ImageValidationError**
- Error badge display
- Dismiss button
- Error icon
- Material Design styling

**Location**: `ui/components/ImageUploadComponents.kt`

### 6. **Enhanced VehiclesScreen**
Complete integration of image upload functionality:

#### **Image Upload Dialog**
- Image selection interface
- Real-time validation feedback
- Upload progress display
- Success/error status indicators
- Format and size guidelines

#### **Add Vehicle Dialog Enhancement**
- Optional image selection during car creation
- Two-step upload: create car first, then upload image
- Pending image handling after car creation

#### **Vehicle Card Enhancement**
- Image preview in car list
- Clickable image to change
- Camera icon for upload action
- Integration with deletion flow

**Location**: `ui/screens/VehiclesScreen.kt`

## API Integration

### Backend Endpoint
```
POST /cars/upload-image/:id
Content-Type: multipart/form-data
Headers: Authorization: Bearer {token}
Body: file (image file)
```

### Request Implementation
- Uses Retrofit's `@Multipart` annotation
- Sends file as `MultipartBody.Part`
- Includes proper Authorization header (auto-handled by interceptor)

### Response Handling
The backend returns updated `CarResponse` with:
```json
{
  "_id": "car_id",
  "imageUrl": "/uploads/cars/filename.jpg",
  "imageMeta": {
    "width": 1920,
    "height": 1440,
    "format": "jpeg",
    "size": 512000
  },
  ...
}
```

## Usage Flow

### 1. **Adding Image to Existing Vehicle**
```
1. Tap camera icon on vehicle card
2. Dialog opens with image picker
3. Select image from device
4. Image validated on selection
5. Tap "Télécharger" button
6. Progress indicator shown
7. Success message displayed
8. List refreshed with new image
```

### 2. **Adding Image During Vehicle Creation**
```
1. Tap "+" FAB to create vehicle
2. Fill vehicle details
3. Tap image picker section
4. Select image (validated immediately)
5. Continue with vehicle creation
6. After car created, image automatically uploaded
7. Car list refreshed with image
```

### 3. **Validation Flow**
```
1. User selects image
2. File size checked (max 5MB)
3. MIME type verified
4. Extension validated
5. If valid: preview shown, upload enabled
6. If invalid: error message displayed, upload disabled
```

## Error Handling

### Validation Errors
- **"La taille de l'image dépasse 5MB (X.XX MB)"**: File too large
- **"Format non supporté. Formats acceptés: JPG, PNG, WebP"**: Wrong file type
- **"Extension de fichier non supportée: .xyz"**: Invalid extension
- **"Impossible de déterminer la taille du fichier"**: File read error

### Upload Errors
- **400**: "Format ou taille de fichier invalide"
- **401**: "Non authentifié"
- **403**: "Accès refusé"
- **404**: "Véhicule introuvable"
- **413**: "Fichier trop volumineux (max 5MB)"
- **415**: "Type de fichier non supporté"

### Network Errors
- **Connection timeout**: "Erreur réseau: [message]"
- **General exception**: "Erreur inattendue: [message]"

## State Management

### Resource States
```kotlin
sealed class Resource<T> {
    class Success<T>(data: T)
    class Error<T>(message: String)
    class Loading<T>()
}
```

### Upload Lifecycle
1. **Idle**: `uploadState = null`
2. **Loading**: `uploadState = Resource.Loading()`
3. **Success**: `uploadState = Resource.Success(carResponse)`
4. **Error**: `uploadState = Resource.Error(errorMessage)`

## File Handling

### Temporary Files
- Files created in app cache directory
- Format: `car_image_${System.currentTimeMillis()}.jpg`
- Automatically deleted after upload
- Cleanup happens even on error

### Image Serving
- Backend serves from: `/uploads/cars/{filename}`
- Images cached by Coil Image Library
- CrossFade animation during load

## Supported Formats
- **JPEG** (image/jpeg, image/jpg)
- **PNG** (image/png)
- **WebP** (image/webp)

## Size Limits
- **Maximum File Size**: 5 MB (5,242,880 bytes)
- **Minimum File Size**: > 0 bytes

## Logging

### Debug Logs
The implementation includes comprehensive logging:

```
CarImageRepository - "Starting image upload for car: {carId}"
CarImageRepository - "Image validated successfully. Size: {size} bytes"
CarImageRepository - "Temp file created: {path}, size: {size}"
CarImageRepository - "Image uploaded successfully! Car ID: {id}"
```

### Error Logs
```
CarImageRepository - "Image validation failed: {error}"
CarImageRepository - "Failed to create temp file: {error}"
CarImageRepository - "API returned error: {errorMsg}. Body: {body}"
```

## Testing Checklist

- [ ] Upload JPG image successfully
- [ ] Upload PNG image successfully
- [ ] Upload WebP image successfully
- [ ] Reject file > 5MB with appropriate error
- [ ] Reject non-image file with appropriate error
- [ ] Handle network timeout gracefully
- [ ] Handle 404 error (vehicle not found)
- [ ] Handle 401 error (not authenticated)
- [ ] Display upload progress correctly
- [ ] Refresh car list after successful upload
- [ ] Show success message after upload
- [ ] Show error message on upload failure
- [ ] Clean up temp files after upload
- [ ] Support image change (replace old with new)
- [ ] Display image in car card after upload
- [ ] Handle rapid consecutive uploads
- [ ] Validate image on selection before upload
- [ ] Show correct error messages for each error type

## Performance Considerations

1. **Image Compression**: Considered for future optimization
2. **Multipart Chunking**: Files streamed directly to avoid memory issues
3. **Cache Directory**: Temp files stored in cache (automatically cleaned by OS)
4. **Async Operations**: All network operations on IO dispatcher
5. **UI Thread Safety**: State flows ensure thread-safe UI updates

## Future Enhancements

1. **Real-time Progress Bars**: Enhanced with actual upload percentage
2. **Image Cropping**: Allow users to crop images before upload
3. **Batch Upload**: Support multiple images per vehicle
4. **Compression**: Automatic image compression before upload
5. **Retry Logic**: Automatic retry on network failure
6. **Image Deletion**: Backend support for removing images
7. **Image History**: Track multiple versions of car images
8. **Gallery Integration**: Better system integration for image selection

## Dependencies

- **Retrofit 2**: HTTP client
- **OkHttp3**: HTTP implementation with multipart support
- **Coil**: Image loading and caching
- **Compose**: UI framework
- **Coroutines**: Async operations

## Migration Notes

If integrating into existing project:

1. Add `ImageUploadValidator.kt` to `util/` package
2. Add `ProgressRequestBody.kt` to `util/` package
3. Update `CarImageRepository` in `data/repository/NewFeatureRepositories.kt`
4. Update `CarImageViewModel` in `viewmodel/NewFeatureViewModels.kt`
5. Add `ImageUploadComponents.kt` to `ui/components/` package
6. Update `VehiclesScreen.kt` with new dialogs and enhanced cards
7. Ensure `ImageUploadValidator` import in components
8. Verify API endpoint compatibility in `KarhebtiApiService.kt`

## Support Files Modified

- ✅ `VehiclesScreen.kt` - Enhanced with image upload UI
- ✅ `NewFeatureViewModels.kt` - Enhanced CarImageViewModel
- ✅ `NewFeatureRepositories.kt` - Enhanced CarImageRepository
- ✅ Created `ImageUploadValidator.kt`
- ✅ Created `ProgressRequestBody.kt`
- ✅ Created `ImageUploadComponents.kt`

## API Compliance

The implementation follows these requirements:

✅ Endpoint: POST /cars/upload-image/:id
✅ Content-Type: multipart/form-data
✅ Authorization: Bearer {token}
✅ File types: JPG, JPEG, PNG, WEBP
✅ Max size: 5MB
✅ Returns updated car object with imageUrl
✅ Images served from /uploads/cars/{filename}
✅ Proper validation and error handling
✅ Image replacement support


