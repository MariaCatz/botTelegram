package test;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Main {
    public static void main(String[] args) {
        SessionFactory factory = new Configuration().configure().buildSessionFactory();
        YandexDataWork dataWork=new YandexDataWork();
        dataWork.gettingYandexPlaces(factory);
        dataWork.gettingPlacesRating(factory);

        Bot bot=new Bot();
        bot.createUserGoogle(factory);
        bot.updateCompaniesRatings(factory);
        bot.run(factory);
    }
}
