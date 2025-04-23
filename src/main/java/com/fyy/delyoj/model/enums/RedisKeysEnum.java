package com.fyy.delyoj.model.enums;

public enum RedisKeysEnum {

    EXAM("考试", "exam"),
    SCORE("成绩","score"),
    RANK("排名","rank"),
    LOCK("锁","lock"),
    INIT("初始化","init"),
    USER("用户","user");



    private final String text;
    private final String value;

    RedisKeysEnum(String text, String value){
        this.text = text;
        this.value = value;
    }

    public static String examRankKey(Long examId){
        return EXAM.value + ":" + RANK.value + ":" + examId;
    }
    public static String userScoreKey(Long examId, Long userId){
        return EXAM.value + ":" + SCORE.value + ":" +examId + ":" + USER.value + ":" + userId;
    }
    public static String ExamLockKey(Long examId){
        return LOCK.value + ":" + EXAM.value + ":" +INIT.value +":" + examId;
    }


}
