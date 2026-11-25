package com.detector.manager;

import com.detector.model.SocialMediaAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manages in-memory storage, retrieval, and indexing of social media accounts.
 * Demonstrates the Java Collections Framework using:
 * - ArrayList for ordered storage and indexing.
 * - HashMap for fast O(1) lookups by accountId.
 * - HashSet for ensuring unique account IDs.
 */
public class AccountManager {
    // Ordered storage for sequential listing and stable indexing
    private final List<SocialMediaAccount> accountList;

    // Fast O(1) key-value lookup by accountId
    private final Map<String, SocialMediaAccount> accountMap;

    // Set for uniqueness tracking and fast duplicate detection
    private final Set<String> registeredIds;

    public AccountManager() {
        this.accountList = new ArrayList<>();
        this.accountMap = new HashMap<>();
        this.registeredIds = new HashSet<>();
    }

    /**
     * Adds an account to management. Prevents duplicate account IDs.
     *
     * @param account account to register
     * @return true if added successfully, false if account is null or duplicate ID exists
     */
    public synchronized boolean addAccount(SocialMediaAccount account) {
        if (account == null) {
            return false;
        }
        String id = account.getAccountId();
        if (id == null || registeredIds.contains(id)) {
            return false; // Reject duplicate or invalid ID
        }

        accountList.add(account);
        accountMap.put(id, account);
        registeredIds.add(id);
        return true;
    }

    /**
     * Retrieves an unmodifiable view of all registered accounts.
     *
     * @return List of all accounts
     */
    public synchronized List<SocialMediaAccount> getAllAccounts() {
        return Collections.unmodifiableList(new ArrayList<>(accountList));
    }

    /**
     * Finds an account by its unique identifier in O(1) time complexity.
     *
     * @param accountId unique account ID to locate
     * @return Optional containing the account if found, or empty Optional
     */
    public synchronized Optional<SocialMediaAccount> findAccountById(String accountId) {
        if (accountId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountMap.get(accountId.trim()));
    }

    /**
     * Removes an account from management by account ID.
     *
     * @param accountId ID of account to remove
     * @return true if removed, false if not found
     */
    public synchronized boolean removeAccount(String accountId) {
        if (accountId == null || !registeredIds.contains(accountId.trim())) {
            return false;
        }
        String trimmedId = accountId.trim();
        SocialMediaAccount removed = accountMap.remove(trimmedId);
        registeredIds.remove(trimmedId);
        accountList.remove(removed);
        return true;
    }

    /**
     * Checks if an account ID is already registered.
     *
     * @param accountId account ID to check
     * @return true if ID exists, false otherwise
     */
    public synchronized boolean accountExists(String accountId) {
        if (accountId == null) {
            return false;
        }
        return registeredIds.contains(accountId.trim());
    }

    /**
     * Returns the total count of registered accounts.
     *
     * @return count of accounts
     */
    public synchronized int getAccountCount() {
        return accountList.size();
    }

    /**
     * Clears all registered accounts.
     */
    public synchronized void clear() {
        accountList.clear();
        accountMap.clear();
        registeredIds.clear();
    }
}
