# 🚀 QUICK REFERENCE - Fix Document 500 Error

## ⚡ 30-Second Fix

```javascript
// MongoDB Command (fastest solution)
use karhebti
db.documents.updateOne(
  { _id: ObjectId("693f2e6cdc8ae671ede64f67") },
  { $set: { voiture: null } }
)
```

---

## 📋 Quick Commands

### Fix ONE document
```javascript
use karhebti
db.documents.updateOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") }, { $set: { voiture: null } })
```

### Fix ALL corrupted documents
```javascript
use karhebti
db.documents.updateMany({ voiture: { $type: "object" } }, { $set: { voiture: null } })
```

### Count corrupted documents
```javascript
use karhebti
db.documents.countDocuments({ voiture: { $type: "object" } })
```

### Delete the corrupted document
```javascript
use karhebti
db.documents.deleteOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
```

---

## 🛠️ Tools Available

| Tool | Command | Time |
|------|---------|------|
| **PowerShell Script** | `.\run_cleanup_database.ps1` | 2 min |
| **MongoDB Script** | `mongosh karhebti < cleanup_corrupted_documents_auto.js` | 1 min |
| **Direct Command** | See commands above | 30 sec |
| **Android App** | Delete button in error screen | 10 sec |

---

## 📁 Documentation Files

| File | Purpose |
|------|---------|
| `ACTION_FIX_DOCUMENT_500.md` | 5-minute action guide |
| `FIX_DOCUMENT_500_ERROR_COMPLETE_GUIDE.md` | Complete detailed guide |
| `SUMMARY_FIX_DOCUMENT_500.md` | Summary of all changes |
| `VISUAL_FIX_DOCUMENT_500.md` | Visual diagrams |
| `QUICK_REFERENCE_FIX_DOCUMENT_500.md` | This file |

---

## ✅ Verification

```javascript
// Check the document is fixed
use karhebti
db.documents.findOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
// voiture should be: null OR ObjectId("...") OR "string24chars"
// voiture should NOT be: { _id: "...", marque: "...", ... }
```

---

## 🔍 Troubleshooting

| Problem | Solution |
|---------|----------|
| mongosh not found | `winget install MongoDB.Shell` |
| Connection refused | Check MongoDB URL/credentials |
| Still 500 error | Restart backend, clear app cache |
| Document not found | Already deleted or wrong ID |

---

## 📞 Quick Links

- [Full Guide](./FIX_DOCUMENT_500_ERROR_COMPLETE_GUIDE.md)
- [Action Steps](./ACTION_FIX_DOCUMENT_500.md)
- [Visual Diagrams](./VISUAL_FIX_DOCUMENT_500.md)
- [Summary](./SUMMARY_FIX_DOCUMENT_500.md)

---

## 🎯 One-Liner Solutions

### Windows (PowerShell)
```powershell
.\run_cleanup_database.ps1
```

### MongoDB Shell
```bash
mongosh karhebti --eval "db.documents.updateOne({_id:ObjectId('693f2e6cdc8ae671ede64f67')},{`$set:{voiture:null}})"
```

### MongoDB Script
```bash
mongosh karhebti < cleanup_corrupted_documents_auto.js
```

---

## ⏱️ Time Estimates

| Method | Time | Difficulty | Risk |
|--------|------|------------|------|
| Direct command | 30 sec | Easy | Low |
| PowerShell script | 2 min | Easy | Low |
| MongoDB script | 1 min | Medium | Low |
| App deletion | 10 sec | Very Easy | Medium (data loss) |

---

## 🆘 Emergency

If nothing works:

```javascript
// Nuclear option: Delete the document
use karhebti
db.documents.deleteOne({ _id: ObjectId("693f2e6cdc8ae671ede64f67") })
```

---

**Last Updated:** January 6, 2026
**Status:** Ready to use ✅

