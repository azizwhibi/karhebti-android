# Swift iOS Marketplace Hub Implementation Prompt

You are an expert iOS engineer. Implement a complete **Marketplace Hub Screen** with exactly 4 main sections matching the Android implementation, plus full marketplace and real-time chat functionality.

---

## 🎯 EXACT SCREEN STRUCTURE REQUIRED

### Main Marketplace Hub Screen Layout

Create a **MarketplaceHubView** that displays exactly 4 action cards in a 2x2 grid:

```
┌─────────────────────────────────────┐
│   🚗 Car Marketplace                │
│                                     │
│  ┌──────────┐  ┌──────────┐       │
│  │  🔍      │  │  🏷️      │       │
│  │ Browse   │  │ My       │       │
│  │ Cars     │  │ Listings │       │
│  └──────────┘  └──────────┘       │
│                                     │
│  ┌──────────┐  ┌──────────┐       │
│  │  💬      │  │  🔔      │       │
│  │ Conver-  │  │ Requests │       │
│  │ sations  │  │          │       │
│  └──────────┘  └──────────┘       │
└─────────────────────────────────────┘
```

**Visual Design Requirements:**
- Large, tappable cards with SF Symbols icons
- Icon + label stacked vertically
- Material 3 style elevation/shadows
- Rounded corners (16-20dp corner radius)
- Primary color scheme matching app theme
- Responsive grid layout (2 columns, adaptive spacing)

---

## 📱 FOUR CORE FEATURES TO IMPLEMENT

### 1️⃣ Browse Cars (Tinder-Style Swipe)
**Icon:** `safari` or `magnifyingglass.circle.fill`  
**Label:** "Browse Cars"

**Functionality:**
- Swipeable card interface (like Tinder)
- Display available cars from marketplace
- Swipe RIGHT = interested (creates swipe request)
- Swipe LEFT = not interested (pass)
- Card shows: car image, make/model, year, mileage, fuel type, price
- Visual feedback: overlay labels "INTERESTED" (green) / "PASS" (red) during swipe
- When all cards viewed: "That's all for now!" empty state
- Real-time WebSocket connection for instant match notifications
- If seller accepts your swipe → show "It's a Match!" dialog → navigate to conversation

**Backend Integration:**
- GET `/cars/marketplace/available` → List of available cars
- POST `/swipes` with `{ carId, direction: "right" | "left" }`
- WebSocket event `swipe_accepted` → triggers match dialog

---

### 2️⃣ My Listings (Manage Your Cars For Sale)
**Icon:** `tag.fill` or `dollarsign.circle.fill`  
**Label:** "My Listings"

**Functionality:**
- List ALL user's cars (owned vehicles)
- Each car card shows:
  - Car image (or placeholder)
  - Make, model, year
  - Current status badge: "Listed for Sale" (green) / "Not Listed" (gray)
  - Price (if listed)
  - Action buttons:
    - **If NOT listed:** "List for Sale" button → opens dialog (price + description input)
    - **If LISTED:** "Unlist" button → removes from marketplace
- Pull-to-refresh to reload cars
- Image handling: prepend base URL to relative paths `/uploads/cars/...`
- Success feedback: "Car listed successfully!" / "Car unlisted"

**Backend Integration:**
- GET `/cars` (user's own cars via auth token)
- POST `/cars/{id}/list-for-sale` with `{ price: Double, description: String? }`
- POST `/cars/{id}/unlist`
- Response: `MarketplaceCarResponse` with updated `forSale` status

**Data Model Notes:**
- Backend returns `forSale` field → map to `isForSale` in Swift model
- Use `CodingKeys` for proper JSON mapping

---

### 3️⃣ Conversations (Active Chats)
**Icon:** `message.fill` or `bubble.left.and.bubble.right.fill`  
**Label:** "Conversations"

**Functionality:**
- List of all active conversations (from accepted swipes)
- Each conversation row shows:
  - Other user's name (buyer/seller depending on role)
  - Car details (make/model) that conversation is about
  - Last message preview
  - Timestamp (relative: "2m ago", "1h ago", "Yesterday")
  - Unread count badge (red circle with number)
- Tap conversation → navigate to ChatDetailView
- Real-time updates: new messages appear instantly via WebSocket
- Connection status banner at top: "Connecting..." / "Connected" / "Offline - Reconnecting..."
- Empty state: "No conversations yet. Accept a pending request to start chatting."

**Backend Integration:**
- GET `/conversations` → List of conversations
- WebSocket `/chat` → receives `new_message` events
- Auto-refresh list when new message arrives

**Conversation Model Fields:**
- `id`, `carId` (can be object or string), `buyerId`, `sellerId`, `participants`, `lastMessage`, `lastMessageAt`, `unreadCount`, `createdAt`

---

### 4️⃣ Requests (Pending Swipes You Received)
**Icon:** `bell.fill` or `hand.raised.fill`  
**Label:** "Requests"

**Functionality:**
- Shows swipes from OTHER users on YOUR listed cars
- Each request card displays:
  - Buyer's name (from swipe data)
  - Car they're interested in (make/model/year)
  - Swipe timestamp
  - Two action buttons:
    - ✅ **Accept** (green) → creates conversation, navigates to chat
    - ❌ **Decline** (red) → removes swipe
- Real-time updates: new swipes appear instantly via WebSocket event `swipe_right`
- Loading states with 10-second timeout
- Dialog after accept: "Conversation started! Start chatting now?" → navigate to chat
- Empty state: "No pending requests"

**Backend Integration:**
- GET `/swipes/pending` → List of swipes received
- POST `/swipes/{id}/accept` → Returns `{ status: "accepted", conversationId: "..." }`
- POST `/swipes/{id}/decline` → Returns `{ status: "declined" }`
- WebSocket event `swipe_right` → triggers list refresh

---

## 🔧 BACKEND CONFIGURATION

### Environment Setup
```swift
enum APIEnvironment {
    case emulator
    case device
    case production
    
    var baseURL: String {
        switch self {
        case .emulator: return "http://127.0.0.1:3000"
        case .device: return "http://192.168.1.190:3000"
        case .production: return "https://api.yourprod.com" // TODO
        }
    }
    
    var wsURL: String {
        switch self {
        case .emulator: return "ws://127.0.0.1:3000/chat"
        case .device: return "ws://192.168.1.190:3000/chat"
        case .production: return "wss://api.yourprod.com/chat" // TODO
        }
    }
}
```

### REST API Endpoints (Exact Paths)

**Marketplace:**
```
GET    /cars/marketplace/available      → [MarketplaceCar]
POST   /cars/{id}/list-for-sale         → MarketplaceCar
       Body: { price: Double, description: String? }
POST   /cars/{id}/unlist                → MarketplaceCar
```

**Swipes:**
```
POST   /swipes                          → SwipeResponse
       Body: { carId: String, direction: "left"|"right" }
POST   /swipes/{id}/accept              → SwipeStatusResponse
POST   /swipes/{id}/decline             → SwipeStatusResponse
GET    /swipes/pending                  → [SwipeResponse]
```

**Conversations:**
```
GET    /conversations                   → [Conversation]
GET    /conversations/{id}              → Conversation
GET    /conversations/{id}/messages     → [ChatMessage]
POST   /conversations/{id}/messages     → ChatMessage
       Body: { content: String }
POST   /conversations/{id}/mark-read    → { message: String }
```

**User Cars:**
```
GET    /cars                            → [CarResponse] (user's cars)
```

### WebSocket Protocol (Raw WebSocket, NOT Socket.IO)

**Connection:** `ws://192.168.1.190:3000/chat`

**Outgoing Events (Client → Server):**
```json
{ "event": "join_conversation", "conversationId": "<id>" }
{ "event": "leave_conversation", "conversationId": "<id>" }
{ "event": "send_message", "conversationId": "<id>", "content": "<text>" }
{ "event": "typing", "conversationId": "<id>" }
```

**Incoming Events (Server → Client):**
```json
{
  "event": "new_message",
  "data": {
    "id": "...",
    "conversationId": "...",
    "senderId": "...",
    "content": "...",
    "createdAt": "2024-01-15T10:30:00.000Z"
  }
}

{
  "event": "notification",
  "data": {
    "type": "swipe_right" | "swipe_accepted" | "swipe_declined",
    "title": "...",
    "message": "...",
    "data": { "conversationId": "...", ... }
  }
}

{ "event": "user_typing", "userId": "...", "conversationId": "..." }
{ "event": "user_online", "userId": "..." }
{ "event": "user_offline", "userId": "..." }
{ "event": "joined_conversation", "conversationId": "..." }
```

**Parser Resilience:** Support 3 formats:
1. `{ event, data }` ← primary
2. `{ type, ... }` ← fallback
3. Direct fields ← legacy

---

## 📊 DATA MODELS (Swift Codable)

### MarketplaceCar
```swift
struct MarketplaceCar: Codable, Identifiable {
    let id: String
    let marque: String
    let modele: String
    let annee: Int
    let immatriculation: String
    let typeCarburant: String
    let kilometrage: Int?
    let statut: String?
    let imageUrl: String?
    let images: [String]?
    let price: Double?
    let description: String?
    let isForSale: Bool
    let saleStatus: String?
    let user: String? // seller ID
    let ownerName: String?
    let ownerPhone: String?
    let createdAt: Date?
    let updatedAt: Date?
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case marque, modele, annee, immatriculation, typeCarburant
        case kilometrage, statut, imageUrl, images, price, description
        case isForSale = "forSale" // IMPORTANT: backend uses "forSale"
        case saleStatus, user, ownerName, ownerPhone, createdAt, updatedAt
    }
    
    var fullImageURL: URL? {
        guard let imageUrl = imageUrl else { return nil }
        if imageUrl.starts(with: "http") {
            return URL(string: imageUrl)
        }
        let path = imageUrl.starts(with: "/") ? imageUrl : "/\(imageUrl)"
        return URL(string: "\(APIEnvironment.current.baseURL)\(path)")
    }
}
```

### SwipeResponse
```swift
struct SwipeResponse: Codable, Identifiable {
    let id: String
    let carDetails: MarketplaceCar?
    let buyerDetails: UserSummary?
    let sellerId: UserSummary?
    let direction: String?
    let status: String? // "pending", "accepted", "declined"
    let createdAt: Date?
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case carDetails = "carId"
        case buyerDetails = "userId"
        case sellerId, direction, status, createdAt
    }
}

struct SwipeStatusResponse: Codable {
    let id: String
    let status: String
    let conversationId: String?
    let message: String
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case status, conversationId, message
    }
}
```

### Conversation
```swift
struct Conversation: Codable, Identifiable {
    let id: String
    let carId: MarketplaceCar?
    let buyerId: UserSummary?
    let sellerId: UserSummary?
    let participants: [String]?
    let status: String?
    let lastMessage: String?
    let lastMessageAt: Date?
    let unreadCount: Int?
    let createdAt: Date?
    let updatedAt: Date?
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case carId, buyerId, sellerId, participants, status
        case lastMessage, lastMessageAt, unreadCount, createdAt, updatedAt
    }
    
    func getOtherUser(currentUserId: String) -> UserSummary? {
        if buyerId?.id == currentUserId {
            return sellerId
        } else {
            return buyerId
        }
    }
}
```

### ChatMessage
```swift
struct ChatMessage: Codable, Identifiable {
    let id: String
    let conversationId: String
    let senderId: String
    let content: String
    let isRead: Bool
    let createdAt: Date
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case conversationId, senderId, content, isRead, createdAt
    }
}
```

### UserSummary
```swift
struct UserSummary: Codable, Identifiable {
    let id: String?
    let nom: String
    let prenom: String
    let email: String
    let telephone: String?
    
    enum CodingKeys: String, CodingKey {
        case id = "_id"
        case nom, prenom, email, telephone
    }
    
    var displayName: String {
        "\(prenom) \(nom)"
    }
}
```

---

## 🏗️ ARCHITECTURE STRUCTURE

### Repository Layer
```swift
protocol MarketplaceRepository {
    func fetchAvailableCars() async throws -> [MarketplaceCar]
    func listCarForSale(carId: String, price: Double, description: String?) async throws -> MarketplaceCar
    func unlistCar(carId: String) async throws -> MarketplaceCar
    func createSwipe(carId: String, direction: SwipeDirection) async throws -> SwipeResponse
    func acceptSwipe(swipeId: String) async throws -> SwipeStatusResponse
    func declineSwipe(swipeId: String) async throws -> SwipeStatusResponse
    func fetchPendingSwipes() async throws -> [SwipeResponse]
}

protocol ConversationRepository {
    func fetchConversations() async throws -> [Conversation]
    func fetchMessages(conversationId: String) async throws -> [ChatMessage]
    func sendMessage(conversationId: String, content: String) async throws -> ChatMessage
    func markAsRead(conversationId: String) async throws
}

protocol ChatWebSocketService {
    func connect() async throws
    func disconnect()
    func joinConversation(_ id: String) async
    func leaveConversation(_ id: String) async
    func sendMessage(conversationId: String, content: String) async
    func sendTyping(conversationId: String) async
    var events: AsyncStream<ChatEvent> { get }
}
```

### ViewModels (ObservableObject)

**MarketplaceHubViewModel:**
- Manages navigation state
- Holds references to child ViewModels
- Coordinates WebSocket connection lifecycle

**BrowseCarsViewModel:**
- `@Published var cars: [MarketplaceCar] = []`
- `@Published var currentIndex: Int = 0`
- `@Published var isLoading = false`
- `@Published var showMatchDialog = false`
- `@Published var matchedConversationId: String?`
- Functions: `loadCars()`, `swipeLeft()`, `swipeRight()`

**MyListingsViewModel:**
- `@Published var myCars: [MarketplaceCar] = []`
- `@Published var isLoading = false`
- `@Published var showListDialog = false`
- Functions: `loadMyCars()`, `listCar(id, price, description)`, `unlistCar(id)`

**ConversationsViewModel:**
- `@Published var conversations: [Conversation] = []`
- `@Published var connectionState: WebSocketState = .disconnected`
- `@Published var isLoading = false`
- Functions: `loadConversations()`, `connectWebSocket()`, `handleNewMessage()`

**PendingSwipesViewModel:**
- `@Published var pendingSwipes: [SwipeResponse] = []`
- `@Published var isLoading = false`
- Functions: `loadPendingSwipes()`, `acceptSwipe(id)`, `declineSwipe(id)`

**ChatDetailViewModel:**
- `@Published var messages: [ChatMessage] = []`
- `@Published var isTyping = false`
- `@Published var otherUserTyping = false`
- Functions: `loadMessages()`, `sendMessage()`, `markRead()`, `handleTyping()`

---

## 🎨 UI IMPLEMENTATION (SwiftUI)

### MarketplaceHubView (Main Entry Point)
```swift
struct MarketplaceHubView: View {
    @StateObject private var viewModel = MarketplaceHubViewModel()
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // Header
                    Text("🚗 Car Marketplace")
                        .font(.title)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal)
                    
                    // 2x2 Grid of Action Cards
                    LazyVGrid(columns: [
                        GridItem(.flexible(), spacing: 16),
                        GridItem(.flexible(), spacing: 16)
                    ], spacing: 16) {
                        MarketplaceActionCard(
                            icon: "safari",
                            label: "Browse Cars",
                            destination: BrowseCarsView()
                        )
                        
                        MarketplaceActionCard(
                            icon: "tag.fill",
                            label: "My Listings",
                            destination: MyListingsView()
                        )
                        
                        MarketplaceActionCard(
                            icon: "message.fill",
                            label: "Conversations",
                            destination: ConversationsView()
                        )
                        
                        MarketplaceActionCard(
                            icon: "bell.fill",
                            label: "Requests",
                            destination: PendingSwipesView()
                        )
                    }
                    .padding(.horizontal)
                }
                .padding(.vertical)
            }
            .navigationTitle("Marketplace")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct MarketplaceActionCard<Destination: View>: View {
    let icon: String
    let label: String
    let destination: Destination
    
    var body: some View {
        NavigationLink(destination: destination) {
            VStack(spacing: 12) {
                Image(systemName: icon)
                    .font(.system(size: 40))
                    .foregroundColor(.white)
                
                Text(label)
                    .font(.headline)
                    .foregroundColor(.white)
                    .multilineTextAlignment(.center)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 140)
            .background(
                RoundedRectangle(cornerRadius: 20)
                    .fill(Color.blue.gradient)
            )
            .shadow(color: .black.opacity(0.1), radius: 8, x: 0, y: 4)
        }
    }
}
```

### BrowseCarsView (Swipeable Cards)
```swift
struct BrowseCarsView: View {
    @StateObject private var viewModel = BrowseCarsViewModel()
    @State private var dragOffset: CGSize = .zero
    
    var body: some View {
        ZStack {
            if viewModel.isLoading {
                ProgressView()
            } else if viewModel.currentIndex >= viewModel.cars.count {
                EmptyStateView(
                    icon: "checkmark.circle",
                    title: "That's all for now!",
                    message: "Check back later for more cars",
                    actionButton: ("Refresh", { viewModel.loadCars() })
                )
            } else {
                // Swipeable card stack
                ForEach(viewModel.cars.indices.reversed(), id: \.self) { index in
                    if index >= viewModel.currentIndex {
                        CarSwipeCard(car: viewModel.cars[index])
                            .offset(dragOffset)
                            .rotationEffect(.degrees(Double(dragOffset.width / 20)))
                            .gesture(
                                DragGesture()
                                    .onChanged { value in
                                        dragOffset = value.translation
                                    }
                                    .onEnded { value in
                                        if abs(value.translation.width) > 150 {
                                            let direction: SwipeDirection = value.translation.width > 0 ? .right : .left
                                            withAnimation {
                                                dragOffset = CGSize(width: direction == .right ? 500 : -500, height: 0)
                                            }
                                            Task {
                                                if direction == .right {
                                                    await viewModel.swipeRight()
                                                } else {
                                                    await viewModel.swipeLeft()
                                                }
                                            }
                                        } else {
                                            withAnimation {
                                                dragOffset = .zero
                                            }
                                        }
                                    }
                            )
                            .zIndex(Double(viewModel.cars.count - index))
                    }
                }
            }
        }
        .navigationTitle("Browse Cars")
        .task { await viewModel.loadCars() }
        .alert("It's a Match! 🎉", isPresented: $viewModel.showMatchDialog) {
            Button("Start Chatting") {
                // Navigate to chat
            }
            Button("Later", role: .cancel) { }
        }
    }
}
```

### MyListingsView
```swift
struct MyListingsView: View {
    @StateObject private var viewModel = MyListingsViewModel()
    
    var body: some View {
        List(viewModel.myCars) { car in
            CarListingRow(car: car) {
                if car.isForSale {
                    Button("Unlist", role: .destructive) {
                        Task { await viewModel.unlistCar(car.id) }
                    }
                } else {
                    Button("List for Sale") {
                        viewModel.selectedCar = car
                        viewModel.showListDialog = true
                    }
                }
            }
        }
        .refreshable { await viewModel.loadMyCars() }
        .navigationTitle("My Listings")
        .sheet(isPresented: $viewModel.showListDialog) {
            ListCarSheet(viewModel: viewModel)
        }
    }
}
```

### ConversationsView
```swift
struct ConversationsView: View {
    @StateObject private var viewModel = ConversationsViewModel()
    
    var body: some View {
        VStack(spacing: 0) {
            // Connection status banner
            if viewModel.connectionState != .connected {
                ConnectionBanner(state: viewModel.connectionState)
            }
            
            List(viewModel.conversations) { conversation in
                NavigationLink(destination: ChatDetailView(conversationId: conversation.id)) {
                    ConversationRow(conversation: conversation)
                }
            }
        }
        .navigationTitle("Conversations")
        .task {
            await viewModel.loadConversations()
            await viewModel.connectWebSocket()
        }
    }
}
```

### PendingSwipesView
```swift
struct PendingSwipesView: View {
    @StateObject private var viewModel = PendingSwipesViewModel()
    
    var body: some View {
        List(viewModel.pendingSwipes) { swipe in
            PendingSwipeRow(swipe: swipe) {
                HStack(spacing: 12) {
                    Button {
                        Task { await viewModel.acceptSwipe(swipe.id) }
                    } label: {
                        Label("Accept", systemImage: "checkmark.circle.fill")
                            .foregroundColor(.green)
                    }
                    .buttonStyle(.bordered)
                    
                    Button(role: .destructive) {
                        Task { await viewModel.declineSwipe(swipe.id) }
                    } label: {
                        Label("Decline", systemImage: "xmark.circle.fill")
                    }
                    .buttonStyle(.bordered)
                }
            }
        }
        .navigationTitle("Pending Requests")
        .refreshable { await viewModel.loadPendingSwipes() }
    }
}
```

---

## ⚙️ WEBSOCKET IMPLEMENTATION

```swift
actor ChatWebSocketClient {
    private var webSocketTask: URLSessionWebSocketTask?
    private var continuation: AsyncStream<ChatEvent>.Continuation?
    private let baseURL: String
    private var isConnected = false
    
    init(baseURL: String) {
        self.baseURL = baseURL
    }
    
    func connect() async throws {
        guard !isConnected else { return }
        
        let url = URL(string: baseURL)!
        webSocketTask = URLSession.shared.webSocketTask(with: url)
        webSocketTask?.resume()
        isConnected = true
        
        // Start receiving messages
        Task { await receiveMessages() }
    }
    
    func disconnect() {
        webSocketTask?.cancel(with: .goingAway, reason: nil)
        isConnected = false
    }
    
    func send(event: OutgoingChatEvent) async {
        guard let task = webSocketTask else { return }
        let encoder = JSONEncoder()
        if let data = try? encoder.encode(event),
           let json = String(data: data, encoding: .utf8) {
            let message = URLSessionWebSocketTask.Message.string(json)
            try? await task.send(message)
        }
    }
    
    var events: AsyncStream<ChatEvent> {
        AsyncStream { continuation in
            self.continuation = continuation
        }
    }
    
    private func receiveMessages() async {
        while isConnected {
            do {
                let message = try await webSocketTask?.receive()
                if case .string(let text) = message {
                    if let event = parseEvent(text) {
                        continuation?.yield(event)
                    }
                }
            } catch {
                isConnected = false
                continuation?.finish()
            }
        }
    }
    
    private func parseEvent(_ json: String) -> ChatEvent? {
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        
        // Try format 1: { event, data }
        if let wrapper = try? decoder.decode(EventWrapper.self, from: json.data(using: .utf8)!) {
            switch wrapper.event {
            case "new_message":
                if let msg = try? decoder.decode(ChatMessage.self, from: wrapper.data) {
                    return .newMessage(msg)
                }
            case "notification":
                // Parse notification
                return .notification(...)
            case "user_typing":
                // Parse typing
                return .typing(...)
            default:
                return .raw(json)
            }
        }
        
        return nil
    }
}
```

---

## 🔄 REAL-TIME BEHAVIOR

### Auto-Reconnect Strategy
- Exponential backoff: 1s, 2s, 4s, 8s, max 30s
- Max 5 attempts before falling back to HTTP polling
- Heartbeat/ping every 30 seconds

### Optimistic UI Updates
- Messages: insert immediately with `.sending` status
- Update to `.sent` when WebSocket echo received
- Show retry button on failure

### Typing Indicators
- Send initial typing event
- Throttle: max every 3 seconds while typing
- Auto-stop after 2 seconds of inactivity

---

## ✅ TESTING REQUIREMENTS

### Unit Tests
- MarketplaceCar JSON decoding (forSale → isForSale mapping)
- SwipeResponse parsing with nested objects
- WebSocket event parsing resilience
- Repository error handling

### UI Tests
- Swipe gesture detection
- List/unlist flow
- Accept/decline swipe flow
- Message send/receive

### Edge Cases
- Empty states for all 4 screens
- 10-second loading timeout
- Network errors
- Duplicate message deduplication
- Image URL normalization (relative paths)

---

## 📋 IMPLEMENTATION CHECKLIST

### Phase 1: Data Layer
- [ ] Define all Codable models with proper CodingKeys
- [ ] Implement MarketplaceRepository
- [ ] Implement ConversationRepository
- [ ] Create ChatWebSocketClient actor

### Phase 2: ViewModels
- [ ] BrowseCarsViewModel with swipe logic
- [ ] MyListingsViewModel with list/unlist
- [ ] ConversationsViewModel with WebSocket
- [ ] PendingSwipesViewModel with accept/decline
- [ ] ChatDetailViewModel

### Phase 3: UI Components
- [ ] MarketplaceHubView (4-card grid)
- [ ] BrowseCarsView (swipeable cards)
- [ ] MyListingsView (car list + dialogs)
- [ ] ConversationsView (chat list)
- [ ] PendingSwipesView (swipe requests)
- [ ] ChatDetailView (message bubbles)

### Phase 4: Real-Time
- [ ] WebSocket connection management
- [ ] Event parsing with fallback formats
- [ ] Typing indicators
- [ ] Auto-reconnect with backoff
- [ ] Connection status UI

### Phase 5: Polish
- [ ] Loading states with timeout
- [ ] Empty states with illustrations
- [ ] Error handling with user-friendly messages
- [ ] Pull-to-refresh
- [ ] Accessibility labels
- [ ] Dark mode support

---

## 🎯 VISUAL REFERENCE (Android Implementation)

Your Android app has this exact structure in `HomeScreen.kt`:

```kotlin
// Marketplace Section (lines 337-374)
Text("🚗 Car Marketplace")

// Row 1
QuickActionButton(icon = Icons.Default.Explore, label = "Browse Cars")
QuickActionButton(icon = Icons.Default.Sell, label = "My Listings")

// Row 2
QuickActionButton(icon = Icons.Default.Chat, label = "Conversations")
QuickActionButton(icon = Icons.Default.Notifications, label = "Requests")
```

**Replicate this EXACTLY in Swift** with the same visual hierarchy, spacing, and functionality.

---

## 🚀 DELIVERABLES

Generate complete, production-ready Swift code for:

1. **MarketplaceHubView** - Main screen with 4 action cards
2. **BrowseCarsView** - Tinder-style swipe interface
3. **MyListingsView** - List/unlist car management
4. **ConversationsView** - Active chat list
5. **PendingSwipesView** - Swipe request management
6. **ChatDetailView** - Individual chat screen
7. **All ViewModels** - With proper state management
8. **Repository implementations** - With real API calls
9. **WebSocket client** - Event-based with resilience
10. **Data models** - With proper JSON mapping

Include inline documentation, error handling, and TODO markers for customization points.

---

**Generate all code now. Focus on matching the Android implementation's structure and behavior EXACTLY.**

