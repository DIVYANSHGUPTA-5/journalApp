package net.engineeringdigest.journalApp.service;

import net.engineeringdigest.journalApp.entity.User;
import net.engineeringdigest.journalApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@Disabled
@SpringBootTest
public class UserServiceTests {

    @Autowired
    private UserRepository userRepository;



//              | Annotation      | Runs When?                  | Runs How Many Times?        | Main Purpose              |
//            |----------------|----------------------------|-----------------------------|---------------------------|
//            | @BeforeEach    | Before each test method     | Runs before every test      | Setup required per test   |
//            | @BeforeAll     | Before all test methods     | Runs only once              | Global setup              |
//            | @AfterEach     | After each test method      | Runs after every test       | Cleanup per test          |
//            | @AfterAll      | After all test methods      | Runs only once              | Final cleanup
//
//            |
  //  @Disabled  jiske upar bhi ye laga hoga wo nhi chalega



    @ParameterizedTest     /// // this is parameterized way
    @ValueSource(strings={
            "ram",
            "shyam",
            "vipul"
    })
    public void testFindByUserName(String name) {
        assertNotNull(userRepository.findByUserName(name));
    }

    @Disabled
    @ParameterizedTest
    @CsvSource({
            "1,1,2",
            "2,10,12",
            "3,3,9"
    })
    public void test(int a, int b, int expected) {
        assertEquals(expected, a + b);
    }

}