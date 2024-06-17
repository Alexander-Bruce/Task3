import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Server {

    private static final ArrayList<ClientHandler> clients = new ArrayList<>(); // 存储所有客户端处理程序的列表

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(1234); // 创建服务器套接字并绑定到端口1234
            System.out.println("服务器已启动，等待客户端连接...");

            // 不断等待客户端连接
            while (true) {
                Socket clientSocket = serverSocket.accept(); // 接受客户端连接
                System.out.println(STR."客户端连接: \{clientSocket.getInetAddress()}");
                ClientHandler clientHandler = new ClientHandler(clientSocket); // 创建新的ClientHandler处理该客户端
                clients.add(clientHandler); // 将新客户端处理程序添加到列表
                clientHandler.start(); // 启动处理线程
            }
        } catch (IOException e) {
            System.out.println(STR."发生错误: \{e.getMessage()}");
        }
    }
}

// 客户端处理线程类
class ClientHandler extends Thread {
    private final Socket socket; // 客户端套接字
    private DataInputStream inputStream; // 数据输入流
    private DataOutputStream outputStream; // 数据输出流
    private boolean isInitialized; // 标志是否已初始化
    private boolean isRunning; // 标志线程是否在运行

    public static final String INIT_HEADER = "In"; // 初始化消息头部
    public static final String AGREE_MESSAGE = "Ag"; // 同意消息

    public int chunkcount; // 分块计数器

    // 构造函数
    public ClientHandler(Socket socket) {
        this.socket = socket; // 初始化套接字
        this.isInitialized = false; // 初始化未完成
        this.isRunning = true; // 标志线程在运行
        try {
            inputStream = new DataInputStream(socket.getInputStream()); // 创建输入流
            outputStream = new DataOutputStream(socket.getOutputStream()); // 创建输出流
        } catch (IOException e) {
            System.out.println(STR."流创建错误: \{e.getMessage()}");
        }
    }

    @Override
    public void run() {
        try {
            while (isRunning) { // 当线程运行时循环
                if (!isInitialized) {
                    initializeClient(); // 初始化客户端
                } else {
                    System.out.println("等待消息...");
                    processMessages(); // 处理后续消息
                }
            }
        } catch (IOException e) {
            System.out.println(STR."I/O 错误: \{e.getMessage()}");
        } finally {
            closeResources(); // 关闭资源
        }
    }

    private void initializeClient() throws IOException {
        byte[] initHeaderBytes = new byte[INIT_HEADER.length()]; // 初始化消息头字节数组
        inputStream.readFully(initHeaderBytes); // 完全读取初始化消息头
        String receivedHeader = new String(initHeaderBytes, StandardCharsets.UTF_8); // 转换为字符串

        if (INIT_HEADER.equals(receivedHeader)) {
            System.out.println(STR."初始化消息接收: \{receivedHeader}");

            byte[] initContentLengthBytes = new byte[4]; // 初始化消息内容长度字节数组
            inputStream.readFully(initContentLengthBytes); // 完全读取内容长度
            int initContentLength = java.nio.ByteBuffer.wrap(initContentLengthBytes).getInt(); // 将字节数组转换为整数
            System.out.println(STR."接收到信息的长度: \{initContentLength}");
            chunkcount = initContentLength; // 设置分块计数

            outputStream.write(AGREE_MESSAGE.getBytes(StandardCharsets.UTF_8)); // 发送同意消息
            outputStream.flush(); // 刷新输出流
            System.out.println("发送同意消息成功");

            isInitialized = true; // 标记初始化完成
        } else {
            System.out.println(STR."异常头部消息: \{receivedHeader}");
            // 可以在这里添加更多错误处理逻辑，如关闭连接等
        }
    }

    private void processMessages() throws IOException {
        boolean flag = true; // 标志处理消息的循环
        try {
            while (flag && isRunning) {
                if (chunkcount == 0) { // 检查分块计数
                    flag = false; // 终止循环
                    isRunning = false; // 终止运行
                }
                chunkcount--; // 递减分块计数
                byte[] headerBytes = new byte[2]; // 创建消息头字节数组
                if (inputStream.read(headerBytes) == -1) break; // 读取消息头
                String header = new String(headerBytes, StandardCharsets.UTF_8); // 转换为字符串
                System.out.println(STR."收到的头部: \{header}");

                byte[] contentLengthBytes = new byte[4]; // 创建内容长度字节数组
                inputStream.readFully(contentLengthBytes); // 完全读取内容长度
                int contentLength = java.nio.ByteBuffer.wrap(contentLengthBytes).getInt(); // 将字节数组转换为整数
                System.out.println(STR."收到消息的长度: \{contentLength}");

                if (contentLength > 0) { // 确保内容长度是合理的，避免分配过大的数组
                    byte[] contentBytes = new byte[contentLength]; // 创建内容字节数组
                    inputStream.readFully(contentBytes); // 完全读取内容
                    String content = new String(contentBytes, StandardCharsets.UTF_8); // 转换为字符串
                    System.out.println(STR."内容: \{content}");

                    String reversedContent = new StringBuilder(content).reverse().toString(); // 反转字符串内容
                    sendResponse("RS", reversedContent); // 发送反转后的内容作为响应
                }
            }
        } catch (EOFException e) {
            System.out.println("客户端关闭连接");
            isRunning = false; // 终止运行
        } catch (SocketException e) {
            System.out.println("套接字错误关闭");
            isRunning = false; // 终止运行
        } finally {
            closeResources(); // 关闭资源
        }
    }

    private void sendResponse(String header, String content) throws IOException {
        byte[] responseHeaderBytes = header.getBytes(StandardCharsets.UTF_8); // 转换响应头为字节数组
        byte[] responseContentBytes = content.getBytes(StandardCharsets.UTF_8); // 转换响应内容为字节数组
        int responseContentLength = responseContentBytes.length; // 获取响应内容长度

        outputStream.write(responseHeaderBytes); // 发送2字节的响应头
        outputStream.writeInt(responseContentLength); // 发送4字节的内容长度
        outputStream.write(responseContentBytes); // 发送实际内容
        outputStream.flush(); // 刷新输出流

        System.out.println(STR."响应消息头: \{header}, 内容长度: \{responseContentLength}");
    }

    private void closeResources() {
        try {
            if (inputStream != null) {
                inputStream.close(); // 关闭输入流
            }
            if (outputStream != null) {
                outputStream.close(); // 关闭输出流
            }
            if (socket != null && !socket.isClosed()) {
                socket.close(); // 关闭套接字
                System.out.println("关闭套接字成功");
            }
        } catch (IOException e) {
            System.out.println(STR."套接字关闭错误: \{e.getMessage()}");
        }
    }
}
