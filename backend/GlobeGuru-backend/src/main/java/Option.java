public class Option {
    private String hotelName;
    private String country;
    private String price;
    private String link; // Ensure to add a field for link
    private String imgSrc;
    // Constructor
    public Option(){}

    public boolean isEmpty(){
        return (hotelName == null || country == null || price == null || link == null || imgSrc == null);
    }
    // Getters and setters (ensure you have these methods)
    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setImgSrc(String imgSrc) {
        this.imgSrc = imgSrc;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    // toString method (for debugging purposes)
    @Override
    public String toString() {
        return "Option{" +
                "hotelName='" + hotelName + '\'' +
                ", country='" + country + '\'' +
                ", price='" + price + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
