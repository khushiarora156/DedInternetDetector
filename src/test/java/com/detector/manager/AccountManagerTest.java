package com.detector.manager;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AccountManager Collections & Indexing Unit Tests")
public class AccountManagerTest {

    private AccountManager manager;
    private SocialMediaAccount acc1;
    private SocialMediaAccount acc2;

    @BeforeEach
    void setUp() {
        manager = new AccountManager();
        AccountActivity act = new AccountActivity(10, 2, 0, 5, 1);
        acc1 = new SocialMediaAccount("ACC_001", "alice", 500, 100, 50, 200, act);
        acc2 = new SocialMediaAccount("ACC_002", "bob", 800, 200, 75, 300, act);
    }

    @Test
    @DisplayName("Should successfully add accounts and increment count")
    void testAddAccountSuccess() {
        assertTrue(manager.addAccount(acc1));
        assertTrue(manager.addAccount(acc2));
        assertEquals(2, manager.getAccountCount());
        assertTrue(manager.accountExists("ACC_001"));
        assertTrue(manager.accountExists("ACC_002"));
    }

    @Test
    @DisplayName("Should reject duplicate account IDs")
    void testPreventDuplicateAccountIds() {
        assertTrue(manager.addAccount(acc1));
        assertEquals(1, manager.getAccountCount());

        // Attempt to insert another account with the same ID
        SocialMediaAccount duplicate = new SocialMediaAccount("ACC_001", "imposter", 10, 20, 5, 2, new AccountActivity());
        assertFalse(manager.addAccount(duplicate));
        assertEquals(1, manager.getAccountCount());
        assertEquals("alice", manager.findAccountById("ACC_001").get().getUsername());
    }

    @Test
    @DisplayName("Should retrieve account by ID in O(1) via HashMap")
    void testFindAccountById() {
        manager.addAccount(acc1);
        manager.addAccount(acc2);

        Optional<SocialMediaAccount> found = manager.findAccountById("ACC_002");
        assertTrue(found.isPresent());
        assertEquals("bob", found.get().getUsername());

        Optional<SocialMediaAccount> missing = manager.findAccountById("ACC_999");
        assertTrue(missing.isEmpty());
    }

    @Test
    @DisplayName("Should remove account correctly and update all collections")
    void testRemoveAccount() {
        manager.addAccount(acc1);
        manager.addAccount(acc2);

        assertTrue(manager.removeAccount("ACC_001"));
        assertEquals(1, manager.getAccountCount());
        assertFalse(manager.accountExists("ACC_001"));
        assertTrue(manager.findAccountById("ACC_001").isEmpty());
        assertFalse(manager.removeAccount("ACC_001")); // Subsequent removal fails
    }

    @Test
    @DisplayName("Should return unmodifiable list from getAllAccounts")
    void testGetAllAccountsUnmodifiable() {
        manager.addAccount(acc1);
        assertThrows(UnsupportedOperationException.class, () -> manager.getAllAccounts().clear());
    }
}
