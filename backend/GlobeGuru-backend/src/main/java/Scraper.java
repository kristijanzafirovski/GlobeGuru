import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class Scraper extends Thread {
    private List<String> urls;
    private String destination;
    private String departureDate;
    private int numberOfPeople;
    private ConcurrentLinkedQueue<Option> optionsQueue;
    private CountDownLatch latch;

    public Scraper(String destination, String departureDate, int numberOfPeople) {
        urls = new ArrayList<>();
        this.optionsQueue = new ConcurrentLinkedQueue<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(new File("src/main/java/URLsJSON.json"));
            JsonNode urlNode = root.get("agencyurls");
            if (urlNode.isArray()) {
                Iterator<JsonNode> elements = urlNode.elements();
                while (elements.hasNext()) {
                    JsonNode next = elements.next();
                    urls.add(next.asText());
                }
            }
            System.out.println("Loaded " + urls.size() + " urls");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.destination = destination;
        this.departureDate = departureDate;
        this.numberOfPeople = numberOfPeople;
        this.latch = new CountDownLatch(urls.size());
    }

    @Override
    public void run() {
        System.out.println("Scraper has started ");
        for (String url : urls) {
            new ScraperThread(url, destination, departureDate, numberOfPeople, optionsQueue, latch).start();
        }
    }
    public List<Option> getOptions() {
        try {
            latch.await();  // Wait for all threads to finish
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(optionsQueue);
    }
}
