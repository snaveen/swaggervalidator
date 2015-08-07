/**
 * 
 */
package io.swagger.validator.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author naveens
 *
 */
public class Utils {
    
  
 
        public static final String ALL_PATHS = "+/*paths";

  
        public static List<String> convertRouteToList(String route) {
            String[] pathArray = route.split("/");
            List<String> path = new ArrayList<String>();
            for (String p : pathArray) {
                if (p.length() > 0) {
                    path.add(p);
                }
            }
            return path;
        }

        public static boolean isParam(String routePart) {
            return routePart.startsWith(":");
        }

        public static boolean isSplat(String routePart) {
            return routePart.equals("*");
        }

    }
