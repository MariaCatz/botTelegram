package test;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class YandexCompanyRating implements Serializable
{
    @Id
    @OneToOne
    @JoinColumn(name = "id")
    private YandexCompany company;
    @Column(nullable = true)
    private Double rating;

    public YandexCompany getCompany() {
        return company;
    }

    public void setCompany(YandexCompany company) {
        this.company = company;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public YandexCompanyRating()
    {}
}
