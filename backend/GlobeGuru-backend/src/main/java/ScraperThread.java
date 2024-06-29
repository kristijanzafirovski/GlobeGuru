import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ScraperThread extends Thread {
    private String url;
    private String destination;
    private String departureDate;
    private int numberOfPeople;
    private ConcurrentLinkedQueue<Option> uniqueOptions;
    private CountDownLatch latch;

    public ScraperThread(String url, String destination, String departureDate, int numberOfPeople, ConcurrentLinkedQueue<Option> optionsQueue, CountDownLatch latch) {
        this.url = url;
        this.destination = destination;
        this.departureDate = departureDate;
        this.numberOfPeople = numberOfPeople;
        this.uniqueOptions = optionsQueue;
        this.latch = latch;
    }

    private void connectToWeb(String queryUrl) {
        // Selenium
        ChromeOptions options = new ChromeOptions();
        options.setBinary("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe"); // Path to Brave, remove for Chrome compatibility
        options.addArguments("--headless");  // Run in headless mode
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"); // User-Agent

        // chromeDriver
        System.setProperty("webdriver.chrome.driver", "C:\\drivers\\chromedriver.exe");
        System.setProperty("webdriver.http.factory", "jdk-http-client");
        WebDriver driver = new ChromeDriver(options);
        try {
            // Navigate to URL
            driver.get(queryUrl);
            Thread.sleep(10000); // Sleep to fetch all data

            // Get page source
            String pageSource = driver.getPageSource();
            System.out.println("Thread " + Thread.currentThread().getId() + " connected to " + queryUrl);

            // Get only options
            Document doc = Jsoup.parse(pageSource);
            Element parentDiv;
            Elements childDivs;
            switch (url) {
                case "https://www.fibula.com.mk/":
                    parentDiv = doc.selectFirst("div.flex.flex-col.gap-5");
                    if (parentDiv != null) {
                        childDivs = parentDiv.select("div");
                        for (Element div : childDivs) {
                            String data = div.html();
                            Option option = optionParser(data);
                            if (option != null) {
                                if (uniqueOptions.add(option)) {
                                    System.out.println("Parsed Option: " + option);
                                }
                            }
                        }
                    } else {
                        System.out.println("Parent div not found");
                    }
                    break;
                case "https://booking.escapetravel.mk/":
                    parentDiv = doc.selectFirst("div.container.pt-4.pt-md-6.scroll-into-view");
                    Element subParent;
                    System.out.println(parentDiv);
                    if(parentDiv != null) {
                         subParent = parentDiv.selectFirst("div.row");
                    }else{
                        System.out.println("Parent div not found");
                        break;
                    }

                    if (subParent != null) {
                        childDivs = subParent.select("div.col-md-3");

                        for (Element div : childDivs) {
                            String data = div.html();
                            Option option = optionParser(data);
                            if (option != null) {
                                if (uniqueOptions.add(option)) {
                                    System.out.println("Parsed option: " + option);
                                }
                            }
                        }
                    }else {
                        System.out.println("subparent div not found");
                    }
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            latch.countDown();
        }
    }

    private Option optionParser(String data) {
        Document doc = Jsoup.parse(data);
        Option created = new Option();

        switch (url) {
            case "https://www.fibula.com.mk/":
                created = parseFibula(doc);
                break;
            case "https://booking.escapetravel.mk/":
                created = parseEscapeTravel(doc);
                break;
            default:
                System.out.println("URL not recognized for parsing.");
                break;
        }

        if (created.isEmpty()) {
            return null;
        }

        return created;
    }

    private Option parseFibula(Document doc) {
        Option created = new Option();

        Element linkElement = doc.selectFirst("a[target='_blank']");
        created.setLink(linkElement != null ? url + linkElement.attr("href") : null);

        Element imgElement = doc.selectFirst("div.md\\:aspect-none img");
        created.setImgSrc(imgElement != null ? imgElement.attr("src") : null);

        Element hotelNameElement = doc.selectFirst("h5.text-md");
        created.setHotelName(hotelNameElement != null ? hotelNameElement.text() : null);

        Element countryElement = doc.selectFirst("small.text-navy");
        created.setCountry(countryElement != null ? countryElement.text() : null);

        Element priceElement = doc.selectFirst("small.line-through");
        String price = priceElement != null ? priceElement.text().replaceAll("[^\\d.]", "") : "0";
        created.setPrice(price);

        return created;
    }

    private Option parseEscapeTravel(Document doc) {
        Option created = new Option();

        // Extract link
        Element linkElement = doc.selectFirst("a[target='_blank']");
        created.setLink(linkElement != null ? linkElement.attr("href") : null);

        // Extract image source
        Element imgElement = doc.selectFirst("img.card-img-top");
        created.setImgSrc(imgElement != null ? imgElement.attr("src") : null);

        // Extract hotel name
        Element hotelNameElement = doc.selectFirst("h3.fw-bold.text-body.mb-2");
        created.setHotelName(hotelNameElement != null ? hotelNameElement.text() : null);

        // Extract country/location
        Element countryElement = doc.selectFirst("h5.fw-light.text-primary.mb-1");
        created.setCountry(countryElement != null ? countryElement.text() : null);

        // Extract price
        Element priceElement = doc.selectFirst("h4.fw-light.text-success.mb-0");
        String price = priceElement != null ? priceElement.text().replaceAll("[^\\d.]", "") : "0";
        created.setPrice(price);

        return created;
    }


    @Override
        public void run() {
            System.out.println("Thread started for url: " + url);
            StringBuilder builder = new StringBuilder();
            builder.append(url);
            String queryUrl;
            switch (url) {
                case "https://www.fibula.com.mk/":
                    builder.append("search?productType=2&"); // search for hotels
                    for (int i = 0; i < numberOfPeople; i++) { // add all passengers (default adults)
                        builder.append("passengers=1993-01-01&");
                    }
                    queryUrl = builder.toString();
                    System.out.println(queryUrl);
                    connectToWeb(queryUrl);
                    break;
                case "https://booking.escapetravel.mk/":
                    builder.append("destinations?Category=&Search=&DateFrom=");
                    builder.append(departureDate);
                    builder.append("&Rooms=1&Adults=");
                    builder.append(numberOfPeople);
                    queryUrl = builder.toString();
                    System.out.println(queryUrl);
                    connectToWeb(queryUrl);
                    break;
                default:
                    System.out.println("Not available for current url");
                    latch.countDown();
                    break;
            }
        }
    }
