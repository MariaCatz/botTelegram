package test;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class UserCompanyMessage {
    @Id
    private Long user_id;
    @ManyToOne
    private YandexCompany company;

    public Long getUserId() {
        return user_id;
    }

    public void setUserId(Long userId) {
        this.user_id = userId;
    }

    public YandexCompany getCompany() {
        return company;
    }

    public void setCompany(YandexCompany company) {
        this.company = company;
    }
}
