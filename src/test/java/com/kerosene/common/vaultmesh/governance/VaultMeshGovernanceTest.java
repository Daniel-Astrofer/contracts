package com.kerosene.common.vaultmesh.governance;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VaultMeshGovernanceTest {

    @Test
    void dayAdvanceResultValidation() {
        var success = VaultMeshDayAdvanceResult.ok("2026-10-04", true);
        assertTrue(success.ok());
        assertTrue(success.advanced());
        assertEquals("2026-10-04", success.dayEpoch());
        assertNull(success.error());

        var failed = VaultMeshDayAdvanceResult.failed("Quorum unavailable");
        assertFalse(failed.ok());
        assertFalse(failed.advanced());
        assertNull(failed.dayEpoch());
        assertEquals("Quorum unavailable", failed.error());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshDayAdvanceResult("2026", true, true, "error"));
        assertThrows(IllegalArgumentException.class, () -> VaultMeshDayAdvanceResult.ok("", true));
        assertThrows(IllegalArgumentException.class, () -> VaultMeshDayAdvanceResult.failed(null));
    }

    @Test
    void dayStatusValidation() {
        var current = VaultMeshDayStatus.upToDate("2026-10-04");
        assertTrue(current.upToDate());
        assertFalse(current.stale());
        assertEquals("2026-10-04", current.dayEpoch());
        assertEquals("2026-10-04", current.neededDayEpoch());

        var stale = VaultMeshDayStatus.stale("2026-10-03", "2026-10-04");
        assertFalse(stale.upToDate());
        assertTrue(stale.stale());
        assertEquals("2026-10-03", stale.dayEpoch());
        assertEquals("2026-10-04", stale.neededDayEpoch());

        var failed = VaultMeshDayStatus.failed("Connection timeout");
        assertFalse(failed.upToDate());
        assertFalse(failed.stale());
        assertEquals("Connection timeout", failed.error());

        assertThrows(IllegalArgumentException.class, () -> new VaultMeshDayStatus("d", "d", true, true, null));
    }

    @Test
    void reshareResultValidation() {
        var reshare = new VaultMeshReshareResult(true, "epoch-1", "scheduled rotation", true, null);
        assertTrue(reshare.reshared());
        assertEquals("epoch-1", reshare.policy());
        assertEquals("scheduled rotation", reshare.reason());
        assertTrue(reshare.ok());
        assertNull(reshare.error());

        var success = VaultMeshReshareResult.ok("epoch-2", "quorum upgrade");
        assertTrue(success.ok());
        assertEquals("epoch-2", success.policy());

        var fail = VaultMeshReshareResult.failed("Not enough nodes");
        assertFalse(fail.ok());
        assertEquals("Not enough nodes", fail.error());
    }
}
