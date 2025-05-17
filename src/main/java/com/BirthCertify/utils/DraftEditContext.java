package com.birthcertify.utils;

import com.birthcertify.models.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton context holder for temporarily storing a draft being edited.
 * Used for sharing the selected application between views.
 */
public class DraftEditContext {
    private static final Logger logger = LoggerFactory.getLogger(DraftEditContext.class); // Logger initialization
    private static DraftEditContext instance;
    private Application activeDraft;

    private DraftEditContext() {}

    public static DraftEditContext getInstance() {
        if (instance == null) {
            instance = new DraftEditContext();
            logger.info("DraftEditContext instance created.");
        }
        return instance;
    }

    public Application getActiveDraft() {
        if (activeDraft != null) {
            logger.info("Retrieved active draft.");
        } else {
            logger.warn("Attempted to retrieve active draft, but no draft is set.");
        }
        return activeDraft;
    }

    public void setActiveDraft(Application draft) {
        this.activeDraft = draft;
        if (draft != null) {
            logger.info("Active draft set with ID: {}", draft.getApplicationId());
        } else {
            logger.info("Active draft cleared.");
        }
    }

    public void clear() {
        this.activeDraft = null;
        logger.info("Active draft cleared.");
    }

    public boolean hasDraft() {
        boolean hasDraft = activeDraft != null;
        if (hasDraft) {
            logger.info("Draft exists.");
        } else {
            logger.warn("No draft found.");
        }
        return hasDraft;
    }
}
