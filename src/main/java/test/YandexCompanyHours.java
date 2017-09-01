package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexCompanyHours implements Serializable {
    @Id
    @OneToOne
    @JoinColumn(name = "id")
    private YandexCompany company;
    private String text;

    public YandexCompanyHours() {
    }

    public YandexCompanyHours(YandexCompany company, String text) {
        this.company = company;
        this.text = text;
    }

    public YandexCompany getCompany() {
        return company;
    }

    public void setCompany(YandexCompany company) {
        this.company = company;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
