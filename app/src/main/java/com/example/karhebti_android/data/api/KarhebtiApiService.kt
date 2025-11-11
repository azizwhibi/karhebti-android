package com.example.karhebti_android.data.api

import retrofit2.Response
import retrofit2.http.*

interface KarhebtiApiService {

    // ==================== AUTH ====================

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    // ==================== USERS ====================

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserResponse>>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserResponse>

    @POST("users")
    suspend fun createUser(@Body request: SignupRequest): Response<UserResponse>

    @PATCH("users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body request: UpdateUserRequest
    ): Response<UserResponse>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<MessageResponse>

    @PATCH("users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: String,
        @Body request: UpdateRoleRequest
    ): Response<UserResponse>

    // ==================== CARS ====================

    @GET("cars")
    suspend fun getMyCars(): Response<List<CarResponse>>

    @GET("cars/{id}")
    suspend fun getCar(@Path("id") id: String): Response<CarResponse>

    @POST("cars")
    suspend fun createCar(@Body request: CreateCarRequest): Response<CarResponse>

    @PATCH("cars/{id}")
    suspend fun updateCar(
        @Path("id") id: String,
        @Body request: UpdateCarRequest
    ): Response<CarResponse>

    @DELETE("cars/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>

    // ==================== MAINTENANCES ====================

    @GET("maintenances")
    suspend fun getMaintenances(): Response<List<MaintenanceResponse>>

    @GET("maintenances/{id}")
    suspend fun getMaintenance(@Path("id") id: String): Response<MaintenanceResponse>

    @POST("maintenances")
    suspend fun createMaintenance(@Body request: CreateMaintenanceRequest): Response<MaintenanceResponse>

    @PATCH("maintenances/{id}")
    suspend fun updateMaintenance(
        @Path("id") id: String,
        @Body request: UpdateMaintenanceRequest
    ): Response<MaintenanceResponse>

    @DELETE("maintenances/{id}")
    suspend fun deleteMaintenance(@Path("id") id: String): Response<MessageResponse>

    // ==================== GARAGES ====================

    @GET("garages")
    suspend fun getGarages(): Response<List<GarageResponse>>

    @GET("garages/{id}")
    suspend fun getGarage(@Path("id") id: String): Response<GarageResponse>

    @POST("garages")
    suspend fun createGarage(@Body request: CreateGarageRequest): Response<GarageResponse>

    @PATCH("garages/{id}")
    suspend fun updateGarage(
        @Path("id") id: String,
        @Body request: UpdateGarageRequest
    ): Response<GarageResponse>

    @DELETE("garages/{id}")
    suspend fun deleteGarage(@Path("id") id: String): Response<MessageResponse>

    // ==================== DOCUMENTS ====================

    @GET("documents")
    suspend fun getDocuments(): Response<List<DocumentResponse>>

    @GET("documents/{id}")
    suspend fun getDocument(@Path("id") id: String): Response<DocumentResponse>

    @POST("documents")
    suspend fun createDocument(@Body request: CreateDocumentRequest): Response<DocumentResponse>

    @PATCH("documents/{id}")
    suspend fun updateDocument(
        @Path("id") id: String,
        @Body request: UpdateDocumentRequest
    ): Response<DocumentResponse>

    @DELETE("documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String): Response<MessageResponse>

    // ==================== PARTS ====================

    @GET("parts")
    suspend fun getParts(): Response<List<PartResponse>>

    @GET("parts/{id}")
    suspend fun getPart(@Path("id") id: String): Response<PartResponse>

    @POST("parts")
    suspend fun createPart(@Body request: CreatePartRequest): Response<PartResponse>

    @PATCH("parts/{id}")
    suspend fun updatePart(
        @Path("id") id: String,
        @Body request: CreatePartRequest
    ): Response<PartResponse>

    @DELETE("parts/{id}")
    suspend fun deletePart(@Path("id") id: String): Response<MessageResponse>

    // ==================== SERVICES ====================

    @GET("services")
    suspend fun getServices(): Response<List<ServiceResponse>>

    @GET("services/{id}")
    suspend fun getService(@Path("id") id: String): Response<ServiceResponse>

    @GET("services/garage/{garageId}")
    suspend fun getServicesByGarage(@Path("garageId") garageId: String): Response<List<ServiceResponse>>

    @POST("services")
    suspend fun createService(@Body request: CreateServiceRequest): Response<ServiceResponse>

    @PATCH("services/{id}")
    suspend fun updateService(
        @Path("id") id: String,
        @Body request: CreateServiceRequest
    ): Response<ServiceResponse>

    @DELETE("services/{id}")
    suspend fun deleteService(@Path("id") id: String): Response<MessageResponse>

    // ==================== RECLAMATIONS (FEEDBACK) ====================

    @GET("reclamations")
    suspend fun getReclamations(): Response<List<ReclamationResponse>>

    @GET("reclamations/{id}")
    suspend fun getReclamation(@Path("id") id: String): Response<ReclamationResponse>

    @GET("reclamations/garage/{garageId}")
    suspend fun getReclamationsByGarage(@Path("garageId") garageId: String): Response<List<ReclamationResponse>>

    @GET("reclamations/service/{serviceId}")
    suspend fun getReclamationsByService(@Path("serviceId") serviceId: String): Response<List<ReclamationResponse>>

    @POST("reclamations")
    suspend fun createReclamation(@Body request: CreateReclamationRequest): Response<ReclamationResponse>

    @PATCH("reclamations/{id}")
    suspend fun updateReclamation(
        @Path("id") id: String,
        @Body request: UpdateReclamationRequest
    ): Response<ReclamationResponse>

    @DELETE("reclamations/{id}")
    suspend fun deleteReclamation(@Path("id") id: String): Response<MessageResponse>

    // ==================== AI FEATURES ====================

    @POST("ai/report-road-issue")
    suspend fun reportRoadIssue(@Body request: ReportRoadIssueRequest): Response<RoadIssueResponse>

    @GET("ai/danger-zones")
    suspend fun getDangerZones(
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("rayon") rayon: Double? = null
    ): Response<List<DangerZone>>

    @POST("ai/maintenance-recommendations")
    suspend fun getMaintenanceRecommendations(
        @Body request: MaintenanceRecommendationRequest
    ): Response<MaintenanceRecommendationResponse>

    @GET("ai/garage-recommendation")
    suspend fun getGarageRecommendations(
        @Query("typePanne") typePanne: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("rayon") rayon: Double? = null
    ): Response<List<GarageRecommendation>>

    // ==================== NOTIFICATIONS ====================

    @GET("notifications")
    suspend fun getNotifications(): Response<List<NotificationResponse>>

    @GET("notifications/mes-notifications")
    suspend fun getMyNotifications(): Response<List<NotificationResponse>>

    @GET("notifications/non-lues")
    suspend fun getUnreadNotifications(): Response<List<NotificationResponse>>

    @PATCH("notifications/{id}/marquer-lu")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<NotificationResponse>

    @PATCH("notifications/marquer-toutes-lues")
    suspend fun markAllNotificationsAsRead(): Response<MessageResponse>

    @DELETE("notifications/{id}")
    suspend fun deleteNotification(@Path("id") id: String): Response<MessageResponse>
}

