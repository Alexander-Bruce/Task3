import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Client extends Thread {
    private Socket socket; // 客户端套接字
    private BufferedReader br; // 缓冲读取器
    private FileReader fr; // 文件读取器
    private int fileLength; // 文件长度
    private final String Init_message = "In"; // 初始化消息
    private byte[] fileBytes; // 文件字节数组
    private int totalchunk; // 总块数
    private Receiver receiver; // 接收器

    // 客户端构造函数，接受文件路径和反转文件路径
    public Client(String filePath, String reverseFilePath) {
        try {
            socket = new Socket("localhost", 1234); // 连接到服务器
            System.out.println("发现服务器");

            File file = new File(filePath); // 创建文件对象
            try {
                fr = new FileReader(file); // 创建文件读取器
                br = new BufferedReader(fr); // 创建缓冲读取器
            } catch (Exception e) {
                System.out.printf("不能成功读取文件: %s%n", e.getMessage());
            }

            // 执行初次连接以发送和接收 "agree" 消息
            if (initializeConnection()) {
                System.out.println("成功建立连接");
                System.out.printf("总块数为: %d%n", totalchunk);
                try {
                    Thread.sleep(1000); // 睡眠一秒
                } catch (Exception e) {
                    System.out.println("不能建立连接");
                }

                // 启动 Sender 和 Receiver 线程
                receiver = new Receiver(socket, reverseFilePath);
                receiver.start();
                new Sender(socket, br, fileLength, fileBytes, totalchunk, receiver).start();
            } else {
                System.out.println("不能建立连接");
                closeResources();
            }
        } catch (Exception e) {
            System.out.printf("发生错误: %s%n", e.getMessage());
            closeResources();
        }
    }

    // 初始化连接，发送初始化消息并接收 "agree" 消息
    private boolean initializeConnection() {
        try {
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream()); // 创建输出流
            DataInputStream inputStream = new DataInputStream(socket.getInputStream()); // 创建输入流

            StringBuilder fileContent = new StringBuilder(); // 文件内容
            String line;

            // 读取文件内容
            while ((line = br.readLine()) != null) {
                fileContent.append(line).append("\n");
            }

            fileBytes = fileContent.toString().getBytes(StandardCharsets.UTF_8); // 文件字节数组
            fileLength = fileBytes.length; // 文件长度
            if (fileLength > 10) {
                Random random = new Random();
                totalchunk = random.nextInt((10 - 5) + 1) + 5; // 随机生成总块数
            } else {
                totalchunk = 1; // 文件小于等于10字节时块数为1
            }

            // 发送初始化消息到服务器
            InitMessage initMessage = new InitMessage(Init_message, totalchunk);
            byte[] initMessageBytes = initMessage.createInitMessage();
            outputStream.write(initMessageBytes);
            outputStream.flush();

            // 接收来自服务器的 "agree" 消息
            byte[] receivedBytes = new byte[2]; // "agree" 的长度为2字节
            inputStream.readFully(receivedBytes);
            String receivedMessage = new String(receivedBytes, StandardCharsets.UTF_8);

            return "Ag".equals(receivedMessage.trim()); // 判断是否接收到"agree"消息
        } catch (IOException e) {
            System.out.printf("初始化连接时发生错误: %s%n", e.getMessage());
            return false;
        }
    }

    // 关闭资源
    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
            if (receiver != null) {
                receiver.stopRunning();
            }
        } catch (IOException e) {
            System.out.printf("释放资源时发生错误: %s%n", e.getMessage());
        }
    }

    // 客户端主方法
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        String filePath = "C:\\Users\\26259\\OneDrive\\桌面\\New\\ascii_document.txt";
        String reverseFilePath = "C:\\Users\\26259\\OneDrive\\桌面\\New\\reverse.txt";
        new Client(filePath, reverseFilePath); // 创建并启动客户端线程
    }
}

// 初始化消息类
class InitMessage {
    private final int fileLength; // 文件长度

    private final String initMessage = "In"; // 初始化消息

    public InitMessage(String header, int fileLength) {
        this.fileLength = fileLength; // 初始化文件长度
    }

    // 创建初始化消息
    public byte[] createInitMessage() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(); // 字节数组输出流
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream); // 数据输出流

        // 写入头部 (2 字节)
        byte[] headerBytes = new byte[2];
        System.arraycopy(initMessage.getBytes(StandardCharsets.UTF_8), 0, headerBytes, 0, initMessage.length());
        dataOutputStream.write(headerBytes);

        // 写入文件长度 (4 字节)
        dataOutputStream.writeInt(fileLength);

        return byteArrayOutputStream.toByteArray(); // 返回初始化消息字节数组
    }
}

// 发送消息的线程类
class Sender extends Thread {
    private final Socket socket; // 套接字
    private final BufferedReader fileReader; // 文件读取器
    private DataOutputStream clientToServer; // 客户端到服务器的数据输出流
    private final Random random; // 随机数生成器
    private final int fileLength; // 文件长度
    private final int totalchunk; // 总块数
    private final byte[] fileBytes; // 文件字节数组
    private final Receiver receiver; // 接收器

    public Sender(Socket socket, BufferedReader fileReader, int fileLength, byte[] fileBytes, int totalchunk, Receiver receiver) {
        this.socket = socket;
        this.fileReader = fileReader;
        this.random = new Random();
        this.fileLength = fileLength;
        this.fileBytes = fileBytes;
        this.totalchunk = totalchunk;
        this.receiver = receiver;
        try {
            this.clientToServer = new DataOutputStream(socket.getOutputStream()); // 创建数据输出流
        } catch (Exception e) {
            System.out.printf("创建输出流时发生错误: %s%n", e.getMessage());
        }
    }

    // 运行方法
    public void run() {
        try {
            int currentPos = 0; // 当前文件读取位置

            // 发送文件块
            for (int i = 0; i < totalchunk; i++) {
                int remainingLength = fileLength - currentPos; // 剩余长度
                int chunkLength;

                // 如果是最后一块，则发送剩余的所有数据
                if (i == totalchunk - 1 || remainingLength <= 5) {
                    chunkLength = remainingLength;
                } else {
                    // 生成随机长度
                    chunkLength = random.nextInt(Math.min(1024, remainingLength) - 5 + 1) + 5;
                }

                // 创建包含2字节头部、4字节长度和内容的消息
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

                // 2字节头部 (例如 "RR" 表示 Reverse Request)
                String header = "RR";
                byte[] headerBytes = new byte[2];
                System.arraycopy(header.getBytes(StandardCharsets.UTF_8), 0, headerBytes, 0, header.length());
                dataOutputStream.write(headerBytes);

                // 4字节长度
                dataOutputStream.writeInt(chunkLength);

                // 提取数据块
                byte[] chunk = new byte[chunkLength];
                System.arraycopy(fileBytes, currentPos, chunk, 0, chunkLength);
                dataOutputStream.write(chunk);

                byte[] message = byteArrayOutputStream.toByteArray();

                // 发送消息到服务器
                clientToServer.write(message);
                clientToServer.flush();

                System.out.printf("消息头: %s, 长度: %d%n", header, chunkLength);

                currentPos += chunkLength;

                // 模拟延迟或结束条件
                Thread.sleep(1000); // 根据需要调整睡眠时间
            }
        } catch (Exception e) {
            System

                    .out.printf("发送消息时发送错误: %s%n", e.getMessage());
        } finally {
            // 发送完成后通知接收器关闭
            receiver.stopRunning();
            // 关闭资源
            closeResources();
        }
    }

    // 关闭资源
    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("套接字关闭");
            }
            if (fileReader != null) {
                fileReader.close();
            }
            if (clientToServer != null) {
                clientToServer.close();
            }
        } catch (IOException e) {
            System.out.printf("释放资源时发生错误: %s%n", e.getMessage());
        }
    }
}

// 接收消息的线程类
class Receiver extends Thread {
    private final Socket socket; // 套接字
    private DataInputStream inputStream; // 数据输入流
    private int count; // 计数器
    private final String filePath; // 文件路径
    private volatile boolean isRunning = true; // 运行状态标志

    public Receiver(Socket socket, String filePath) {
        this.socket = socket;
        this.filePath = filePath;
        try {
            this.inputStream = new DataInputStream(socket.getInputStream()); // 创建数据输入流
        } catch (IOException e) {
            System.out.printf("创建输入流时发生错误: %s%n", e.getMessage());
        }
    }

    // 运行方法
    public void run() {
        try {
            while (isRunning) {
                byte[] headerBytes = new byte[2];
                if (inputStream.read(headerBytes) == -1) break; // 读取头部
                String header = new String(headerBytes, StandardCharsets.UTF_8);

                int contentLength = inputStream.readInt(); // 读取内容长度
                byte[] contentBytes = new byte[contentLength];
                inputStream.readFully(contentBytes); // 读取内容
                String content = new String(contentBytes, StandardCharsets.UTF_8);

                System.out.printf("消息头: %s 第%d块%n", header, count + 1);
                System.out.printf("消息长度: %d%n", contentLength);
                System.out.printf("内容: %s%n", content);

                // 将新内容写入文件最前面
                prependContentToFile(content);

                count++;
            }
        } catch (EOFException e) {
            System.out.println("End of stream reached.");
        } catch (SocketException e) {
            System.out.println("套接字关闭");
        } catch (IOException e) {
            System.out.printf("接收消息时发生错误: %s%n", e.getMessage());
        } finally {
            closeResources(); // 关闭资源
        }
    }

    // 停止运行
    public void stopRunning() {
        isRunning = false;
        closeResources(); // 关闭资源
    }

    // 将新内容写入文件最前面
    private void prependContentToFile(String content) {
        try {
            // 读取现有文件内容
            List<String> lines = Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);

            // 创建临时文件
            File tempFile = new File(String.format("temp_%s", new File(filePath).getName()));
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile, StandardCharsets.UTF_8, false));

            // 写入新内容
            writer.print(content);

            // 写入原文件内容
            for (String line : lines) {
                writer.println(line);
            }

            writer.close();

            // 替换原文件
            Files.delete(Paths.get(filePath));
            Files.move(Paths.get(tempFile.getPath()), Paths.get(filePath));

            System.out.println("内容已写入文件");
        } catch (IOException e) {
            System.out.println("不能写入文件");
            System.out.println(e.getMessage());
        }
    }

    // 关闭资源
    private void closeResources() {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("套接字关闭");
            }
        } catch (IOException e) {
            System.out.printf("关闭资源时出现错误: %s%n", e.getMessage());
        }
    }
}