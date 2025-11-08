-- 创建数据库
CREATE DATABASE IF NOT EXISTS member_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE member_system;

-- 用户表
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    role ENUM('MEMBER', 'ADMIN') NOT NULL DEFAULT 'MEMBER' COMMENT '角色：MEMBER会员，ADMIN管理员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除'
) COMMENT '用户表';

-- 会员信息表
CREATE TABLE members (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    member_code VARCHAR(20) NOT NULL UNIQUE COMMENT '会员号',
    level ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE' COMMENT '会员等级',
    total_points INT DEFAULT 0 COMMENT '总积分',
    available_points INT DEFAULT 0 COMMENT '可用积分',
    register_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    FOREIGN KEY (user_id) REFERENCES users(id)
) COMMENT '会员信息表';

-- 积分记录表
CREATE TABLE point_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    points_change INT NOT NULL COMMENT '积分变化（正数为获得，负数为消费）',
    type ENUM('EARN', 'CONSUME') NOT NULL COMMENT '类型：EARN获得，CONSUME消费',
    order_id BIGINT COMMENT '关联订单ID',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (member_id) REFERENCES members(id)
) COMMENT '积分记录表';

-- 门店表
CREATE TABLE stores (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    store_name VARCHAR(100) NOT NULL COMMENT '门店名称',
    address VARCHAR(255) COMMENT '地址',
    phone VARCHAR(20) COMMENT '电话',
    manager_id BIGINT COMMENT '店长ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    FOREIGN KEY (manager_id) REFERENCES users(id)
) COMMENT '门店表';

-- 消费记录表
CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    store_id BIGINT NOT NULL COMMENT '门店ID',
    amount DECIMAL(10,2) NOT NULL COMMENT '消费金额',
    points_earned INT NOT NULL DEFAULT 0 COMMENT '获得积分',
    order_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    description VARCHAR(255) COMMENT '描述',
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (store_id) REFERENCES stores(id)
) COMMENT '消费记录表';

-- 兑换商品表
CREATE TABLE exchange_products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description TEXT COMMENT '商品描述',
    points_required INT NOT NULL COMMENT '所需积分',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    image_url VARCHAR(500) COMMENT '商品图片URL',
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE上架，INACTIVE下架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除'
) COMMENT '兑换商品表';

-- 兑换记录表
CREATE TABLE exchange_records (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL COMMENT '会员ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    points_used INT NOT NULL COMMENT '使用积分',
    exchange_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '兑换时间',
    receive_info VARCHAR(255) COMMENT '领取信息',
    FOREIGN KEY (member_id) REFERENCES members(id),
    FOREIGN KEY (product_id) REFERENCES exchange_products(id)
) COMMENT '兑换记录表';

-- 插入初始数据
-- 插入管理员账号（用户名：admin，密码：123456）
INSERT INTO users (username, password, phone, email, role) VALUES 
('admin', '$2a$10$Wo2pFO8oWOzUayKYHiT9M..R9p8aeyleFvqEohEvulCKkGA1YtaUa', '13800138000', 'admin@restaurant.com', 'ADMIN');

-- 插入测试门店
INSERT INTO stores (store_name, address, phone) VALUES 
('总店', '北京市朝阳区建国路88号', '010-12345678'),
('分店1', '北京市海淀区中关村大街1号', '010-87654321');

-- 插入测试商品
INSERT INTO exchange_products (product_name, description, points_required, stock_quantity, status) VALUES 
('10元代金券', '可在任意门店使用，满50可用', 100, 100, 'ACTIVE'),
('20元代金券', '可在任意门店使用，满100可用', 180, 50, 'ACTIVE'),
('免费饮料', '指定饮料任选一杯', 50, 200, 'ACTIVE'),
('会员升级券', '直接升级为下一级会员', 500, 20, 'ACTIVE');

-- 插入测试用户 密码 123456
INSERT INTO users (username, password, phone, email, role, create_time, update_time) VALUES 
('testuser1', '$2a$10$Wo2pFO8oWOzUayKYHiT9M..R9p8aeyleFvqEohEvulCKkGA1YtaUa', '13800138001', 'test1@example.com', 'MEMBER', NOW(), NOW()),
('testuser2', '$2a$10$Wo2pFO8oWOzUayKYHiT9M..R9p8aeyleFvqEohEvulCKkGA1YtaUa', '13800138002', 'test2@example.com', 'MEMBER', NOW(), NOW()),
('testuser3', '$2a$10$Wo2pFO8oWOzUayKYHiT9M..R9p8aeyleFvqEohEvulCKkGA1YtaUa', '13800138003', 'test3@example.com', 'MEMBER', NOW(), NOW()),
('testuser4', '$2a$10$Wo2pFO8oWOzUayKYHiT9M..R9p8aeyleFvqEohEvulCKkGA1YtaUa', '13800138004', 'test4@example.com', 'MEMBER', NOW(), NOW());

-- 插入对应的会员信息（不同等级）
INSERT INTO members (user_id, member_code, level, total_points, available_points, register_time, update_time) VALUES 
(2, 'M2023103101', 'BRONZE', 100, 100, '2023-10-31 10:00:00', NOW()),
(3, 'M2023103102', 'SILVER', 500, 450, '2023-10-31 11:00:00', NOW()),
(4, 'M2023103103', 'GOLD', 1200, 800, '2023-10-31 12:00:00', NOW()),
(5, 'M2023103104', 'PLATINUM', 3000, 2500, '2023-10-31 13:00:00', NOW());

-- 插入一些积分记录
INSERT INTO point_records (member_id, points_change, type, description, create_time) VALUES 
(1, 100, 'EARN', '注册赠送积分', '2023-10-31 10:00:00'),
(2, 500, 'EARN', '消费获得积分', '2023-10-31 11:00:00'),
(2, -50, 'CONSUME', '积分兑换商品', '2023-10-31 11:30:00'),
(3, 1200, 'EARN', '大额消费获得积分', '2023-10-31 12:00:00'),
(3, -400, 'CONSUME', '积分兑换代金券', '2023-10-31 12:30:00'),
(4, 3000, 'EARN', 'VIP消费获得积分', '2023-10-31 13:00:00'),
(4, -500, 'CONSUME', '积分兑换高级商品', '2023-10-31 13:30:00');

-- 插入一些消费记录
INSERT INTO orders (order_no, member_id, store_id, amount, points_earned, order_time, description) VALUES 
('ORD2023103101', 1, 1, 100.00, 10, '2023-10-31 10:30:00', '普通消费'),
('ORD2023103102', 2, 1, 500.00, 50, '2023-10-31 11:30:00', '中等消费'),
('ORD2023103103', 3, 2, 1200.00, 120, '2023-10-31 12:30:00', '大额消费'),
('ORD2023103104', 4, 1, 3000.00, 300, '2023-10-31 13:30:00', 'VIP消费');