package com.kerosene.common.financial.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FinancialNotificationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void depositConfirmedNotification() throws Exception {
        var txId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var req = new FinancialDepositConfirmedNotificationRequest(
                123L, txId, walletId, "ONCHAIN", 50000L, 3
        );

        assertEquals(123L, req.userId());
        assertEquals(txId, req.transactionId());
        assertEquals(walletId, req.walletId());
        assertEquals("ONCHAIN", req.rail());
        assertEquals(50000L, req.creditedSats());
        assertEquals(3, req.confirmations());

        String json = mapper.writeValueAsString(req);
        var read = mapper.readValue(json, FinancialDepositConfirmedNotificationRequest.class);
        assertEquals(req, read);

        assertThrows(IllegalArgumentException.class, () ->
                new FinancialDepositConfirmedNotificationRequest(null, txId, walletId, "ONCHAIN", 50000L, 3));
        assertThrows(IllegalArgumentException.class, () ->
                new FinancialDepositConfirmedNotificationRequest(123L, txId, walletId, "ONCHAIN", 0L, 3));
        assertThrows(IllegalArgumentException.class, () ->
                new FinancialDepositConfirmedNotificationRequest(123L, txId, walletId, "ONCHAIN", 50000L, -1));
    }

    @Test
    void externalPaymentNotification() throws Exception {
        var txId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var req = new FinancialExternalPaymentNotificationRequest(
                123L, txId, walletId, "LIGHTNING", 1000L
        );

        String json = mapper.writeValueAsString(req);
        var read = mapper.readValue(json, FinancialExternalPaymentNotificationRequest.class);
        assertEquals(req, read);

        assertThrows(IllegalArgumentException.class, () ->
                new FinancialExternalPaymentNotificationRequest(123L, txId, walletId, "", 1000L));
        assertThrows(IllegalArgumentException.class, () ->
                new FinancialExternalPaymentNotificationRequest(123L, txId, walletId, "LIGHTNING", -100L));
    }

    @Test
    void internalTransferNotification() throws Exception {
        var txId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var req = new FinancialInternalTransferNotificationRequest(
                123L, txId, walletId, 25000L
        );

        String json = mapper.writeValueAsString(req);
        var read = mapper.readValue(json, FinancialInternalTransferNotificationRequest.class);
        assertEquals(req, read);

        assertThrows(IllegalArgumentException.class, () ->
                new FinancialInternalTransferNotificationRequest(123L, null, walletId, 25000L));
    }

    @Test
    void outboundNotification() throws Exception {
        var txId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var req = new FinancialOutboundNotificationRequest(
                123L, txId, walletId, "ONCHAIN", 75000L, 1, "bc1q..."
        );

        String json = mapper.writeValueAsString(req);
        var read = mapper.readValue(json, FinancialOutboundNotificationRequest.class);
        assertEquals(req, read);
    }

    @Test
    void paymentRequestDepositNotification() throws Exception {
        var txId = UUID.randomUUID();
        var prId = UUID.randomUUID();
        var walletId = UUID.randomUUID();
        var req = new FinancialPaymentRequestDepositConfirmedNotificationRequest(
                123L, txId, prId, "pr-public-123", walletId, "LIGHTNING", 10000L
        );

        String json = mapper.writeValueAsString(req);
        var read = mapper.readValue(json, FinancialPaymentRequestDepositConfirmedNotificationRequest.class);
        assertEquals(req, read);

        assertThrows(IllegalArgumentException.class, () ->
                new FinancialPaymentRequestDepositConfirmedNotificationRequest(
                        123L, txId, prId, "", walletId, "LIGHTNING", 10000L));
    }
}
