package com.phantomguard.security

import com.phantomguard.security.domain.engine.CommandVerificationEngine
import com.phantomguard.security.domain.engine.CommandVerificationResult
import com.phantomguard.security.domain.engine.RemoteCommandPayload
import org.junit.Assert.assertEquals
import org.junit.Test

class CommandVerificationEngineTest {

    private val localDeviceId = "dev_local_test_123"
    private val engine = CommandVerificationEngine(localDeviceId = localDeviceId)

    @Test
    fun testValidCommandSucceeds() {
        val now = System.currentTimeMillis()
        val command = RemoteCommandPayload(
            commandId = "cmd_1",
            deviceId = localDeviceId,
            ownerId = "usr_1",
            commandType = "LOCK",
            timestamp = now,
            expiresAt = now + 60000,
            nonce = "nonce_random_abc_1",
            payload = emptyMap(),
            signature = ""
        )
        val result = engine.verifyCommand(command)
        assertEquals(CommandVerificationResult.SUCCESS, result)
    }

    @Test
    fun testReplayNonceRejected() {
        val now = System.currentTimeMillis()
        val command = RemoteCommandPayload(
            commandId = "cmd_2",
            deviceId = localDeviceId,
            ownerId = "usr_1",
            commandType = "ALARM",
            timestamp = now,
            expiresAt = now + 60000,
            nonce = "nonce_duplicate_xyz",
            payload = emptyMap(),
            signature = ""
        )
        val firstResult = engine.verifyCommand(command)
        assertEquals(CommandVerificationResult.SUCCESS, firstResult)

        // Replay attempt with same nonce
        val replayResult = engine.verifyCommand(command)
        assertEquals(CommandVerificationResult.REJECTED_REPLAY_NONCE, replayResult)
    }

    @Test
    fun testExpiredCommandRejected() {
        val past = System.currentTimeMillis() - 100000
        val command = RemoteCommandPayload(
            commandId = "cmd_3",
            deviceId = localDeviceId,
            ownerId = "usr_1",
            commandType = "WIPE_DATA",
            timestamp = past - 10000,
            expiresAt = past,
            nonce = "nonce_expired_123",
            payload = emptyMap(),
            signature = ""
        )
        val result = engine.verifyCommand(command)
        assertEquals(CommandVerificationResult.REJECTED_EXPIRED, result)
    }

    @Test
    fun testMismatchTargetDeviceRejected() {
        val now = System.currentTimeMillis()
        val command = RemoteCommandPayload(
            commandId = "cmd_4",
            deviceId = "dev_other_victim_phone",
            ownerId = "usr_1",
            commandType = "LOCK",
            timestamp = now,
            expiresAt = now + 60000,
            nonce = "nonce_mismatch_456",
            payload = emptyMap(),
            signature = ""
        )
        val result = engine.verifyCommand(command)
        assertEquals(CommandVerificationResult.REJECTED_INVALID_TARGET, result)
    }
}
