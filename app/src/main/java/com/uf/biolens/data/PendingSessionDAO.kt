/*
 * Copyright (c) 2022 University of Florida
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.uf.biolens.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingSessionDAO {

    @Insert
    suspend fun insert(session: PendingSession): Long

    @Delete
    suspend fun delete(session: PendingSession)

    @Query("DELETE FROM pending_sessions WHERE requestCode = :requestCode")
    suspend fun deleteByRequestCode(requestCode: Long)

    @Query("SELECT * FROM pending_sessions WHERE requestCode = :requestCode")
    suspend fun getPendingSession(requestCode: Long): PendingSession?

    @Query("SELECT * FROM pending_sessions ORDER BY datetime(scheduledDateTime)")
    suspend fun getAllPendingSessions(): List<PendingSession>

    @Query("SELECT * FROM pending_sessions ORDER BY datetime(scheduledDateTime)")
    fun getAllPendingSessionsFlow(): Flow<List<PendingSession>>

    @Query("SELECT * FROM pending_sessions ORDER BY datetime(scheduledDateTime) limit 1")
    fun getEarliestPendingSession(): Flow<PendingSession?>
}
