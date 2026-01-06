# 🧪 TEST GUIDE - Client-Side Document 500 Fix

## ⚡ Quick Test (2 minutes)

### Prerequisites
- App installed and running
- User logged in

### Test Steps

#### 1. **Load Documents List** (Populate Cache)
```
1. Open the app
2. Navigate to "Documents" screen
3. Wait for documents to load
4. ✅ Verify: List of documents displayed
```

**Expected Log:**
```
D/DocumentViewModel: 📦 Cached 5 documents
```

---

#### 2. **Click on Corrupted Document**
```
1. Find document with ID: 693f2e6cdc8ae671ede64f67
   (or any document you know returns 500 error)
2. Click on it
3. Wait 1-2 seconds
```

**Expected Behavior:**
- ✅ Document detail screen opens
- ✅ Warning banner displayed at top:
  ```
  📦 Données en cache
  Le serveur a rencontré une erreur. Ces informations 
  proviennent de votre liste de documents locale.
  ```
- ✅ Document information visible:
  - Type (e.g., "Assurance")
  - Date d'émission
  - Date d'expiration
  - Image (if not corrupted)
  - Description

**Expected Logs:**
```
D/DocumentViewModel: getDocumentById called with ID: 693f2e6cdc8ae671ede64f67
D/DocumentViewModel: Fetching document from repository...
D/DocumentRepository: Response code: 500
W/DocumentViewModel: ⚠️ Backend failed with 500 - Using cached data from list
W/DocumentViewModel: 📦 Cached document: assurance, [date]
D/DocumentDetailScreen: Document loaded: assurance
```

---

## 🎯 Detailed Test Scenarios

### Scenario A: Happy Path (Cache Available)

**Steps:**
1. Go to Documents list
2. Click on corrupted document
3. View details

**Expected:**
- ✅ Details displayed
- ✅ Warning banner visible
- ✅ Can scroll through information
- ✅ Edit button available
- ✅ Back button works

**Result:** ✅ PASS

---

### Scenario B: No Cache (Direct Navigation)

**Steps:**
1. Fresh app start (clear cache)
2. Navigate directly to document detail via deep link
3. Backend returns 500

**Expected:**
- ⚠️ Error screen displayed (original behavior)
- ⚠️ "Document corrompu" message
- ⚠️ Delete button available
- ℹ️ User instructed to view list first

**Result:** ⚠️ EXPECTED (cache not yet populated)

**Fix:** User goes back to list, then returns

---

### Scenario C: Backend Works Normally

**Steps:**
1. Click on a NON-corrupted document
2. Backend returns 200 OK

**Expected:**
- ✅ Details displayed
- ❌ NO warning banner (not using cache)
- ✅ Fresh data from server

**Result:** ✅ PASS

---

### Scenario D: Cache Stale Data

**Steps:**
1. Load documents list (cache populated)
2. Admin updates document on backend
3. User clicks document (gets 500 or cached data)

**Expected:**
- ℹ️ Cached data may be outdated
- ℹ️ Warning banner informs user
- ℹ️ User can refresh list to update cache

**Result:** ℹ️ EXPECTED BEHAVIOR

---

## 📊 Checklist

Before testing:
- [ ] App compiled successfully
- [ ] No compilation errors
- [ ] User logged in
- [ ] Internet connection available

During test:
- [ ] Documents list loads
- [ ] Cache populated (check logs)
- [ ] Corrupted document opens
- [ ] Warning banner visible
- [ ] Document details displayed
- [ ] No app crash
- [ ] Logs show cache usage

After test:
- [ ] Back navigation works
- [ ] Can view other documents
- [ ] Can refresh list
- [ ] Cache persists during session

---

## 🐛 Troubleshooting

### Issue: "No cached data available"

**Symptom:**
```
E/DocumentViewModel: ❌ Backend failed and no cached data available
```

**Cause:** Cache not populated (user didn't visit list first)

**Solution:**
1. Go to Documents list screen
2. Wait for load
3. Return to document detail

---

### Issue: Warning banner not showing

**Symptom:** Document displays without warning banner

**Possible Causes:**
1. Backend actually returned 200 OK (document fixed!)
2. Error message doesn't match detection criteria

**Check Logs:**
```
Look for:
- "Response code: 200" → Backend works (good!)
- "Response code: 500" → Should show warning
- "Using cached data" → Cache used successfully
```

---

### Issue: Error screen still showing

**Symptom:** Original error screen instead of document

**Possible Causes:**
1. Cache empty (see "No cached data available" above)
2. Document not in cache
3. Backend error not detected as 500

**Solution:**
1. Verify logs show cache population
2. Check document ID exists in cache
3. Visit list screen first

---

## 📱 Visual Verification

### What You Should See

#### ✅ SUCCESS STATE
```
┌─────────────────────────────────────────┐
│  [←]  Détails du Document         [✏️] │
├─────────────────────────────────────────┤
│  ┌───────────────────────────────────┐  │
│  │ ℹ️ 📦 Données en cache           │  │
│  │ Le serveur a rencontré une erreur │  │
│  │ Ces informations proviennent de   │  │
│  │ votre liste de documents locale.  │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │    Image du document              │  │
│  │  [       Document Image       ]   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │  📄 Type de document              │  │
│  │     ASSURANCE                     │  │
│  └───────────────────────────────────┘  │
│                                         │
│  Date d'émission: 01/01/2024           │
│  Date d'expiration: 31/12/2024         │
│                                         │
│  Véhicule: Toyota Corolla              │
└─────────────────────────────────────────┘
```

#### ❌ FAILURE STATE (Cache Empty)
```
┌─────────────────────────────────────────┐
│  [←]  Détails du Document         [✏️] │
├─────────────────────────────────────────┤
│                                         │
│           ⚠️                            │
│     Document Corrompu                   │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ ⚠️ Ce document contient           │  │
│  │ probablement des données          │  │
│  │ corrompues...                     │  │
│  │                                   │  │
│  │ Solutions possibles:              │  │
│  │ • Supprimer ce document           │  │
│  │ • Voir la liste d'abord           │  │
│  └───────────────────────────────────┘  │
│                                         │
│  [Supprimer le document corrompu] 🔴    │
│  [Retour à la liste]                   │
└─────────────────────────────────────────┘
```

---

## 🔍 Log Analysis

### Successful Cache Usage
```bash
# Step 1: Documents list loaded
D/DocumentViewModel: getDocuments
D/DocumentViewModel: 📦 Cached 8 documents

# Step 2: User clicks corrupted document
D/DocumentViewModel: getDocumentById called with ID: 693f2e...
D/DocumentViewModel: Fetching document from repository...
D/DocumentRepository: Fetching document with ID: 693f2e...
D/DocumentRepository: Response code: 500
E/DocumentRepository: Error body: {"statusCode":500,"message":"Internal server error"}

# Step 3: ViewModel uses cache
W/DocumentViewModel: ⚠️ Backend failed with 500 - Using cached data from list
W/DocumentViewModel: 📦 Cached document: assurance, Mon Dec 31 00:00:00 GMT 2024

# Step 4: UI displays document
D/DocumentDetailScreen: Document loaded: assurance
```

### Failed Cache Usage (Empty Cache)
```bash
# Step 1: User navigates directly to detail
D/DocumentViewModel: getDocumentById called with ID: 693f2e...
D/DocumentViewModel: Fetching document from repository...
D/DocumentRepository: Response code: 500

# Step 2: No cache available
E/DocumentViewModel: ❌ Backend failed and no cached data available

# Step 3: UI shows error
E/DocumentDetailScreen: Error: ⚠️ Ce document contient probablement...
```

---

## ✅ Success Criteria

Test is successful if:

1. ✅ User can view corrupted document details
2. ✅ Warning banner is displayed
3. ✅ All document information is visible
4. ✅ No app crashes
5. ✅ Logs show cache usage
6. ✅ Back navigation works
7. ✅ Normal documents still work

---

## 📋 Test Report Template

```
TEST REPORT: Client-Side Document 500 Fix
Date: [Date]
Tester: [Name]
App Version: [Version]
Device: [Device Model]

SCENARIO A: Cache Available
- Documents list loaded: [ ] Pass [ ] Fail
- Cache populated: [ ] Pass [ ] Fail
- Corrupted document opens: [ ] Pass [ ] Fail
- Warning banner visible: [ ] Pass [ ] Fail
- Details displayed: [ ] Pass [ ] Fail
Result: [ ] PASS [ ] FAIL

SCENARIO B: No Cache
- Direct navigation: [ ] Pass [ ] Fail
- Error screen shown: [ ] Pass [ ] Fail
- User can go back: [ ] Pass [ ] Fail
Result: [ ] PASS [ ] FAIL

SCENARIO C: Normal Document
- Document opens: [ ] Pass [ ] Fail
- No warning banner: [ ] Pass [ ] Fail
- Fresh data loaded: [ ] Pass [ ] Fail
Result: [ ] PASS [ ] FAIL

OVERALL RESULT: [ ] PASS [ ] FAIL

Notes:
[Add any observations or issues]
```

---

## 🎯 Quick Validation Commands

### Check if running
```bash
adb logcat | grep "DocumentViewModel"
```

### Filter for cache logs
```bash
adb logcat | grep "Cached"
```

### Filter for errors
```bash
adb logcat | grep "Backend failed"
```

---

**Test Duration:** ~5 minutes
**Complexity:** Low
**Prerequisites:** User account with documents
**Expected Result:** ✅ All scenarios pass

