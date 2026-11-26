package com.detector.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry managing detection rules available within the application.
 * Decouples rule catalog management from detection execution.
 */
public class RuleRegistry {
    private final Map<String, DetectionRule> registeredRules;

    public RuleRegistry() {
        this.registeredRules = new LinkedHashMap<>();
    }

    /**
     * Registers a detection rule into the catalog.
     *
     * @param rule rule to register
     * @return true if successfully registered, false if null or duplicate name exists
     */
    public boolean register(DetectionRule rule) {
        if (rule == null || rule.getRuleName() == null) {
            return false;
        }
        String key = rule.getRuleName().trim().toLowerCase();
        if (registeredRules.containsKey(key)) {
            return false;
        }
        registeredRules.put(key, rule);
        return true;
    }

    /**
     * Finds a rule by its rule name (case-insensitive).
     *
     * @param ruleName name of rule to find
     * @return Optional containing rule if found
     */
    public Optional<DetectionRule> findByName(String ruleName) {
        if (ruleName == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(registeredRules.get(ruleName.trim().toLowerCase()));
    }

    /**
     * Unregisters a rule by its rule name.
     *
     * @param ruleName name of rule to unregister
     * @return true if removed, false otherwise
     */
    public boolean unregister(String ruleName) {
        if (ruleName == null) {
            return false;
        }
        return registeredRules.remove(ruleName.trim().toLowerCase()) != null;
    }

    /**
     * Returns an unmodifiable list of all registered rules in insertion order.
     *
     * @return list of detection rules
     */
    public List<DetectionRule> getAllRules() {
        return Collections.unmodifiableList(new ArrayList<>(registeredRules.values()));
    }

    /**
     * Returns the number of registered rules.
     *
     * @return rule count
     */
    public int size() {
        return registeredRules.size();
    }

    /**
     * Clears all registered rules.
     */
    public void clear() {
        registeredRules.clear();
    }
}
