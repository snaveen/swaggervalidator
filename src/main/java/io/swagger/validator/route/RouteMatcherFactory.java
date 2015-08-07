package io.swagger.validator.route;


/**
 * RouteMatcherFactory
 *
 */
public final class RouteMatcherFactory {
    /**
     * The logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RouteMatcherFactory.class);

    private static SimpleRouteMatcher routeMatcher = null;

    private RouteMatcherFactory() {
    }

    public static synchronized SimpleRouteMatcher get() {
        if (routeMatcher == null) {
            LOG.debug("creates RouteMatcher");
            routeMatcher = new SimpleRouteMatcher();
        }
        return routeMatcher;
    }

}
