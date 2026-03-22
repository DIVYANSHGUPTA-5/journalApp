package net.engineeringdigest.journalApp.repository;

import net.engineeringdigest.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, ObjectId> {

   // existing
   User findByUserName(String username);

   void deleteByUserName(String username);

   // ✅ NEW (for OAuth)
   Optional<User> findByEmail(String email);
}