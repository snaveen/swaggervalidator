package io.swagger.validator;

import io.swagger.models.Model;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import io.swagger.util.ModelDeserializer;
import io.swagger.validator.route.RouteEntry;
import io.swagger.validator.route.RouteMatcherFactory;
import io.swagger.validator.route.SimpleRouteMatcher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class ValidatorFilter implements Filter {


    private static final String HTTP_METHOD_OVERRIDE_HEADER = "X-HTTP-Method-Override";
    private static final String ACCEPT_TYPE_REQUEST_MIME_HEADER = "Accept";
    protected FilterConfig config;
    private Swagger swagger;
    SimpleRouteMatcher matcher;

    public void init(FilterConfig config) throws ServletException {
        this.config = config;
        try {
            swagger = new SwaggerParser().read("C:\\Users\\naveens\\Downloads\\swagger2.json");
            Map<String, String> definitionsJsonSchemaMap = new HashMap<String, String>();
            
            
            //Init routes here
            matcher=RouteMatcherFactory.get();
            matcher.addRoute("get", "/swaggervalidator/hello", "application/json");
            matcher.addRoute("get", "/swaggervalidator/hello/{name}", "application/json");

            for (Entry<String, Model> entry : swagger.getDefinitions().entrySet()) {
                definitionsJsonSchemaMap.put(entry.getKey(),
                        createJsonObjectMapper().writeValueAsString(entry.getValue()));
            }
        } catch (Exception ex) {
            System.err.println("Error " + ex);
        }

    }

    /**
     * Create custom Json Object mapper, that support the serialization and deserialization of the
     *
     * @return
     */
    private static ObjectMapper createJsonObjectMapper() {

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Model.class, new ModelDeserializer());

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;

    }


    private static class ByteArrayServletStream extends ServletOutputStream {
        ByteArrayOutputStream baos;

        ByteArrayServletStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        public void write(int param) throws IOException {
            baos.write(param);
        }
    }

    private static class ByteArrayPrintWriter {

        private ByteArrayOutputStream baos = new ByteArrayOutputStream();

        private PrintWriter pw = new PrintWriter(baos);

        private ServletOutputStream sos = new ByteArrayServletStream(baos);

        public PrintWriter getWriter() {
            return pw;
        }

        public ServletOutputStream getStream() {
            return sos;
        }

        byte[] toByteArray() {
            return baos.toByteArray();
        }
    }

    public class CharResponseWrapper extends HttpServletResponseWrapper {
        private ByteArrayPrintWriter output;
        private boolean usingWriter;

        public CharResponseWrapper(HttpServletResponse response) {
            super(response);
            usingWriter = false;
            output = new ByteArrayPrintWriter();
        }

        public byte[] getByteArray() {
            return output.toByteArray();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            // will error out, if in use
            if (usingWriter) {
                super.getOutputStream();
            }
            usingWriter = true;
            return output.getStream();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            // will error out, if in use
            if (usingWriter) {
                super.getWriter();
            }
            usingWriter = true;
            return output.getWriter();
        }

        public String toString() {
            return output.toString();
        }
    }


    public void destroy() {
        config = null;
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        
        RouteEntry routeEntry = getRouteEntry(request);
        
        System.out.println("Matching route entry: "+routeEntry);
        
        System.out.println("inside filter");
        CharResponseWrapper wrappedResponse = new CharResponseWrapper((HttpServletResponse) response);

        chain.doFilter(request, wrappedResponse);
        System.out.println("After chain");
        byte[] bytes = wrappedResponse.getByteArray();

        // if (wrappedResponse.getContentType().contains("text/html")) {
        String out = new String(bytes);
        System.out.println(out);
        response.getOutputStream().write(out.getBytes());
        // }
        // else {
        // response.getOutputStream().write(bytes);
        // }
    }

    /**
     * @param request
     * @return
     */
    public RouteEntry getRouteEntry(ServletRequest request) {
        //Find matching route
        HttpServletRequest httpRequest=(HttpServletRequest) request;
        String method = httpRequest.getHeader(HTTP_METHOD_OVERRIDE_HEADER);
        if (method == null) {
            method = httpRequest.getMethod();
        }
        String httpMethodStr = method.toLowerCase(); // NOSONAR
        String uri = httpRequest.getPathInfo(); // NOSONAR
        if(uri==null) {
            uri=httpRequest.getRequestURI();
        }
        String acceptType = httpRequest.getHeader(ACCEPT_TYPE_REQUEST_MIME_HEADER);
        
        RouteEntry routeEntry=matcher.findTargetsForRequestedRoute(httpMethodStr, uri,acceptType);
        return routeEntry;
    }
}
