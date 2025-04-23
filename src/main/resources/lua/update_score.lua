local rankKey = KEYS[1]
local userKey = KEYS[2]
local questionId = ARGV[1]
local newScore = tonumber(ARGV[2])
local userId = ARGV[3]

-- 获取旧分数
local oldScore = redis.call('HGET', userKey, questionId)
oldScore = oldScore and tonumber(oldScore) or 0

-- 计算分数差
local diff = newScore - oldScore

-- 更新题目得分
redis.call('HSET', userKey, questionId, newScore)

redis.call('ZINCRBY', rankKey, diff, userId)


return 1