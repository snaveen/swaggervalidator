/**
 * 
 */
package io.swagger.validator.spark;

import spark.servlet.SparkApplication;
import static spark.Spark.*;
/**
 * @author naveens
 *
 */
public class App implements SparkApplication{
    public void init() {
        get("/hello", (req,resp)->{
         System.out.println("inside servlet");
            return "helloworld";   
        });
        get("/hello/:name", (req,resp)->{
            System.out.println("inside servlet "+req.params("name"));
               return "hello "+req.params("name");   
           });
        
    }
}
