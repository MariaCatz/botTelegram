package test;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class UserCompanyRating implements Serializable {
    @Id
    private Long user_id;
    @ManyToOne
    @Id
    private YandexCompany company;
    @Column(nullable = true)
    private double rating;

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long userId) {
        this.user_id = userId;
    }

    public YandexCompany getCompany() {
        return company;
    }

    public void setCompany(YandexCompany companyId) {
        this.company = companyId;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }
    public UserCompanyRating(){}
}
