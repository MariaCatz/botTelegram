package test;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Query;
import org.json.*;

public class YandexDataWork {
    private JSONObject getJSONfromUrl(String url) {
        try {
            URLConnection connection = new URL(url).openConnection();
            InputStream is = connection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);
            char[] buffer = new char[256];
            int rc;

            StringBuilder str = new StringBuilder();

            while ((rc = reader.read(buffer)) != -1)
                str.append(buffer, 0, rc);
            reader.close();
            return new JSONObject(str.toString());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    private List<YandexCompany> getCompaniesParseJSON(JSONObject JSONforParse) {
        List<YandexCompany> companies = new ArrayList<YandexCompany>();
        JSONArray arr = JSONforParse.getJSONArray("features");
        JSONObject companyJSON;
        YandexCompany Yandexcompany;
        String classRes = "";
        String hours = "";
        for (int i = 0; i < arr.length(); i++) {
            companyJSON = arr.getJSONObject(i).
                    getJSONObject("properties").
                    getJSONObject("CompanyMetaData");
            Yandexcompany = new YandexCompany();
            Yandexcompany.setId(companyJSON.getString("id"));
            Yandexcompany.setAddress(companyJSON.getString("address"));
            Yandexcompany.setName(companyJSON.getString("name"));
            for (int j = 0; j < companyJSON.getJSONArray("Categories").length(); j++) {
                JSONforParse = companyJSON.getJSONArray("Categories").getJSONObject(j);
                try {
                    classRes = JSONforParse.getString("class");
                } catch (Exception ex) {
                    classRes = new String("");
                }
                Yandexcompany.addCategory(new YandexCategory
                        (Yandexcompany, JSONforParse.
                                getString("name"),
                                classRes
                        ));
            }
            try {
                hours = companyJSON.getJSONObject("Hours").
                        getString("text");
            } catch (Exception ex) {
                hours = new String("");
            }
            Yandexcompany.setHours
                    (new YandexCompanyHours
                            (Yandexcompany, hours));

            companies.add(Yandexcompany);
        }
        return companies;
    }

    private void saveCompanies(List<YandexCompany> companies, Session session) {
        for (int i = 0; i < companies.size(); i++) {
            Transaction transaction = session.beginTransaction();
            try {
                session.save(companies.get(i));
                transaction.commit();
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                transaction.rollback();
            }
        }
    }

    public void gettingYandexPlaces(SessionFactory factory) {
        Session session = factory.openSession();
        session.beginTransaction();
        session.createSQLQuery("ALTER TABLE yandexcompany" +
                " ADD CONSTRAINT uniquecompany UNIQUE (name,address)").executeUpdate();
        session.getTransaction().commit();

        String url = "https://search-maps.yandex.ru/v1/?apikey=f4d87dae-3b78-46d1-ab06-947d9b9473c4&" +
                "lang=ru&text=Новосибирск+Еда&results=500";
        //Long count = (Long) session.
        //createQuery("select count(*) from YandexCompany").getSingleResult();
        int count = 0;
        while (count < 2500) {

            JSONObject JSONobject = getJSONfromUrl(url + "&skip=" + count);

            //parse json (getting companies)
            try {
                List<YandexCompany> companies = getCompaniesParseJSON(JSONobject);

                saveCompanies(companies, session);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

                /*count = (Long) session.
                        createQuery("select count(*) from YandexCompany").getSingleResult();
                System.out.println(count);*/
            count += 500;
        }
        session.close();
    }

    public void gettingPlacesRating(SessionFactory factory) {
        Session session = factory.openSession();

        Query query = session.createQuery("from YandexCompany");
        List<YandexCompany> companies = query.list();

        YandexCompany company;
        for (int i = 0; i < companies.size(); i++) {
            company = companies.get(i);

            String url = ("https://maps.googleapis.com/maps/api/place/textsearch/json?key=AIzaSyC9g73NhyPlwt0e3iR0bjKFDkvF_WWTR1k&"
                    + "query=");
            String encodeParams;
            try {
                encodeParams = URLEncoder.encode
                        (company.getName() + " " + company.getAddress(), "UTF-8");
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
                return;
            }
            url += encodeParams;


            JSONObject JSONobj = getJSONfromUrl(url);
            YandexCompanyRating rating = new YandexCompanyRating();
            rating.setCompany(company);
            try {
                rating.setRating(JSONobj.getJSONArray
                        ("results").
                        getJSONObject(0).getDouble("rating"));
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            Transaction tr = session.beginTransaction();
            try {
                session.save(rating);
                tr.commit();
            } catch (Exception ex) {
                tr.rollback();
            }

        }
        session.close();
    }

}
