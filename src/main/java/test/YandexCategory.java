package test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class YandexCategory implements Serializable {
    @ManyToOne
    @Id
    private YandexCompany company;
    @Id
    private String name;

    @JsonProperty("class")
    @Id
    @Column(nullable = true)
    private String categoryClass;

    public YandexCategory(){}
    public YandexCategory(YandexCompany company, String name, String categoryClass) {
        this.company = company;
        this.name = name;
        this.categoryClass = categoryClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryClass() {
        return categoryClass;
    }

    public void setCategoryClass(String categoryClass) {
        this.categoryClass = categoryClass;
    }
    public YandexCompany getCompany() {
        return company;
    }

    public void setCompany(YandexCompany company) {
        this.company = company;
    }
}
