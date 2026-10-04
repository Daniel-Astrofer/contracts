package io.kerosene.contracts.ledger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LedgerContractsTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @Test
    void journalDirectionValues() {
        assertEquals("debit", JournalDirection.DEBIT.getValue());
        assertEquals("credit", JournalDirection.CREDIT.getValue());
        assertEquals(JournalDirection.DEBIT, JournalDirection.fromString("debit"));
        assertEquals(JournalDirection.CREDIT, JournalDirection.fromString("CREDIT"));
        assertNull(JournalDirection.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> JournalDirection.fromString("invalid"));
    }

    @Test
    void ledgerAccountRoundtrip() throws Exception {
        var account = new LedgerAccountV1(
                "0.1.0",
                "acc-001",
                "custody",
                100_000L,
                10_000L,
                5_000L,
                2_000L,
                90_000L,
                10_000L,
                80_000L,
                1L,
                "0".repeat(64),
                List.of("hot"),
                "2026-01-01T00:00:00Z",
                "2026-01-02T00:00:00Z"
        );

        String json = mapper.writeValueAsString(account);
        assertTrue(json.contains("\"account_id\":\"acc-001\""));
        assertTrue(json.contains("\"available_sats\":100000"));

        var deserialized = mapper.readValue(json, LedgerAccountV1.class);
        assertEquals(account, deserialized);
    }

    @Test
    void ledgerAccountValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LedgerAccountV1(
                null, "acc", "type", 0, 0, 0, 0, 0, 0, 0, 1, "0".repeat(64), List.of(), "created", "updated"
        ));
        assertThrows(IllegalArgumentException.class, () -> new LedgerAccountV1(
                "0.1.0", "acc", "type", -1, 0, 0, 0, 0, 0, 0, 1, "0".repeat(64), List.of(), "created", "updated"
        ));
        assertThrows(IllegalArgumentException.class, () -> new LedgerAccountV1(
                "0.1.0", "acc", "type", 0, 0, 0, 0, 0, 0, 0, 1, "short", List.of(), "created", "updated"
        ));
    }

    @Test
    void ledgerJournalRoundtrip() throws Exception {
        var entry = new LedgerJournalV1(
                "0.1.0",
                "entry-100",
                "acc-001",
                "debit",
                50_000L,
                "Transfer out",
                "ref-100",
                "2026-01-01T12:00:00Z"
        );

        String json = mapper.writeValueAsString(entry);
        assertTrue(json.contains("\"entry_id\":\"entry-100\""));
        assertTrue(json.contains("\"direction\":\"debit\""));

        var deserialized = mapper.readValue(json, LedgerJournalV1.class);
        assertEquals(entry, deserialized);
    }

    @Test
    void ledgerJournalValidation() {
        assertThrows(IllegalArgumentException.class, () -> new LedgerJournalV1(
                "0.1.0", "e", "a", "invalid", 100L, "d", "r", "t"
        ));
        assertThrows(IllegalArgumentException.class, () -> new LedgerJournalV1(
                "0.1.0", "e", "a", "debit", 0L, "d", "r", "t"
        ));
        assertThrows(IllegalArgumentException.class, () -> new LedgerJournalV1(
                "0.1.0", "e", "a", "debit", -50L, "d", "r", "t"
        ));
    }
}
