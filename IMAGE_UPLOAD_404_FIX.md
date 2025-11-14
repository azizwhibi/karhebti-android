# Image Upload 404 Error - Troubleshooting Guide

## Problem
The image upload is returning a **404 Not Found** error:
```
<-- 404 Not Found http://10.0.2.2:3000/cars/6917033d7a2e035d625e6a7e/image (59ms)
{"message":"Cannot POST /cars/6917033d7a2e035d625e6a7e/image","error":"Not Found","statusCode":404}
```

## Root Cause
The Android app is correctly calling:
- **Endpoint**: `POST /cars/:id/image`
- **Multipart field**: `image`
- **Car ID**: `6917033d7a2e035d625e6a7e`

However, the backend server is returning 404, which means **the route is not registered on the backend**.

## Solution

### Backend Fix Required
The backend needs to implement the image upload endpoint. Here's what's needed:

#### Route Definition (Express.js)
```javascript
// In your cars router file
router.post('/:id/image', authenticate, upload.single('image'), uploadCarImage);
```

#### Handler Function
```javascript
const uploadCarImage = async (req, res) => {
  try {
    const { id } = req.params;
    
    // Validate car exists
    const car = await Car.findById(id);
    if (!car) {
      return res.status(404).json({ message: 'Car not found' });
    }
    
    // Check if file was uploaded
    if (!req.file) {
      return res.status(400).json({ message: 'No file uploaded' });
    }
    
    // Validate file size (5MB max)
    if (req.file.size > 5 * 1024 * 1024) {
      return res.status(413).json({ message: 'File too large' });
    }
    
    // Validate file type
    const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
    if (!allowedMimes.includes(req.file.mimetype)) {
      return res.status(415).json({ message: 'Unsupported file type' });
    }
    
    // Save file path to database
    car.imageUrl = `/uploads/cars/${req.file.filename}`;
    
    // Save image metadata if available
    if (req.file.metadata) {
      car.imageMeta = {
        width: req.file.metadata.width,
        height: req.file.metadata.height,
        format: req.file.metadata.format,
        size: req.file.size
      };
    }
    
    await car.save();
    
    res.json(car);
  } catch (error) {
    res.status(500).json({ message: 'Error uploading image' });
  }
};

module.exports = uploadCarImage;
```

#### Multer Configuration
```javascript
const multer = require('multer');
const path = require('path');

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, 'uploads/cars/');
  },
  filename: (req, file, cb) => {
    cb(null, `car_${Date.now()}${path.extname(file.originalname)}`);
  }
});

const upload = multer({
  storage,
  limits: { fileSize: 5 * 1024 * 1024 }, // 5MB
  fileFilter: (req, file, cb) => {
    const allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
    if (allowedMimes.includes(file.mimetype)) {
      cb(null, true);
    } else {
      cb(new Error('Invalid file type'));
    }
  }
});
```

#### Ensure Route is Registered in Main Server File
```javascript
// app.js or server.js
const carRoutes = require('./routes/cars');

// Mount the cars router
app.use('/cars', carRoutes);

// Make sure cars router includes the image upload route
// router.post('/:id/image', authenticate, upload.single('image'), uploadCarImage);
```

## Verification Steps

### 1. Check Backend Route Registration
Verify the route exists by:
```bash
# Test with curl
curl -X POST http://localhost:3000/cars/test-id/image \
  -H "Authorization: Bearer your-token" \
  -F "image=@/path/to/image.jpg"
```

### 2. Check Upload Directory
Ensure `/uploads/cars/` directory exists:
```bash
mkdir -p uploads/cars
chmod 755 uploads/cars
```

### 3. Verify Authentication
The endpoint requires authentication. Ensure:
- Authorization header is being sent (auto-handled by interceptor)
- Token is valid
- User is authenticated

### 4. Test with Postman/Insomnia
```
POST http://10.0.2.2:3000/cars/6917033d7a2e035d625e6a7e/image

Headers:
- Authorization: Bearer {valid-token}
- Content-Type: multipart/form-data

Body:
- Form field name: "image"
- File: select an image file
```

## Android Side - Already Correct ✅

The Android implementation is correct:

### API Service Definition ✅
```kotlin
@Multipart
@POST("cars/{id}/image")
suspend fun uploadCarImage(
    @Path("id") id: String,
    @Part image: MultipartBody.Part
): Response<CarResponse>
```

### Repository Implementation ✅
```kotlin
val multipartBody = MultipartBody.Part.createFormData(
    "image",           // ← Correct field name
    tempFile.name,
    requestBody
)

apiService.uploadCarImage(carId, multipartBody)  // ← Correct call
```

### Validation & Error Handling ✅
- File validation before upload
- Size check (5MB)
- MIME type validation
- Comprehensive error messages
- Detailed logging for debugging

## Current Error Messages

When 404 occurs, the app now displays:
```
"L'endpoint d'upload n'est pas disponible sur le serveur. Contactez l'administrateur."
```

With debug logs:
```
E/CarImageRepository: Route 404 - Le serveur backend n'a pas l'endpoint /cars/6917033d7a2e035d625e6a7e/image
```

## Next Steps

1. **Implement the backend endpoint** using the code above
2. **Restart the backend server**
3. **Test with Postman** to verify the endpoint works
4. **Retry the upload** from the Android app
5. **Verify the response** includes `imageUrl` field

## Expected Success Response

After implementing the backend route, you should see:
```json
{
  "_id": "6917033d7a2e035d625e6a7e",
  "marque": "Toyota",
  "modele": "Corolla",
  "annee": 2020,
  "immatriculation": "ABC-123",
  "typeCarburant": "Essence",
  "imageUrl": "/uploads/cars/car_1699950127000.jpg",
  "imageMeta": {
    "width": 1920,
    "height": 1440,
    "format": "jpeg",
    "size": 149236
  },
  "createdAt": "2025-11-14T10:00:00Z",
  "updatedAt": "2025-11-14T10:42:07Z"
}
```

## Debugging Log Output

Enable OkHttp logging to see detailed requests:

```kotlin
// In ApiConfig.kt, ensure HttpLoggingInterceptor is configured
val logging = HttpLoggingInterceptor()
logging.setLevel(HttpLoggingInterceptor.Level.BODY)  // Full request/response body
client.addInterceptor(logging)
```

You'll see:
```
--> POST /cars/6917033d7a2e035d625e6a7e/image
Content-Type: multipart/form-data; boundary=...
Authorization: Bearer {token}
Content-Length: 149236

[binary image data]

--> END POST (149236-byte body)

<-- 200 OK (59ms)
Content-Type: application/json; charset=utf-8

{"_id":"6917033d7a2e035d625e6a7e","imageUrl":"/uploads/cars/..."}

<-- END HTTP
```

## Common Backend Errors

| HTTP Code | Issue | Solution |
|-----------|-------|----------|
| 400 | Bad Request | Check multipart form field name is "image" |
| 401 | Unauthorized | Verify authentication token is valid |
| 403 | Forbidden | Check user permissions for this car |
| 404 | Not Found | **Route not registered - implement endpoint** |
| 413 | Payload Too Large | File exceeds 5MB limit |
| 415 | Unsupported Media Type | File MIME type not allowed |
| 500 | Server Error | Check server logs for exceptions |

## Summary

✅ **Android App**: Correctly implemented and sending proper requests  
❌ **Backend**: Missing `/cars/:id/image` endpoint implementation  

**Action Required**: Implement the backend endpoint as shown in the solution section above.


