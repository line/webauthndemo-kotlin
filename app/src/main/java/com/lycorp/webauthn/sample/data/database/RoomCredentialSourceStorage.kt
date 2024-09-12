/*
 * Copyright 2024 LY Corporation
 *
 * LY Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.lycorp.webauthn.sample.data.database

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lycorp.webauthn.db.CredentialSourceStorage
import com.lycorp.webauthn.model.PublicKeyCredentialSource
import com.lycorp.webauthn.model.PublicKeyCredentialType
import java.util.UUID

@Database(
    entities = [PubKeyCredSourceEntity::class],
    version = 1,
    exportSchema = false,
)
internal abstract class RoomCredentialSourceStorage : RoomDatabase(), CredentialSourceStorage {
    companion object {
        private var instance: RoomCredentialSourceStorage? = null

        fun build(context: Context): RoomCredentialSourceStorage {
            if (instance == null) {
                synchronized(RoomCredentialSourceStorage::class) {
                    instance =
                        Room.databaseBuilder(
                            context,
                            RoomCredentialSourceStorage::class.java,
                            "fido2_database",
                        ).build()
                }
            }
            return instance!!
        }
    }

    abstract fun credDao(): RoomCredDao

    override fun store(credSource: PublicKeyCredentialSource) {
        credDao().insertCredentialSource(
            PubKeyCredSourceEntity(
                credType = credSource.type,
                credId = credSource.id,
                rpId = credSource.rpId,
                userHandle = credSource.userHandle,
                aaguid = credSource.aaguid,
                signatureCounter = 0L,
            ),
        )
    }

    override fun loadAll(): List<PublicKeyCredentialSource> {
        val credSourceEntityList: List<PubKeyCredSourceEntity> =
            credDao().selectCredentialSources()
        return credSourceEntityList.map { entity ->
            PublicKeyCredentialSource(
                type = entity.credType,
                id = entity.credId,
                rpId = entity.rpId,
                userHandle = entity.userHandle,
                aaguid = entity.aaguid,
            )
        }
    }

    override fun load(credId: String): PublicKeyCredentialSource? {
        val credSourceEntity: PubKeyCredSourceEntity =
            credDao().selectCredentialSource(credId)
                ?: return null
        return PublicKeyCredentialSource(
            type = credSourceEntity.credType,
            id = credSourceEntity.credId,
            rpId = credSourceEntity.rpId,
            userHandle = credSourceEntity.userHandle,
            aaguid = credSourceEntity.aaguid,
        )
    }

    override fun delete(credId: String) {
        credDao().deleteCredentialSource(credId)
    }

    override fun increaseSignatureCounter(credId: String) {
        credDao().increaseSignatureCounter(credId)
    }

    override fun getSignatureCounter(credId: String): UInt {
        return credDao().selectSignatureCounter(credId).toUInt()
    }
}

// DAO interface
@Dao
internal interface RoomCredDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertCredentialSource(credSource: PubKeyCredSourceEntity)

    @Query("SELECT * FROM cred_source_table WHERE credId = :credId")
    fun selectCredentialSource(credId: String): PubKeyCredSourceEntity?

    @Query("SELECT * FROM cred_source_table")
    fun selectCredentialSources(): List<PubKeyCredSourceEntity>

    @Query("DELETE FROM cred_source_table WHERE credId = :credId")
    fun deleteCredentialSource(credId: String)

    @Query("DELETE FROM cred_source_table")
    fun deleteCredentialSources()

    @Query(
        "UPDATE cred_source_table SET signatureCounter = signatureCounter WHERE credId = :credId",
    )
    fun increaseSignatureCounter(credId: String)

    @Query("SELECT signatureCounter FROM cred_source_table WHERE credId = :credId")
    fun selectSignatureCounter(credId: String): Long
}

// Entity class
@Entity(tableName = "cred_source_table")
data class PubKeyCredSourceEntity(
    val credType: String = PublicKeyCredentialType.PUBLIC_KEY.value,
    @PrimaryKey
    var credId: String,
    val rpId: String,
    val userHandle: String?,
    val aaguid: UUID,
    val signatureCounter: Long,
)
