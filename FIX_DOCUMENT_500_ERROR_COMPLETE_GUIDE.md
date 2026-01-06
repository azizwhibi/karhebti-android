# 🔴 FIX: Document 500 Error - Complete Solution Guide

## 📋 Problem Summary

**Issue:** Document detail screen shows error instead of displaying document details
**Error:** HTTP 500 Internal Server Error
**Root Cause:** Corrupted data in MongoDB - the `voiture` field contains a complex object structure instead of a simple ObjectId reference
**Document ID:** `693f2e6cdc8ae671ede64f67`

### Log Evidence
```
Response code: 500
Error body: {"statusCode":500,"message":"Internal server error"}
⚠️ ERREUR 500 DÉTECTÉE - Probablement un document corrompu!
```

---

## 🎯 Solution Options

### ✅ Option 1: Fix Database (RECOMMENDED - Permanent Fix)

This is the **best solution** as it fixes the root cause in the database.

#### Step 1: Connect to MongoDB
```bash
# If using MongoDB Atlas
mongosh "mongodb+srv://your-cluster-url/karhebti"

# If using local MongoDB
mongosh mongodb://localhost:27017/karhebti
```

#### Step 2: Run the Cleanup Script
Use the existing cleanup script:
```bash
mongosh karhebti < cleanup_corrupted_documents.js
```

#### Step 3: Manual Fix (Alternative)
If the script doesn't work, manually fix the corrupted document:

```javascript
// Connect to database
use karhebti

// Find the corrupted document
db.documents.findOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })

// Check the voiture field structure
// If it's a complex object, extract just the ID

// Option A: Set voiture to null (safe option)
db.documents.updateOne(
  { _id: ObjectId("693f2e6cdc8ae671ede64f67") },
  { $set: { voiture: null } }
)

// Option B: Extract the car ID from the object (if available)
const doc = db.documents.findOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
if (doc.voiture && doc.voiture._id) {
  db.documents.updateOne(
    { _id: ObjectId("693f2e6cdc8ae671ede64f67") },
    { $set: { voiture: doc.voiture._id } }
  )
}

// Option C: Delete the corrupted document (if repair fails)
db.documents.deleteOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
```

#### Step 4: Find ALL Corrupted Documents
```javascript
use karhebti

// Find all documents with invalid voiture field
db.documents.find({
  voiture: { $type: "object" }
}).forEach(doc => {
  print(`Corrupted document: ${doc._id}`)
  print(`Type: ${doc.type}`)
  print(`Voiture field: ${JSON.stringify(doc.voiture).substring(0, 100)}...`)
})

// Count corrupted documents
const count = db.documents.countDocuments({ voiture: { $type: "object" } })
print(`Total corrupted documents: ${count}`)
```

#### Step 5: Bulk Fix All Corrupted Documents
```javascript
// Fix all corrupted documents by setting voiture to null
db.documents.updateMany(
  { voiture: { $type: "object" } },
  { $set: { voiture: null } }
)

// OR: Try to extract IDs from all corrupted documents
db.documents.find({ voiture: { $type: "object" } }).forEach(doc => {
  if (doc.voiture && doc.voiture._id) {
    db.documents.updateOne(
      { _id: doc._id },
      { $set: { voiture: doc.voiture._id.toString() } }
    )
  } else {
    db.documents.updateOne(
      { _id: doc._id },
      { $set: { voiture: null } }
    )
  }
})
```

---

### 🛠️ Option 2: Fix Backend (Makes it more resilient)

Modify the backend to handle corrupted data gracefully.

#### Location: Backend NestJS Service
File: `src/documents/documents.service.ts` (or similar)

#### Add Error Handling
```typescript
async findOne(id: string) {
  try {
    const document = await this.documentModel.findById(id).exec();
    
    if (!document) {
      throw new NotFoundException(`Document with ID ${id} not found`);
    }

    // 🔥 ADD THIS: Sanitize the voiture field
    if (document.voiture && typeof document.voiture === 'object') {
      // Extract ID if it's an object
      document.voiture = document.voiture._id || null;
    }

    return document;
  } catch (error) {
    // Handle corrupted data errors
    if (error.name === 'CastError') {
      throw new BadRequestException(
        'Document contains corrupted data. Please contact administrator.'
      );
    }
    throw error;
  }
}
```

#### Add Schema Validation
```typescript
import { Schema } from 'mongoose';

export const DocumentSchema = new Schema({
  type: { type: String, required: true },
  dateEmission: { type: Date, required: true },
  dateExpiration: { type: Date, required: true },
  fichier: { type: String, required: true },
  // 🔥 IMPORTANT: Ensure voiture is stored as ObjectId or String, not Object
  voiture: { 
    type: Schema.Types.ObjectId, 
    ref: 'Car',
    validate: {
      validator: function(v) {
        return !v || typeof v === 'string' || v instanceof Schema.Types.ObjectId;
      },
      message: 'voiture field must be a valid ObjectId or string'
    }
  },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now }
});

// Add pre-save hook to sanitize voiture field
DocumentSchema.pre('save', function(next) {
  if (this.voiture && typeof this.voiture === 'object' && this.voiture._id) {
    this.voiture = this.voiture._id;
  }
  next();
});
```

---

### 📱 Option 3: Frontend Workaround (Already Implemented)

The frontend now shows a user-friendly error message with options to:
1. Delete the corrupted document
2. Go back to the list
3. Contact administrator

**Status:** ✅ Already implemented in this commit

---

## 🔍 Verification Steps

### 1. Check if Fix Worked
```javascript
use karhebti
db.documents.findOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
// Should show voiture as null or a valid ObjectId string
```

### 2. Test in Android App
1. Rebuild the app
2. Navigate to the document detail screen
3. Verify the document loads successfully

### 3. Check Backend Logs
Look for successful GET requests without 500 errors:
```
GET /documents/693f2e6cdc8ae671ede64f67 200 OK
```

---

## 🚀 Quick Fix Commands

### If you have MongoDB access:
```bash
# Quick fix - Set voiture to null for all corrupted documents
mongosh karhebti --eval "db.documents.updateMany({ voiture: { \$type: 'object' } }, { \$set: { voiture: null } })"
```

### If you want to delete the specific corrupted document:
```bash
mongosh karhebti --eval "db.documents.deleteOne({ _id: ObjectId('693f2e6cdc8ae671ede64f67') })"
```

---

## 📊 Prevention

### 1. Add Backend Validation
Ensure all document creation/update endpoints validate the `voiture` field:
```typescript
@Post()
async create(@Body() createDocumentDto: CreateDocumentDto) {
  // Validate voiture field
  if (createDocumentDto.voiture && typeof createDocumentDto.voiture !== 'string') {
    throw new BadRequestException('voiture must be a valid Car ID string');
  }
  return this.documentsService.create(createDocumentDto);
}
```

### 2. Add Database Migration
Create a migration to fix all existing corrupted documents:
```javascript
// migration-fix-corrupted-voiture.js
db.documents.find({ voiture: { $type: "object" } }).forEach(doc => {
  let newVoiture = null;
  
  if (doc.voiture && doc.voiture._id) {
    newVoiture = doc.voiture._id.toString();
  }
  
  db.documents.updateOne(
    { _id: doc._id },
    { $set: { voiture: newVoiture } }
  );
  
  print(`Fixed document ${doc._id}`);
});
```

### 3. Add API Health Check
Create an endpoint to detect corrupted documents:
```typescript
@Get('health/corrupted')
async checkCorruptedDocuments() {
  const corrupted = await this.documentModel.aggregate([
    {
      $match: {
        voiture: { $type: 'object' }
      }
    },
    {
      $count: 'corrupted_count'
    }
  ]);
  
  return {
    status: corrupted[0]?.corrupted_count > 0 ? 'WARNING' : 'OK',
    corruptedDocuments: corrupted[0]?.corrupted_count || 0
  };
}
```

---

## 📝 Summary

| Solution | Difficulty | Impact | Permanence |
|----------|-----------|--------|------------|
| **Fix Database** | Easy | High | Permanent |
| Fix Backend | Medium | Medium | Permanent |
| Frontend Workaround | Easy | Low | Temporary |

**Recommended Action:** Fix the database using Option 1 ✅

---

## 🆘 If Nothing Works

1. **Export the document data** (before deleting):
   ```javascript
   use karhebti
   db.documents.find({ _id: ObjectId("693f2e6cdc8ae671ede64f67") }).forEach(printjson)
   ```

2. **Delete the corrupted document**:
   ```javascript
   db.documents.deleteOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
   ```

3. **Recreate it manually** with correct data structure

---

## ✅ Checklist

- [ ] Connected to MongoDB
- [ ] Identified corrupted document(s)
- [ ] Backed up document data (optional)
- [ ] Applied fix (Option 1, 2, or 3)
- [ ] Verified fix in database
- [ ] Tested in Android app
- [ ] Checked backend logs
- [ ] Applied prevention measures

---

**Last Updated:** January 6, 2026
**Status:** Frontend fix applied ✅ | Database fix pending ⏳

