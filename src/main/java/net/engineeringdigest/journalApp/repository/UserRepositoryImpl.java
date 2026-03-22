package net.engineeringdigest.journalApp.repository;

import net.engineeringdigest.journalApp.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl {

    @Autowired
    private MongoTemplate mongoTemplate; // ❌ removed static

    public List<User> getUserForSA() { // ❌ removed static

        Query query = new Query();

        query.addCriteria(
                Criteria.where("email")
                        .regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
        );

        query.addCriteria(
                Criteria.where("sentimentAnalysis").is(true)
        );

        return mongoTemplate.find(query, User.class);
    }
}