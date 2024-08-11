package lu.kremi151.desk.api

sealed interface DrawErrorHandling {

    /**
     * Ignore error and proceed with rendering the next movables.
     * @param invalidate if set to true, the desk view to be re-rendered once again after the current iteration
     */
    class Ignore(val invalidate: Boolean = false): DrawErrorHandling

    /**
     * Retry drawing this movable.
     * @param attempts the total attempts
     * @param then if attempts are exceeded, the fallback handling to apply
     */
    class Retry(val attempts: Int = 3, val then: DrawErrorHandling = Ignore()): DrawErrorHandling

}
