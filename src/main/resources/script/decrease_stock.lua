-- KEYS[1] = "ticket_stock:{ticketId}"
-- ARGV[1] = số lượng muốn mua (quantity)
-- Return:
--   >= 0 : còn hàng, đã trừ thành công → stock còn lại
--   -1   : hết hàng (không đủ stock)
--   -2   : key không tồn tại (chưa preheat)

local stock = redis.call('GET', KEYS[1])

-- Key không tồn tại = chưa preheat cache
if stock == false then
    return -2
end

local currentStock = tonumber(stock)
local quantity = tonumber(ARGV[1])

-- Không đủ hàng
if currentStock < quantity then
    return -1
end

-- Đủ hàng → trừ và trả về stock còn lại
return redis.call('DECRBY', KEYS[1], quantity)