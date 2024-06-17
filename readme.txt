---

# Task3 项目

## 项目概述
Task3 是一个基于TCP协议的文件通信项目，包括了一个服务器端（Server.java）和一个客户端（Client.java）。

## 运行环境
- Java 开发环境
- 支持的操作系统：Windows、Linux、macOS

### 服务器端（Server）

1. **IP 地址和端口配置**
   - 默认使用本地 IP 地址 `127.0.0.1` 和端口 `12345`。
   - 根据需要修改 `Server.java` 文件中的 `serverIP` 和 `serverPort` 变量。

2. **运行服务器**

**Windows**
- 编译 Server.java 文件
- 打开命令提示符（Command Prompt），导航到 Server.java 文件所在的目录。
cd src
- 使用 javac 命令编译 Server.java 文件。
javac Server.java
- 如果编译成功，将生成 Server.class 文件。
- 在同一命令提示符窗口中，运行以下命令启动服务器。
java Server
- 服务器程序将开始执行，并在命令提示符窗口中显示输出信息。

**Linux / macOS**
- 编译 Server.java 文件
- 打开终端（Terminal），导航到 Server.java 文件所在的目录。
cd src
- 使用 javac 命令编译 Server.java 文件。
javac Server.java
- 如果编译成功，将生成 Server.class 文件。
- 在同一终端窗口中，运行以下命令启动服务器。
java Server
- 服务器程序将开始执行，并在终端窗口中显示输出信息。

### 客户端（Client）

1. **IP 地址和端口配置**
   - 默认使用服务器的本地 IP 地址 `127.0.0.1` 和端口 `12345`。
   - 根据需要修改 `Client.java` 文件中的 `serverIP` 和 `serverPort` 变量。

2. **运行客户端**

**Windows**
- 编译 Client.java 文件
- 打开命令提示符（Command Prompt），导航到 Client.java 文件所在的目录。
cd src
- 使用 javac 命令编译 Client.java 文件。
javac Client.java
- 如果编译成功，将生成 Client.class 文件。
- 在同一命令提示符窗口中，运行以下命令启动客户端。
java Client
- 客户端程序将开始执行，并在命令提示符窗口中显示输出信息。

**Linux / macOS**
- 编译 Client.java 文件
- 打开终端（Terminal），导航到 Client.java 文件所在的目录。
cd  src
- 使用 javac 命令编译 Client.java 文件。
javac Client.java
- 如果编译成功，将生成 Client.class 文件。
- 在同一终端窗口中，运行以下命令启动客户端。
java Client
- 客户端程序将开始执行，并在终端窗口中显示输出信息。

## 使用说明
1. **启动服务器**
   - 运行 `Server.java` 文件以启动服务器。
   - 服务器将监听指定端口，并接收来自客户端的消息。

2. **启动客户端**
   - 运行 `Client.java` 文件以启动客户端。
   - 客户端将连接到指定服务器，并发送一系列消息。

3. **查看输出**
   - 在客户端和服务器的控制台中，可以查看消息的发送、接收状态以及网络统计信息（如丢包率、RTT 等）。

## 注意事项
- 确保服务器和客户端在运行时处于相同的网络环境或能够相互访问。
- 根据需要在代码中修改消息发送频率、数量等参数。

## 版权和许可
该项目仅用于学习和教育目的。欢迎任何人根据需要修改和使用。

---

### src 中的其他类
- **AsciiFileGenerator.java**: 用于生成 ASCII 文件的辅助类，用于测试目的。
- **CompareFile.java**: 用于比较两个文件以检查文件逆转操作是否成功的辅助类。
