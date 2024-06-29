import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.openqa.selenium.internal.Debug;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class ScraperServer {

    public static void main(String[] args) throws IOException {
        // Start HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new FrontendHandler());
        server.createContext("/submit", new FormHandler());
        server.start();

        System.out.println("Server started on port 8000");
    }

    static class FrontendHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String uri = exchange.getRequestURI().toString();
            File file = new File("frontend" + uri);
            if (!file.exists() || !file.isFile()) {
                file = new File("frontend/index.html"); // Default to index.html
            }

            exchange.sendResponseHeaders(200, file.length());
            OutputStream outputStream = exchange.getResponseBody();
            Files.copy(file.toPath(), outputStream);
            outputStream.close();
        }
    }

    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Read form data from request body
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                System.out.println("Form data: " + requestBody);

                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> formData = mapper.readValue(requestBody, Map.class);
                String destination = formData.get("destination");
                String departureDate = formData.get("departureDate");
                int numberOfPeople = Integer.parseInt(formData.get("numberOfPeople"));

                // Start Scraper thread with form data
                Scraper scraper = new Scraper(destination, departureDate, numberOfPeople);
                scraper.start();

                // Wait for the scraper threads to finish
                try {
                    scraper.join(); // Ensure the scraper thread completes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                List<Option> options = scraper.getOptions();
                //System.out.println("Scraper finished with options: " + options);

                // Respond with JSON data
                String optionsJson = mapper.writeValueAsString(options);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, optionsJson.getBytes().length);
                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(optionsJson.getBytes());
                outputStream.close();
            } else {
                // Method not allowed
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
            }
        }
    }

}
