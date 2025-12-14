# Car Selection & Auto-Maintenance Implementation Guide

## ✅ What Has Been Implemented

### 1. **Backend API Models Updated**
- **File**: `ApiModels.kt`
- Added `carId` field to `CreateReservationRequest`
```kotlin
data class CreateReservationRequest(
    @SerializedName("carId") val carId: String? = null, // ✅ NEW
    // ...other fields
)
```

### 2. **Repository Layer Updated**
- **File**: `Repositories.kt`
- Updated `createReservation()` method to accept `carId` parameter
- Passes `carId` to the API request

### 3. **ViewModel Layer Updated**
- **File**: `ViewModels.kt`
- Updated `ReservationViewModel.createReservation()` to accept `carId` parameter
- Forwards `carId` to repository

### 4. **UI Infrastructure Added**
- **File**: `ReservationScreen.kt`
- Added `CarViewModel` to fetch user's cars
- Added `MaintenanceViewModel` for creating maintenance
- Added state variables: `selectedCarId` and `carError`
- Added `carViewModel.getMyCars()` to LaunchedEffect

## 🚧 What Still Needs to Be Done

### 1. **Add Car Selection UI Component**
You need to add a new form section between Step 2 (Time) and Step 3 (Services):

```kotlin
// Add this AFTER Step 2 (Time selection) and BEFORE Step 3 (Services)

// Step 3: Car Selection (NEW)
ModernFormSection(
    stepNumber = "3",
    title = "Sélectionner une voiture",
    isComplete = selectedCarId != null
) {
    when (carsState) {
        is Resource.Loading -> {
            Box(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = DeepPurple,
                    strokeWidth = 4.dp
                )
            }
        }
        is Resource.Success -> {
            val cars = (carsState as Resource.Success).data ?: emptyList()
            if (cars.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AccentYellow.copy(alpha = 0.1f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Aucune voiture enregistrée",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Ajoutez une voiture dans votre profil",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                var expanded by remember { mutableStateOf(false) }
                
                StyledExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    selectedValue = cars.find { it.id == selectedCarId }?.let {
                        "${it.marque} ${it.modele} (${it.immatriculation})"
                    } ?: "Choisir une voiture",
                    label = "Voiture",
                    items = cars.map { "${it.marque} ${it.modele} (${it.immatriculation})" },
                    onItemSelected = { selected ->
                        selectedCarId = cars.find { 
                            "${it.marque} ${it.modele} (${it.immatriculation})" == selected 
                        }?.id
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Show selected car details
                selectedCarId?.let { carId ->
                    cars.find { it.id == carId }?.let { car ->
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = DeepPurple.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = DeepPurple,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "${car.marque} ${car.modele} (${car.annee})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DeepPurple,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Immatriculation: ${car.immatriculation}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        is Resource.Error -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AlertRed.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Error, null, tint = AlertRed)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Erreur de chargement des voitures",
                        color = AlertRed
                    )
                }
            }
        }
        else -> {}
    }
    
    AnimatedVisibility(visible = carError != null) {
        Row(
            modifier = Modifier.padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = AlertRed
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = carError ?: "",
                color = AlertRed,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
```

### 2. **Update Step Numbers**
After adding the car selection step:
- Car Selection becomes Step 3
- Services becomes Step 4
- Comments becomes Step 5

### 3. **Update Form Validation**
Update the progress indicator and form validation to include car selection (make it optional):

```kotlin
// Update progress calculation
LinearProgressIndicator(
    progress = when {
        selectedDate != null && selectedStartTime != null &&
                selectedEndTime != null && selectedServiceTypes.isNotEmpty() -> 1f
        selectedDate != null && selectedStartTime != null && selectedEndTime != null -> 0.75f
        selectedDate != null -> 0.5f
        else -> 0.25f
    },
    // ...
)
```

### 4. **Update Reservation Submission**
Update the button onClick to include `carId`:

```kotlin
Button(
    onClick = {
        dateError = validateSelectedDate(selectedDate)
        timeError = validateSelectedTimes(selectedStartTime, selectedEndTime, totalDuration)
        servicesError = validateServices(selectedServiceTypes)

        if (dateError == null && timeError == null && servicesError == null && bayError == null) {
            reservationViewModel.createReservation(
                garageId = garageId,
                date = selectedDate?.let { dateFormat.format(it.time) } ?: "",
                heureDebut = selectedStartTime ?: "",
                heureFin = selectedEndTime ?: "",
                services = selectedServiceTypes,
                commentaires = commentaires,
                carId = selectedCarId  // ✅ ADD THIS
            )
        }
    },
    // ...
)
```

### 5. **Backend Requirements**

The backend needs to:

1. **Accept `carId` in reservation creation**:
```javascript
// POST /api/reservations
{
  "garageId": "693eca621a8120b07e87b453",
  "carId": "user_car_id_here",  // NEW FIELD
  "date": "2025-12-15",
  "heureDebut": "09:00",
  "heureFin": "11:00",
  "services": ["vidange", "revision"],
  "commentaires": "Optional comment"
}
```

2. **Store `carId` in reservation document**:
```javascript
const reservationSchema = new mongoose.Schema({
  userId: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
  garageId: { type: mongoose.Schema.Types.ObjectId, ref: 'Garage' },
  carId: { type: mongoose.Schema.Types.ObjectId, ref: 'Car' },  // NEW
  date: Date,
  heureDebut: String,
  heureFin: String,
  services: [String],
  status: String,
  commentaires: String
});
```

3. **Auto-create maintenance when reservation is confirmed**:
```javascript
// When reservation status changes to "confirmé"
reservationRouter.patch('/:id/status', async (req, res) => {
  const { status } = req.body;
  const reservation = await Reservation.findByIdAndUpdate(
    req.params.id,
    { status },
    { new: true }
  );
  
  // ✅ If status is "confirmé", create maintenance
  if (status === 'confirmé' && reservation.carId) {
    const maintenance = await Maintenance.create({
      type: reservation.services.join(', '),
      title: `Entretien suite à réservation`,
      date: reservation.date,
      dueAt: reservation.date,
      cout: 0,  // Calculate from services
      garage: reservation.garageId,
      voiture: reservation.carId,
      user: reservation.userId
    });
    
    console.log('✅ Maintenance created automatically:', maintenance._id);
  }
  
  res.json(reservation);
});
```

## 📋 Testing Checklist

### Frontend Testing
- [ ] Car dropdown shows user's cars
- [ ] Can select a car from dropdown
- [ ] Selected car details display correctly
- [ ] Reservation submission includes `carId`
- [ ] Form works without selecting a car (optional field)

### Backend Testing
- [ ] `carId` is saved in reservation document
- [ ] Reservation creation works with and without `carId`
- [ ] When reservation status changes to "confirmé", maintenance is created automatically
- [ ] Maintenance includes correct car, garage, and service details
- [ ] User can see the auto-created maintenance in their maintenance list

## 🎯 Expected Flow

1. **User creates reservation**:
   - Selects date, time, services
   - Optionally selects a car
   - Submits reservation

2. **Garage owner confirms reservation**:
   - Updates status to "confirmé"
   - Backend automatically creates maintenance record

3. **User sees maintenance**:
   - Maintenance appears in user's maintenance list
   - Linked to the selected car
   - Contains services from the reservation

## 📝 Notes

- Car selection is **optional** - users can make reservations without selecting a car
- Only confirmed reservations create maintenance records
- The backend is responsible for creating maintenance when status changes
- Make sure to handle cases where `carId` is null

## 🔧 Quick Implementation Steps

1. Add the car selection UI component (copy code from Section 1 above)
2. Update step numbers (3→4, 4→5)
3. Add `carId` to reservation submission (Section 4)
4. Update backend to accept and store `carId`
5. Implement backend logic to auto-create maintenance on confirmation
6. Test the complete flow

That's it! The infrastructure is ready, you just need to add the UI component and update the backend.

