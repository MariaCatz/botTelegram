package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
/*@Table(uniqueConstraints =
@UniqueConstraint(name="UniqueCompany",columnNames = {"name", "address"}))*/
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexCompany {
    @Id
    private String id;
    private String name;
    private String address;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<YandexCategory> Categories = new ArrayList<YandexCategory>();
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "company")
    private YandexCompanyHours Hours;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "company")
    private YandexCompanyRating YandexRating;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<UserCompanyRating> ratings = new ArrayList<UserCompanyRating>();
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<UserCompanyMessage> messages = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "company")
    private CompanyRating Rating;

    public CompanyRating getRating() {
        return Rating;
    }

    public void setRating(CompanyRating rating) {
        Rating = rating;
    }


    public List<UserCompanyRating> getRatings() {
        return ratings;
    }

    public void setRatings(List<UserCompanyRating> ratings) {
        this.ratings = ratings;
    }

    public List<UserCompanyMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<UserCompanyMessage> messages) {
        this.messages = messages;
    }

    public YandexCompanyRating getYandexRating() {
        return YandexRating;
    }

    public void setYandexRating(YandexCompanyRating rating) {
        this.YandexRating = rating;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<YandexCategory> getCategories() {
        return Categories;
    }

    public void setCategories(List<YandexCategory> categories) {
        Categories = categories;
    }

    public YandexCompanyHours getHours() {
        return Hours;
    }

    public void setHours(YandexCompanyHours hours) {
        Hours = hours;
    }

    public void addCategory(YandexCategory category) {
        Categories.add(category);
    }
}
