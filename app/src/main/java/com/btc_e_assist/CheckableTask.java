package com.btc_e_assist;

public interface CheckableTask {
    /**
     * Check the task
     *
     * @return true if the result is positive, false otherwise 
     */
    boolean check();

    /**
     * Do this if result of check is positive
     *
     * @return true if this action has been performed completely, false otherwise 
     */
    boolean doIfPositive();

    /**
     * Do this if result of check is negative
     *
     * @return true if this action has been performed completely, false otherwise 
     */
    boolean doIfNegative();

    /**
     * Delete this task or not from service task list if result of check is positive
     *
     * @return true if this task must be deleted, false otherwise 
     */
    boolean isDeleteIfPositive();

    /**
     * Delete this task or not from service task list if result of check is negative
     *
     * @return true if this task must be deleted, false otherwise 
     */
    boolean isDeleteIfNegative();

    /**
     * Do this, if positive action has been performed not completely
     */
    void doIfPositiveActionFail();

    /**
     * Do this, if negative action has been performed not completely
     */

    void doIfNegativeActionFail();

    /**
     * Get id of this task used inside Database.
     *
     * @return task id from DB
     */
    long getId();
}
