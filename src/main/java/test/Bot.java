package test;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.TelegramBotAdapter;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Random;

public class Bot {
    //Обновление рейтинга одной компании (среднее по UserCompanyRating)
    private void updateCompanyRating(YandexCompany company, Session session) {
        //Получение среднего рейтинга компании
        Double rating = (Double) session.createQuery("select avg(r.rating) from UserCompanyRating r" +
                " where r.company=:cmp").setParameter("cmp", company).getSingleResult();
        //Обновление рейтинга компании
        try {
            session.beginTransaction();
            session.createQuery("delete from CompanyRating r where r.company=:cmp").
                    setParameter("cmp", company).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        CompanyRating rt = new CompanyRating();
        rt.setCompany(company);
        rt.setRating(rating);
        session.beginTransaction();
        session.save(rt);
        session.getTransaction().commit();

    }

    //Обновление рейтинга всех компаний
    public void updateCompaniesRatings(SessionFactory factory ) {
        Session session = factory.openSession();
        List<Object[]> list = session.createQuery("select r.company.id,avg(r.rating) from UserCompanyRating r" +
                " group by r.company.id").getResultList();

        session.beginTransaction();
        session.createQuery("delete from CompanyRating").executeUpdate();//не протестировано
        session.getTransaction().commit();

        for (int i = 0; i < list.size(); i++) {
            session.beginTransaction();

            session.createSQLQuery
                    ("insert into companyrating(id,rating) values(:compid,:ratg)")
                    .setParameter("compid", (String) list.get(i)[0]).
                    setParameter("ratg", (Double) list.get(i)[1]).executeUpdate();
            session.getTransaction().commit();
        }
        //System.out.println("q");
        session.close();
    }

    //Для того, чтобы у компаний был первоначальный рейтинг
    //воспользуемся рейтингом от Google
    public void createUserGoogle(SessionFactory factory) {
        Session session = factory.openSession();
        List<YandexCompany> GoogleRatings = session.createQuery(
                "select c from  YandexCompany c, YandexCompanyRating y " +
                        "  where y.company = c and y.rating is not null").getResultList();

        for (int i = 0; i < GoogleRatings.size(); i++) {
            session.beginTransaction();
            UserCompanyRating result = new UserCompanyRating();
            result.setCompany(GoogleRatings.get(i).getYandexRating().getCompany());
            result.setRating(GoogleRatings.get(i).getYandexRating().getRating());
            result.setUserId(new Long(-1));
            session.save(result);
            session.getTransaction().commit();
        }
        session.close();
    }

    private double rating = 4;

    //рандомное получение места с рейтингом выше заданного
    private String getRandomPlace(Session session) {
        List<YandexCompany> companies = session.createQuery
                ("select c from  YandexCompany c, CompanyRating r " +
                        "where r.company = c    and r.rating> :rating and r.rating is not null").setParameter("rating", rating)
                .getResultList();

        int random_id = new Random().nextInt(companies.size());
        String company = companies.get(random_id).getName()
                + "\nАдрес: " + companies.get(random_id).getAddress()
                + "\nРейтинг: " + companies.get(random_id).getRating().getRating()
                + "\nРежим работы:" + companies.get(random_id).getHours().getText();
        System.out.println(company);

        /*Transaction tr = session.beginTransaction();
        int n = session.createQuery("UPDATE UserCompanyMessage set company= :company where user_id= :id").
                setParameter("company", companies.get(random_id)).
                setParameter("id", user_id).executeUpdate();
        if (n == 0) {
            UserCompanyMessage mess = new UserCompanyMessage();
            mess.setUserId(user_id);
            mess.setCompany(companies.get(random_id));
            session.save(mess);
        }
        tr.commit();*/
        return company;
    }

    //оценивание компании пользователем
    private String rateLast(Long user_id, Session session, double rate) {
        String res;

        UserCompanyMessage message = session.get(UserCompanyMessage.class, user_id);

        session.clear();
        try {
            session.beginTransaction();
            session.createQuery("delete from UserCompanyRating cr where cr.user_id = :id " +
                    "and cr.company = :company ").setParameter("id", user_id).
                    setParameter("company", message.getCompany()).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception ex) {
            session.getTransaction().rollback();
            System.out.println(ex.getMessage());
        }

        UserCompanyRating rating = new UserCompanyRating();
        rating.setCompany(message.getCompany());
        rating.setUserId(user_id);
        rating.setRating(rate);
        Transaction tr = session.beginTransaction();
        session.save(rating);
        tr.commit();

        updateCompanyRating(message.getCompany(), session);

        res = "Спасибо за оценку!";
        return res;
    }

    //получение компании из сообщения пользователя
    private YandexCompany getPlaceFromMessage(String message, Session session) {
        session.clear();
        YandexCompany company;
        String name = message.split("\\n")[0];
        String address = message.split("\\n")[1].split("Адрес: ")[1];
        company = (YandexCompany) session.createQuery
                ("from YandexCompany where name=:name and address=:address")
                .setParameter("name", name).
                        setParameter("address", address).
                        getSingleResult();
        return company;
    }

    //сохранение пользователя и места, которое он оценит
    private void savePlace(YandexCompany company, Session session, Long user_id) {
        UserCompanyMessage mess = new UserCompanyMessage();
        mess.setCompany(company);
        try {
            session.beginTransaction();
            session.createQuery("delete from UserCompanyMessage where" +
                    " user_id=:id").setParameter
                    ("id", user_id).executeUpdate();
            session.getTransaction().commit();
        } catch (Exception ex) {
            session.getTransaction().rollback();
        }
        mess.setUserId(user_id);
        session.beginTransaction();
        session.save(mess);
        session.getTransaction().commit();
    }

    public void run(final SessionFactory factory) {
        final TelegramBot bot = TelegramBotAdapter.build("406197850:AAFVm6T2bIjPfJJFtmEYHzcaTOUfWI1jQsI");
        final Session session = factory.openSession();
        bot.setUpdatesListener(new UpdatesListener() {
            @Override
            public int process(List<Update> updates) {
                for (int i = 0; i < updates.size(); i++) {
                    Update lastUpdate = updates.get(i);
                    Keyboard keyboard = new ReplyKeyboardMarkup(
                            new KeyboardButton[]{
                                    new KeyboardButton("Оценить место"),
                                    new KeyboardButton("Другое место")
                            }
                    ).resizeKeyboard(true);
                    String messageToSend;
                    if (lastUpdate.message().text().compareTo("Оценить место") == 0) {
                        SendMessage request = new SendMessage(lastUpdate.message().chat().id(),
                                "1:Перешли сообщение с местом, которое хочешь оценить, боту. Жди дальнейших инструкций!").replyMarkup(new ReplyKeyboardRemove());
                        SendResponse sendResponse = bot.execute(request);

                    } else if (lastUpdate.message().text().matches("[0-5]")) {
                        messageToSend = rateLast(lastUpdate.message().from().id().longValue(), session,
                                Double.parseDouble(lastUpdate.message().text()));

                        SendMessage request = new SendMessage(lastUpdate.message().chat().id(), messageToSend).replyMarkup(keyboard);
                        SendResponse sendResponse = bot.execute(request);
                    } else if ((lastUpdate.message().text().compareTo("/start") == 0) ||
                            (lastUpdate.message().text().compareTo("Другое место") == 0)) {
                        messageToSend = getRandomPlace(session);
                        SendMessage request = new SendMessage(lastUpdate.message().chat().id(), messageToSend).replyMarkup(keyboard);
                        SendResponse sendResponse = bot.execute(request);
                    }
                    //Если пользователь прислал боту место, которое хочет оценить
                    else {
                        try {
                            session.clear();

                            YandexCompany company = getPlaceFromMessage(lastUpdate.message().text(), session);
                            //сохранить место для дальнейшего оценивания
                            savePlace(company, session, lastUpdate.message().from().id().longValue());

                            messageToSend = "2:Оцените место от 1 до 5";
                            SendMessage request = new SendMessage(lastUpdate.message().chat().id(), messageToSend).replyMarkup(new ReplyKeyboardRemove());
                            SendResponse sendResponse = bot.execute(request);
                        } catch (Exception ex) {
                            System.out.println(ex.getMessage());
                        }
                    }
                }
                return UpdatesListener.CONFIRMED_UPDATES_ALL;
            }
        });
    }

}
