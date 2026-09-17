-- =====================================================================
-- Food Website Backend - Initial Schema
-- Flyway migration: V1__init_schema.sql
-- Database: MySQL 8.x
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. USERS  (both ADMIN and CUSTOMER live here, differentiated by role)
-- ---------------------------------------------------------------------
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100)  NOT NULL,
    email           VARCHAR(150)  NOT NULL UNIQUE,
    phone           VARCHAR(15),
    password_hash   VARCHAR(255)  NOT NULL,
    role            ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER',
    is_verified     BOOLEAN NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. OTP_VERIFICATION  (signup verification + forgot password)
-- identifier = email, since during signup the user row may not exist yet
-- ---------------------------------------------------------------------
CREATE TABLE otp_verification (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier      VARCHAR(150) NOT NULL,           -- email the OTP was sent to
    otp_code_hash   VARCHAR(255) NOT NULL,            -- OTP stored hashed, never plain
    purpose         ENUM('SIGNUP', 'FORGOT_PASSWORD') NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    is_used         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_otp_identifier (identifier, purpose)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. ADDRESS  (customer can have multiple, one marked default)
-- ---------------------------------------------------------------------
CREATE TABLE address (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    address_line    VARCHAR(255) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    state           VARCHAR(100) NOT NULL,
    pincode         VARCHAR(10)  NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_address_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. CATEGORY
-- ---------------------------------------------------------------------
CREATE TABLE category (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(255),
    status          ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. PRODUCT
-- version column = optimistic locking, prevents race conditions on stock
-- ---------------------------------------------------------------------
CREATE TABLE product (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id          BIGINT NOT NULL,
    name                 VARCHAR(150) NOT NULL,
    description          TEXT,
    price                DECIMAL(10,2) NOT NULL,
    image_url            VARCHAR(500),
    manufacturing_date   DATE,
    expiry_date          DATE,
    stock_quantity       INT NOT NULL DEFAULT 0,
    min_stock_threshold  INT NOT NULL DEFAULT 10,
    status               ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    version              INT NOT NULL DEFAULT 0,
    created_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(id),
    INDEX idx_product_category (category_id),
    INDEX idx_product_expiry (expiry_date)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. INVENTORY_TRANSACTION  (backbone of stock auto-reflection + reports)
-- ---------------------------------------------------------------------
CREATE TABLE inventory_transaction (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id          BIGINT NOT NULL,
    transaction_type    ENUM('SALE', 'PURCHASE_IN', 'MANUAL_ADJUSTMENT', 'EXPIRED_REMOVAL') NOT NULL,
    quantity_changed    INT NOT NULL,              -- negative for SALE/removal, positive for purchase-in
    previous_quantity   INT NOT NULL,
    new_quantity        INT NOT NULL,
    reason              VARCHAR(255),
    performed_by        BIGINT NULL,               -- NULL when triggered by a customer sale
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inv_product FOREIGN KEY (product_id) REFERENCES product(id),
    CONSTRAINT fk_inv_user FOREIGN KEY (performed_by) REFERENCES users(id),
    INDEX idx_inv_product_date (product_id, created_at)   -- powers daily/monthly/yearly reports
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. CART  (one active cart per customer)
-- ---------------------------------------------------------------------
CREATE TABLE cart (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT NOT NULL UNIQUE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 8. CART_ITEMS  (Amazon/Flipkart style - many products per cart)
-- ---------------------------------------------------------------------
CREATE TABLE cart_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id         BIGINT NOT NULL,
    product_id      BIGINT NOT NULL,
    quantity        INT NOT NULL DEFAULT 1,
    added_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cartitem_cart FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE,
    CONSTRAINT fk_cartitem_product FOREIGN KEY (product_id) REFERENCES product(id),
    UNIQUE KEY uq_cart_product (cart_id, product_id)   -- same product can't have 2 rows in one cart
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 9. ORDERS
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT NOT NULL,
    address_id      BIGINT NOT NULL,
    order_date      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_amount    DECIMAL(10,2) NOT NULL,
    order_status    ENUM('PENDING', 'CONFIRMED','PREPARING','OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    payment_status  ENUM('PENDING', 'PAID', 'FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    cancellation_reason VARCHAR(255) NULL,   -- optional; frontend offers preset options + a free-text "Other"
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES users(id),
    CONSTRAINT fk_order_address FOREIGN KEY (address_id) REFERENCES address(id),
    INDEX idx_order_customer_date (customer_id, order_date),
    INDEX idx_order_date (order_date)                  -- powers sales reports
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 10. ORDER_ITEMS  (snapshot of product + price at the time of order)
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    product_id          BIGINT NOT NULL,
    quantity            INT NOT NULL,
    price_at_purchase   DECIMAL(10,2) NOT NULL,
    subtotal            DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_orderitem_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_orderitem_product FOREIGN KEY (product_id) REFERENCES product(id),
    INDEX idx_orderitem_product (product_id)
) ENGINE=InnoDB;



-- ---------------------------------------------------------------------
-- 11. INVOICE  (one invoice per order)
-- ---------------------------------------------------------------------
CREATE TABLE invoice (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL UNIQUE,
    invoice_number  VARCHAR(50) NOT NULL UNIQUE,
    billing_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal        DECIMAL(10,2) NOT NULL,
    tax             DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount        DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_invoice_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_invoice_date (billing_date)              -- powers billing reports
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 12. PAYMENT
-- ---------------------------------------------------------------------
CREATE TABLE payment (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id            BIGINT NOT NULL,
    payment_method      ENUM('UPI', 'CASH', 'CARD', 'NETBANKING') NOT NULL,
    transaction_ref     VARCHAR(100),
    status              ENUM('PENDING', 'SUCCESS', 'FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    paid_at             TIMESTAMP NULL,
    CONSTRAINT fk_payment_order FOREIGN KEY (order_id) REFERENCES orders(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 13. AUDIT_LOG  (admin action tracking)
-- ---------------------------------------------------------------------
CREATE TABLE audit_log (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_id        BIGINT NOT NULL,
    action          VARCHAR(100) NOT NULL,
    entity_name     VARCHAR(100) NOT NULL,
    entity_id       BIGINT,
    timestamp       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_admin FOREIGN KEY (admin_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 14. Restaurant_settings  (admin action tracking)
-- ---------------------------------------------------------------------

CREATE TABLE restaurant_setting (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- General
    restaurant_name VARCHAR(150) NOT NULL,
    owner_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    fssai_license VARCHAR(100),
    gst_number VARCHAR(50),
    address VARCHAR(500),
    opening_time TIME,
    closing_time TIME,

    -- Delivery
    delivery_radius_km DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    minimum_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    standard_delivery_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    free_delivery_above DECIMAL(10,2),
    average_delivery_time_minutes INT NOT NULL DEFAULT 30,
    max_concurrent_orders INT NOT NULL DEFAULT 10,

    -- Payment methods
    upi_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    card_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    cash_on_delivery_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    netbanking_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    -- Notifications
    new_order_received BOOLEAN NOT NULL DEFAULT TRUE,
    order_delivered BOOLEAN NOT NULL DEFAULT TRUE,
    low_stock_alert BOOLEAN NOT NULL DEFAULT TRUE,
    new_customer_registered BOOLEAN NOT NULL DEFAULT TRUE,
    daily_report BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
);