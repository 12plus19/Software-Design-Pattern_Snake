package com.kob.botrunningsystem.utils;

import java.io.*;
import java.util.concurrent.TimeUnit;

//ljp修改 Python代码执行器
public class PythonBotExecutor implements BotExecutorInterface {
    @Override
    public Integer nextMove(String code, String input) {
        try {
            // 创建临时Python文件
            File botFile = createTempPythonFile(code);

            // 构建命令
            ProcessBuilder processBuilder = new ProcessBuilder("python3", botFile.getAbsolutePath(), input);
            Process process = processBuilder.start();

            // 设置超时时间
            if (!process.waitFor(5, TimeUnit.SECONDS)) {
                process.destroy();
                throw new RuntimeException("代码执行超出时间限制");
            }

            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();

            // 删除临时文件
            botFile.delete();

            return Integer.parseInt(result);
        } catch (Exception e) {
            throw new RuntimeException("执行python代码失败", e);
        }
    }

    private File createTempPythonFile(String code) throws IOException {
        File tempFile = File.createTempFile("bot_", ".py");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(code);
        }
        return tempFile;
    }
}