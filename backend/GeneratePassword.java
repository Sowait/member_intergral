import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeneratePassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "123456";
        String encoded = encoder.encode(password);
        System.out.println("Encoded password: " + encoded);
        
        // 验证密码
        boolean matches = encoder.matches(password, encoded);
        System.out.println("Password matches: " + matches);
    }
}