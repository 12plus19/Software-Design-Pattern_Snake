package com.kob.botrunningsystem.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bot {
    Integer userId;
    Integer botId;
    String botCode;
    String input;
    String language;
}
