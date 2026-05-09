package utils;

public class EmailService
{
    private static final EmailService instance = new EmailService();

    private EmailService()
    {
    }

    public static EmailService getInstance()
    {
        return instance;
    }

    public void send(String to, String subject, String body)
    {
        System.out.println("\n========== EMAIL NOTIFICATION ==========");
        System.out.println("To: " + to);
        System.out.println("Subject: " + subject);
        System.out.println("Message: " + body);
        System.out.println("========================================\n");
    }
}
