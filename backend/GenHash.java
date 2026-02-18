import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();  // strength 10
        String password = "admin";
        String hash = encoder.encode(password);
        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        
        boolean test1 = encoder.matches("admin", hash);
        boolean test2 = encoder.matches("wrong", hash);
        System.out.println("Test 'admin': " + test1);
        System.out.println("Test 'wrong': " + test2);
    }
}
