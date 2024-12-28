package com.kob.botrunningsystem.utils;

import org.joor.Reflect;
import java.util.UUID;

//ljp修改 Java代码执行器
public class JavaBotExecutor implements BotExecutorInterface {

    UUID uuid = UUID.randomUUID();
    String uid = uuid.toString().substring(0,8);

    private String addUid(String code, String uid){            //   在代码里的类名后面加上uid
        int k = code.indexOf(" implements com.kob.botrunningsystem.utils.BotExecutorInterface");
        return code.substring(0, k) + uid + code.substring(k);
    }


    @Override
    public Integer nextMove(String code, String input) {
        BotExecutorInterface botExecutorInterface = Reflect.compile(
                "com.kob.botrunningsystem.utils.BotJava" + uid,
                addUid(code, uid)
        ).create().get();
        return botExecutorInterface.nextMove(code, input);
    }
}