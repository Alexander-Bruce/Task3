import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class AsciiFileGenerator {
    public static void main(String[] args) {
<<<<<<< HEAD
        String fileName = "ascii_document.txt";
=======
        String fileName = "C:\\Users\\26259\\OneDrive\\桌面\\New\\ascii_document.txt";
>>>>>>> 7d3cb73f6f747c7d08913470db857e1cf1f91b10

        try {
            // 创建 PrintWriter 对象
            PrintWriter writer = new PrintWriter(new FileWriter(fileName));
            Random random = new Random();
            int targetLength = 1000;

            // 写入所有ASCII字符
            StringBuilder content = new StringBuilder();
            for (int i = 32; i < 127; i++) {
                content.append((char) i);
            }

            // 继续写入随机ASCII字符直到达到1000个字符
            while (content.length() < targetLength) {
                char randomChar = (char) (random.nextInt(95) + 32); // 生成范围在32到126之间的ASCII字符
                content.append(randomChar);
            }

            writer.print(content);

            // 关闭 PrintWriter
            writer.close();
            System.out.println("ASCII document created successfully.");
        } catch (IOException e) {
            System.out.println(STR."An error occurred.\{e.getMessage()}");
        }
    }
}
