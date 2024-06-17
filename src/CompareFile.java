import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class CompareFile {

    public static boolean areFilesReversed(String filePath1, String filePath2) {
        try {
            // 读取第一个文件的内容
            StringBuilder content1 = new StringBuilder();
            try (BufferedReader reader1 = new BufferedReader(new FileReader(filePath1))) {
                String line;
                while ((line = reader1.readLine()) != null) {
                    content1.append(line).append("\n");
                }
            }

            // 读取第二个文件的内容
            StringBuilder content2 = new StringBuilder();
            try (BufferedReader reader2 = new BufferedReader(new FileReader(filePath2))) {
                String line;
                while ((line = reader2.readLine()) != null) {
                    content2.append(line).append("\n");
                }
            }

            // 去掉头尾的换行符
            String contentStr1 = content1.toString().trim();
            String contentStr2 = content2.toString().trim();

            // 比较第一个文件的反转内容是否等于第二个文件的内容
            return new StringBuilder(contentStr1).reverse().toString().equals(contentStr2);

        } catch (IOException e) {
            System.out.println(STR."An error occurred while reading the files.\{e.getMessage()}");
            return false;
        }
    }

    public static void main(String[] args) {
        String filePath1 = "C:\\Users\\26259\\OneDrive\\桌面\\NewVS2010\\ascii_document.txt";
        String filePath2 = "C:\\Users\\26259\\OneDrive\\桌面\\NewVS2010\\reverse.txt";

        boolean result = areFilesReversed(filePath1, filePath2);
        if (result) {
            System.out.println("The contents of the first file reversed are equal to the contents of the second file.");
        } else {
            System.out.println("The contents of the first file reversed are NOT equal to the contents of the second file.");
        }
    }
}
