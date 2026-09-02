-- Atomic token-bucket refill + consume.
-- KEYS[1]  = the bucket's Redis key (one per rate-limited client)
-- ARGV[1]  = current time, in seconds
-- ARGV[2]  = bucket capacity (max burst size)
-- ARGV[3]  = refill rate, in tokens per second
local tokens_key = KEYS[1]
local now = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local refill_rate = tonumber(ARGV[3])

local bucket = redis.call('HMGET', tokens_key, 'tokens', 'last_refill')
local tokens = tonumber(bucket[1]) or capacity      -- a brand-new key starts full
local last_refill = tonumber(bucket[2]) or now

local elapsed = math.max(0, now - last_refill)
tokens = math.min(capacity, tokens + elapsed * refill_rate)

if tokens < 1 then
    redis.call('HMSET', tokens_key, 'tokens', tokens, 'last_refill', now)
    redis.call('EXPIRE', tokens_key, 60)   -- idle clients' state doesn't live forever
    return 0  -- rejected
else
    tokens = tokens - 1
    redis.call('HMSET', tokens_key, 'tokens', tokens, 'last_refill', now)
    redis.call('EXPIRE', tokens_key, 60)
    return 1  -- allowed
end