package org.aion.precompiled.TRS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import org.aion.base.type.AionAddress;
import org.aion.vm.FastVmResultCode;
import org.aion.precompiled.contracts.DummyRepo;
import org.aion.precompiled.contracts.TRS.AbstractTRS;
import org.aion.precompiled.contracts.TRS.TRSuseContract;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** A class testing the linked list data structure inside the public-facing TRS contract. */
public class TRSlinkedListTest extends TRShelpers {

    @Before
    public void setup() {
        repo = new DummyRepo();
        ((DummyRepo) repo).storageErrorReturn = null;
        tempAddrs = new ArrayList<>();
        repo.addBalance(AION, BigInteger.ONE);
    }

    @After
    public void tearDown() {
        for (AionAddress acct : tempAddrs) {
            repo.deleteAccount(acct);
        }
        tempAddrs = null;
        repo = null;
        senderKey = null;
    }

    @Test
    public void testLinkedListNoDepositors() {
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        TRSuseContract trs = newTRSuseContract(acct);
        assertNull(getLinkedListHead(trs, contract));
    }

    @Test
    public void testLinkedListOneDepositor() {
        // First test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        byte[] input = getDepositInput(contract, BigInteger.ONE);
        TRSuseContract trs = newTRSuseContract(acct);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        checkLinkedListOneDepositor(trs, contract, acct, input);

        repo.incrementNonce(acct);

        // Now test using depositFor.
        contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        input = getDepositForInput(contract, acct, BigInteger.ONE);
        trs = newTRSuseContract(acct);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        checkLinkedListOneDepositor(trs, contract, acct, input);
    }

    @Test
    public void testLinkedListTwoDepositors() {
        // First test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        byte[] input = getDepositInput(contract, BigInteger.ONE);

        TRSuseContract trs = newTRSuseContract(acct);
        trs.execute(input, COST);

        trs = newTRSuseContract(acct2);
        trs.execute(input, COST);
        trs.execute(input, COST);

        trs = newTRSuseContract(acct);
        trs.execute(input, COST);

        checkLinkedListTwoDepositors(trs, contract, acct, acct2);

        // Test using depositFor.
        repo.incrementNonce(acct);
        repo.incrementNonce(acct2);
        contract = createTRScontract(acct, false, false, 1, BigInteger.ZERO, 0);
        input = getDepositForInput(contract, acct, BigInteger.ONE);

        trs = newTRSuseContract(acct);
        trs.execute(input, COST);

        input = getDepositForInput(contract, acct2, BigInteger.ONE);
        trs = newTRSuseContract(acct);
        trs.execute(input, COST);
        trs.execute(input, COST);

        input = getDepositForInput(contract, acct, BigInteger.ONE);
        trs = newTRSuseContract(acct);
        trs.execute(input, COST);

        checkLinkedListTwoDepositors(trs, contract, acct, acct2);
    }

    @Test
    public void testLinkedListMultipleDepositors() {
        // First test using deposit.
        AionAddress acct1, acct2, acct3, acct4;
        acct1 = getNewExistentAccount(DEFAULT_BALANCE);
        acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        acct3 = getNewExistentAccount(DEFAULT_BALANCE);
        acct4 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct1, false, true, 1, BigInteger.ZERO, 0);
        byte[] input = getDepositInput(contract, BigInteger.ONE);

        newTRSuseContract(acct1).execute(input, COST);
        newTRSuseContract(acct4).execute(input, COST);
        newTRSuseContract(acct2).execute(input, COST);
        newTRSuseContract(acct4).execute(input, COST);
        newTRSuseContract(acct1).execute(input, COST);
        newTRSuseContract(acct3).execute(input, COST);
        newTRSuseContract(acct1).execute(input, COST);

        checkLinkedListMultipleDepositors(contract, acct1, acct2, acct3, acct4);

        // Test using depositFor.
        acct1 = getNewExistentAccount(DEFAULT_BALANCE);
        acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        acct3 = getNewExistentAccount(DEFAULT_BALANCE);
        acct4 = getNewExistentAccount(DEFAULT_BALANCE);
        contract = createTRScontract(acct1, false, false, 1, BigInteger.ZERO, 0);

        input = getDepositForInput(contract, acct1, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct4, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct2, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct4, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct1, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct3, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);
        input = getDepositForInput(contract, acct1, BigInteger.ONE);
        newTRSuseContract(acct1).execute(input, COST);

        checkLinkedListMultipleDepositors(contract, acct1, acct2, acct3, acct4);
    }

    @Test
    public void testRemoveHeadOfListWithHeadOnly() {
        // Test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        byte[] input = getDepositInput(contract, DEFAULT_BALANCE);
        TRSuseContract trs = newTRSuseContract(acct);

        checkRemoveHeadOfListWithHeadOnly(trs, contract, acct, input);

        // Test using depositFor
        acct = getNewExistentAccount(DEFAULT_BALANCE);
        contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);
        input = getDepositForInput(contract, acct, DEFAULT_BALANCE);
        trs = newTRSuseContract(acct);

        checkRemoveHeadOfListWithHeadOnly(trs, contract, acct, input);
    }

    @Test
    public void testRemoveHeadOfListWithHeadAndNextOnly() {
        // Test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);

        byte[] input = getDepositInput(contract, DEFAULT_BALANCE);
        TRSuseContract trs = newTRSuseContract(acct);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        assertEquals(
                FastVmResultCode.SUCCESS, newTRSuseContract(acct2).execute(input, COST).getResultCode());

        checkRemoveHeadOfListWithHeadAndNextOnly(trs, contract, acct, acct2);

        // Test using depositFor.
        acct = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(2)));
        acct2 = getNewExistentAccount(BigInteger.ZERO);
        contract = createTRScontract(acct, false, false, 1, BigInteger.ZERO, 0);

        trs = newTRSuseContract(acct);
        input = getDepositForInput(contract, acct, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        input = getDepositForInput(contract, acct2, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        checkRemoveHeadOfListWithHeadAndNextOnly(trs, contract, acct, acct2);
    }

    @Test
    public void testRemoveHeadOfLargerList() {
        // Test using deposit.
        int listSize = 10;
        AionAddress owner = getNewExistentAccount(BigInteger.ONE);
        AionAddress contract =
                getContractMultipleDepositors(listSize, owner, false, true, 1, BigInteger.ZERO, 0);

        checkRemoveHeadOfLargerList(contract, owner, listSize);

        // Test using depositFor.
        owner = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(listSize)));
        contract =
                getContractMultipleDepositorsUsingDepositFor(
                        listSize, owner, false, 1, BigInteger.ZERO, 0);

        checkRemoveHeadOfLargerList(contract, owner, listSize);
    }

    @Test
    public void testRemoveTailOfSizeTwoList() {
        // Test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);

        byte[] input = getDepositInput(contract, DEFAULT_BALANCE);
        TRSuseContract trs = newTRSuseContract(acct);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        assertEquals(
                FastVmResultCode.SUCCESS, newTRSuseContract(acct2).execute(input, COST).getResultCode());

        checkRemoveTailOfSizeTwoList(trs, contract, acct, acct2);

        // Test using depositFor.
        acct = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.TWO));
        acct2 = getNewExistentAccount(BigInteger.ZERO);
        contract = createTRScontract(acct, false, false, 1, BigInteger.ZERO, 0);

        trs = newTRSuseContract(acct);
        input = getDepositForInput(contract, acct, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        input = getDepositForInput(contract, acct2, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        checkRemoveTailOfSizeTwoList(trs, contract, acct, acct2);
    }

    @Test
    public void testRemoveTailOfLargerList() {
        // Test using deposit.
        int listSize = 10;
        AionAddress owner = getNewExistentAccount(BigInteger.ONE);
        AionAddress contract =
                getContractMultipleDepositors(listSize, owner, false, true, 1, BigInteger.ZERO, 0);

        checkRemoveTailOfLargerList(contract, owner, listSize);

        // Test using depositFor.
        owner = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(listSize)));
        contract =
                getContractMultipleDepositorsUsingDepositFor(
                        listSize, owner, false, 1, BigInteger.ZERO, 0);

        checkRemoveTailOfLargerList(contract, owner, listSize);
    }

    @Test
    public void testRemoveInteriorOfSizeThreeList() {
        // Test using deposit.
        AionAddress acct = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress acct2 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress acct3 = getNewExistentAccount(DEFAULT_BALANCE);
        AionAddress contract = createTRScontract(acct, false, true, 1, BigInteger.ZERO, 0);

        byte[] input = getDepositInput(contract, DEFAULT_BALANCE);
        TRSuseContract trs = newTRSuseContract(acct);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        assertEquals(
                FastVmResultCode.SUCCESS, newTRSuseContract(acct2).execute(input, COST).getResultCode());
        assertEquals(
                FastVmResultCode.SUCCESS, newTRSuseContract(acct3).execute(input, COST).getResultCode());

        checkRemoveInteriorOfSizeThreeList(trs, contract, acct, acct2, acct3);

        // Test using depositFor.
        acct = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(3)));
        acct2 = getNewExistentAccount(BigInteger.ZERO);
        acct3 = getNewExistentAccount(BigInteger.ZERO);
        contract = createTRScontract(acct, false, false, 1, BigInteger.ZERO, 0);

        trs = newTRSuseContract(acct);
        input = getDepositForInput(contract, acct, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        input = getDepositForInput(contract, acct2, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        input = getDepositForInput(contract, acct3, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        checkRemoveInteriorOfSizeThreeList(trs, contract, acct, acct2, acct3);
    }

    @Test
    public void testRemoveInteriorOfLargerList() {
        // Test using deposit.
        int listSize = 10;
        AionAddress owner = getNewExistentAccount(BigInteger.ONE);
        AionAddress contract =
                getContractMultipleDepositors(listSize, owner, false, true, 1, BigInteger.ZERO, 0);

        checkRemoveInteriorOfLargerList(contract, owner, listSize);

        // Test using depositFor.
        owner = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(listSize)));
        contract =
                getContractMultipleDepositorsUsingDepositFor(
                        listSize, owner, false, 1, BigInteger.ZERO, 0);

        checkRemoveInteriorOfLargerList(contract, owner, listSize);
    }

    @Test
    public void testMultipleListRemovals() {
        // Test using deposit.
        int listSize = 10;
        AionAddress owner = getNewExistentAccount(BigInteger.ONE);
        AionAddress contract =
                getContractMultipleDepositors(listSize, owner, false, true, 1, BigInteger.ZERO, 0);

        checkMultipleListRemovals(contract, owner, listSize);

        // Test using depositFor.
        owner = getNewExistentAccount(DEFAULT_BALANCE.multiply(BigInteger.valueOf(listSize)));
        contract =
                getContractMultipleDepositorsUsingDepositFor(
                        listSize, owner, false, 1, BigInteger.ZERO, 0);

        checkMultipleListRemovals(contract, owner, listSize);
    }

    // <---------------------------------------HELPERS BELOW--------------------------------------->

    private void checkLinkedListOneDepositor(
            AbstractTRS trs, AionAddress contract, AionAddress acct, byte[] input) {
        assertEquals(acct, getLinkedListHead(trs, contract));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct));

        // Test one depositor makes more than one deposit.
        trs.execute(input, COST);
        trs.execute(input, COST);
        assertEquals(acct, getLinkedListHead(trs, contract));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct));
    }

    // We expect a list with acct2 as head as such: null <- acct2 <-> acct -> null
    private void checkLinkedListTwoDepositors(
            AbstractTRS trs, AionAddress contract, AionAddress acct, AionAddress acct2) {

        assertEquals(acct2, getLinkedListHead(trs, contract));
        assertEquals(acct, getLinkedListNext(trs, contract, acct2));
        assertNull(getLinkedListPrev(trs, contract, acct2));
        assertEquals(acct2, getLinkedListPrev(trs, contract, acct));
        assertNull(getLinkedListNext(trs, contract, acct));
    }

    // Expect a list with acct3 as head as such: null <- acct3 <-> acct2 <-> acct4 <-> acct1 -> null
    private void checkLinkedListMultipleDepositors(
            AionAddress contract, AionAddress acct1, AionAddress acct2, AionAddress acct3, AionAddress acct4) {

        TRSuseContract trs = newTRSuseContract(acct1);
        assertEquals(acct3, getLinkedListHead(trs, contract));
        assertNull(getLinkedListPrev(trs, contract, acct3));
        assertEquals(acct2, getLinkedListNext(trs, contract, acct3));
        assertEquals(acct3, getLinkedListPrev(trs, contract, acct2));
        assertEquals(acct4, getLinkedListNext(trs, contract, acct2));
        assertEquals(acct2, getLinkedListPrev(trs, contract, acct4));
        assertEquals(acct1, getLinkedListNext(trs, contract, acct4));
        assertEquals(acct4, getLinkedListPrev(trs, contract, acct1));
        assertNull(getLinkedListNext(trs, contract, acct1));
    }

    private void checkRemoveHeadOfListWithHeadOnly(
            AbstractTRS trs, AionAddress contract, AionAddress acct, byte[] input) {

        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        assertEquals(acct, getLinkedListHead(trs, contract));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct));

        input = getRefundInput(contract, acct, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        assertFalse(accountIsValid(trs, contract, acct));
        assertNull(getLinkedListHead(trs, contract));
    }

    // Expects acct2 as head with:  null <- acct2 <-> acct -> null
    private void checkRemoveHeadOfListWithHeadAndNextOnly(
            AbstractTRS trs, AionAddress contract, AionAddress acct, AionAddress acct2) {

        assertEquals(acct2, getLinkedListHead(trs, contract));
        assertEquals(acct, getLinkedListNext(trs, contract, acct2));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertEquals(acct2, getLinkedListPrev(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct2));

        // We remove acct2, the head. Should have:      null <- acct -> null
        byte[] input = getRefundInput(contract, acct2, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        assertFalse(accountIsValid(trs, contract, acct2));
        assertEquals(acct, getLinkedListHead(trs, contract));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct));
    }

    private void checkRemoveHeadOfLargerList(AionAddress contract, AionAddress owner, int listSize) {
        // We have a linked list with 10 depositors. Remove the head.
        TRSuseContract trs = newTRSuseContract(owner);
        AionAddress head = getLinkedListHead(trs, contract);
        AionAddress next = getLinkedListNext(trs, contract, head);
        assertNull(getLinkedListPrev(trs, contract, head));
        assertEquals(head, getLinkedListPrev(trs, contract, next));
        byte[] input = getRefundInput(contract, head, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        // We verify next is the new head, its prev is null and that we can advance 8 times before
        // hitting the end of the list.
        assertEquals(next, getLinkedListHead(trs, contract));
        assertNull(getLinkedListPrev(trs, contract, next));

        // We also make sure each address in the list is unique.
        Set<AionAddress> addressesInList = new HashSet<>();
        for (int i = 0; i < listSize - 1; i++) {
            if (i == listSize - 2) {
                assertNull(getLinkedListNext(trs, contract, next));
            } else {
                next = getLinkedListNext(trs, contract, next);
                assertNotNull(next);
                assertFalse(addressesInList.contains(next));
                addressesInList.add(next);
            }
        }
    }

    // Expects acct2 as head with:  null <- acct2 <-> acct -> null
    private void checkRemoveTailOfSizeTwoList(
            AbstractTRS trs, AionAddress contract, AionAddress acct, AionAddress acct2) {

        assertEquals(acct2, getLinkedListHead(trs, contract));
        assertEquals(acct, getLinkedListNext(trs, contract, acct2));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertEquals(acct2, getLinkedListPrev(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct2));

        // We remove acct, the tail. Should have:      null <- acct2 -> null
        byte[] input = getRefundInput(contract, acct, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        assertFalse(accountIsValid(trs, contract, acct));
        assertEquals(acct2, getLinkedListHead(trs, contract));
        assertNull(getLinkedListNext(trs, contract, acct2));
        assertNull(getLinkedListPrev(trs, contract, acct2));
    }

    private void checkRemoveTailOfLargerList(AionAddress contract, AionAddress owner, int listSize) {
        // We have a linked list with 10 depositors. First find the tail. Ensure each address is
        // unique too.
        TRSuseContract trs = newTRSuseContract(owner);
        AionAddress next = getLinkedListHead(trs, contract);
        AionAddress head = new AionAddress(next.toBytes());
        Set<AionAddress> addressesInList = new HashSet<>();
        for (int i = 0; i < listSize; i++) {
            if (i == listSize - 1) {
                assertNull(getLinkedListNext(trs, contract, next));
            } else {
                next = getLinkedListNext(trs, contract, next);
                assertNotNull(next);
                assertFalse(addressesInList.contains(next));
                addressesInList.add(next);
            }
        }

        // Now next should be the tail. Remove it. Iterate over list again.
        byte[] input = getRefundInput(contract, next, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        assertFalse(accountIsValid(trs, contract, next));

        assertEquals(head, getLinkedListHead(trs, contract));
        for (int i = 0; i < listSize - 1; i++) {
            if (i == listSize - 2) {
                assertNull(getLinkedListNext(trs, contract, head));
            } else {
                head = getLinkedListNext(trs, contract, head);
                assertNotNull(head);
                assertTrue(addressesInList.contains(head));
                assertNotEquals(next, head);
            }
        }
    }

    // Expects acct3 as head with: null <- acct3 <-> acct2 <-> acct -> null
    private void checkRemoveInteriorOfSizeThreeList(
            AbstractTRS trs, AionAddress contract, AionAddress acct, AionAddress acct2, AionAddress acct3) {

        assertEquals(acct3, getLinkedListHead(trs, contract));
        assertEquals(acct2, getLinkedListNext(trs, contract, acct3));
        assertEquals(acct, getLinkedListNext(trs, contract, acct2));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertEquals(acct2, getLinkedListPrev(trs, contract, acct));
        assertEquals(acct3, getLinkedListPrev(trs, contract, acct2));
        assertNull(getLinkedListPrev(trs, contract, acct3));

        // We remove acct2. Should have:      null <- acct3 <-> acct -> null    with acct3 as head.
        byte[] input = getRefundInput(contract, acct2, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());

        assertFalse(accountIsValid(trs, contract, acct2));
        assertEquals(acct3, getLinkedListHead(trs, contract));
        assertEquals(acct, getLinkedListNext(trs, contract, acct3));
        assertNull(getLinkedListNext(trs, contract, acct));
        assertEquals(acct3, getLinkedListPrev(trs, contract, acct));
        assertNull(getLinkedListPrev(trs, contract, acct3));
    }

    private void checkRemoveInteriorOfLargerList(AionAddress contract, AionAddress owner, int listSize) {
        // We have a linked list with 10 depositors. Grab the 5th in line. Ensure each address is
        // unique too.
        TRSuseContract trs = newTRSuseContract(owner);
        AionAddress next = getLinkedListHead(trs, contract);
        AionAddress head = new AionAddress(next.toBytes());
        AionAddress mid = null;
        Set<AionAddress> addressesInList = new HashSet<>();
        for (int i = 0; i < listSize; i++) {
            if (i == listSize - 1) {
                assertNull(getLinkedListNext(trs, contract, next));
            } else if (i == 4) {
                next = getLinkedListNext(trs, contract, next);
                assertNotNull(next);
                assertFalse(addressesInList.contains(next));
                addressesInList.add(next);
                mid = new AionAddress(next.toBytes());
            } else {
                next = getLinkedListNext(trs, contract, next);
                assertNotNull(next);
                assertFalse(addressesInList.contains(next));
                addressesInList.add(next);
            }
        }

        // Remove mid. Iterate over list again.
        byte[] input = getRefundInput(contract, mid, DEFAULT_BALANCE);
        assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
        assertFalse(accountIsValid(trs, contract, mid));

        assertEquals(head, getLinkedListHead(trs, contract));
        for (int i = 0; i < listSize - 1; i++) {
            if (i == listSize - 2) {
                assertNull(getLinkedListNext(trs, contract, head));
            } else {
                head = getLinkedListNext(trs, contract, head);
                assertNotNull(head);
                assertTrue(addressesInList.contains(head));
                assertNotEquals(mid, head);
            }
        }
    }

    private void checkMultipleListRemovals(AionAddress contract, AionAddress owner, int listSize) {
        // We have a linked list with 10 depositors. Ensure each address is unique. Grab every other
        // address to remove.
        TRSuseContract trs = newTRSuseContract(owner);
        AionAddress next = getLinkedListHead(trs, contract);
        Set<AionAddress> removals = new HashSet<>();
        Set<AionAddress> addressesInList = new HashSet<>();
        for (int i = 0; i < listSize; i++) {
            if (i == listSize - 1) {
                assertNull(getLinkedListNext(trs, contract, next));
            } else {
                next = getLinkedListNext(trs, contract, next);
                assertNotNull(next);
                assertFalse(addressesInList.contains(next));
                addressesInList.add(next);
                if (i % 2 == 0) {
                    removals.add(next);
                }
            }
        }

        // Remove all accts in removals. Iterate over list again.
        for (AionAddress rm : removals) {
            byte[] input = getRefundInput(contract, rm, DEFAULT_BALANCE);
            assertEquals(FastVmResultCode.SUCCESS, trs.execute(input, COST).getResultCode());
            assertFalse(accountIsValid(trs, contract, rm));
        }

        // Note: may give +/-1 errors if listSize is not divisible by 2.
        AionAddress head = getLinkedListHead(trs, contract);
        assertFalse(removals.contains(head));
        for (int i = 0; i < listSize / 2; i++) {
            if (i == (listSize / 2) - 1) {
                assertNull(getLinkedListNext(trs, contract, head));
            } else {
                head = getLinkedListNext(trs, contract, head);
                assertNotNull(head);
                assertTrue(addressesInList.contains(head));
                assertFalse(removals.contains(head));
            }
        }
    }
}
