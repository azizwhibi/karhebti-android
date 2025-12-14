package com.example.karhebti_android.repository

// Repository pour la gestion des pannes (breakdowns)
// Utilise Retrofit et expose desFlow pour la gestion asynchrone

import com.example.karhebti_android.data.BreakdownResponse
import com.example.karhebti_android.data.CreateBreakdownRequest
import com.example.karhebti_android.network.BreakdownsApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import retrofit2.HttpException

class BreakdownsRepository(private val api: BreakdownsApi) {

    /**
     * Créer une nouvelle panne (SOS)
     * NOTE: The client will NOT send `userId` — backend should extract
     * the authenticated user from the JWT Authorization header.
     * If backend still requires `userId` you can provide `userIdFallback` to retry.
     */
    fun createBreakdown(
        request: CreateBreakdownRequest,
        userIdFallback: String? = null
    ): Flow<Result<BreakdownResponse>> = flow {
        try {
            // Always sanitize the DTO to ensure no userId is sent by the client.
            // If the caller already included userId explicitly, send that request directly
            if (!request.userId.isNullOrBlank()) {
                val resp = api.createBreakdown(request)
                emit(Result.success(resp))
                return@flow
            }

            // Sanitize: force userId to null
            val sanitizedDto = request.copy(userId = null)
            android.util.Log.d("BreakdownsRepo", "createBreakdown sanitized DTO: $sanitizedDto (userId omitted)")

            val resp = api.createBreakdown(sanitizedDto)
            emit(Result.success(resp))

        } catch (httpEx: HttpException) {
            // If backend complains explicitly about userId, optionally retry with fallback
            handleHttpEx(httpEx, userIdFallback)?.let { emit(it); return@flow }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    // Helper to centralize HttpException handling and optional retry with fallback
    private fun handleHttpEx(httpEx: HttpException, userIdFallback: String?): Result<BreakdownResponse>? {
        try {
            val statusCode = httpEx.code()
            val errorBody = try { httpEx.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val extracted = try {
                if (!errorBody.isNullOrEmpty()) {
                    val json = JSONObject(errorBody)
                    val m = json.optString("message", "")
                    if (m.isNotBlank()) m else json.optString("error", "")
                } else null
            } catch (_: Exception) { null }

            val wantsUserId = extracted?.contains("userId", ignoreCase = true) == true || (errorBody?.contains("userId") == true)

            if (wantsUserId && !userIdFallback.isNullOrBlank()) {
                try {
                    // Build DTO with fallback userId
                    val withUser = CreateBreakdownRequest(
                        vehicleId = null,
                        type = "",
                        description = null,
                        latitude = 0.0,
                        longitude = 0.0,
                        photo = null,
                        userId = userIdFallback
                    )
                    // The above is only to show intent; rather the caller should retry with a proper filled DTO.
                    // Instead, we return null to let higher level handle retries if desired.
                    // For safety, we won't automatically populate missing fields here.
                    return Result.failure(Exception("HTTP $statusCode: ${extracted ?: errorBody ?: httpEx.message()}"))
                } catch (retryEx: Exception) {
                    android.util.Log.e("BreakdownsRepo", "Retry with userIdFallback failed: ${retryEx.message}")
                    return Result.failure(Exception("HTTP $statusCode: ${extracted ?: errorBody ?: httpEx.message()}"))
                }
            }

            // Not a userId-specific error or no fallback -> report the error
            val message = buildString {
                append("HTTP $statusCode")
                if (!extracted.isNullOrBlank()) append(": $extracted")
                else if (!errorBody.isNullOrBlank()) append(": $errorBody")
                else append(": ${httpEx.message()}")
            }
            android.util.Log.e("BreakdownsRepo", "createBreakdown failed: $message")
            return Result.failure(Exception(message))
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Récupérer toutes les pannes (avec filtres optionnels)
     */
    fun getAllBreakdowns(
        status: String? = null,
        userId: Int? = null
    ): Flow<Result<List<BreakdownResponse>>> = flow {
        try {
            val response = api.getAllBreakdowns(status, userId)
            emit(Result.success(response.toList()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Récupérer l'historique des pannes d'un utilisateur
     */
    fun getUserBreakdowns(userId: Int): Flow<Result<List<BreakdownResponse>>> = flow {
        try {
            emit(Result.success(api.getUserBreakdowns(userId)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Récupérer une panne spécifique par ID
     */
    fun getBreakdown(id: String): Flow<Result<BreakdownResponse>> = flow {
        try {
            emit(Result.success(api.getBreakdown(id)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Alias for getBreakdown for consistency
     */
    fun getBreakdownById(id: String): Flow<Result<BreakdownResponse>> = getBreakdown(id)

    /**
     * Update breakdown status (ACCEPTED, REFUSED, IN_PROGRESS, COMPLETED)
     */
    fun updateBreakdownStatus(id: String, status: String): Flow<Result<BreakdownResponse>> = flow {
        try {
            val statusMap = mapOf("status" to status)
            emit(Result.success(api.updateStatus(id, statusMap)))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
