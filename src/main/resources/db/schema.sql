CREATE DATABASE IF NOT EXISTS idlewheels
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE idlewheels;

CREATE TABLE users (
  id            BIGINT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(150) NOT NULL UNIQUE,
  password_hash VARCHAR(100) NOT NULL,
  phone         VARCHAR(15),
  city          VARCHAR(80)  NOT NULL,
  role          VARCHAR(10)  NOT NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_users_role CHECK (role IN ('OWNER','RENTER'))
);

CREATE TABLE vehicles (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id         BIGINT      NOT NULL,
  vehicle_type     VARCHAR(10) NOT NULL,
  make             VARCHAR(60) NOT NULL,
  model            VARCHAR(60) NOT NULL,
  manufacture_year SMALLINT    NOT NULL,
  seats            TINYINT     NULL,
  transmission     VARCHAR(10) NULL,
  engine_cc        INT         NULL,
  created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vehicles_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT chk_vehicle_type CHECK (vehicle_type IN ('CAR','BIKE'))
);

CREATE TABLE listings (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  vehicle_id     BIGINT        NOT NULL,
  owner_id       BIGINT        NOT NULL,
  title          VARCHAR(120)  NOT NULL,
  description    TEXT,
  city           VARCHAR(80)   NOT NULL,
  price_per_day  DECIMAL(10,2) NOT NULL,
  available_from DATE          NOT NULL,
  available_to   DATE          NOT NULL,
  photo_key      VARCHAR(255)  NULL,
  status         VARCHAR(10)   NOT NULL DEFAULT 'ACTIVE',
  created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_listings_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
  CONSTRAINT fk_listings_owner   FOREIGN KEY (owner_id)   REFERENCES users(id),
  CONSTRAINT chk_listing_price   CHECK (price_per_day > 0),
  CONSTRAINT chk_listing_dates   CHECK (available_to >= available_from),
  CONSTRAINT chk_listing_status  CHECK (status IN ('ACTIVE','INACTIVE'))
);
CREATE INDEX idx_listings_city   ON listings(city);
CREATE INDEX idx_listings_price  ON listings(price_per_day);
CREATE INDEX idx_listings_status ON listings(status);

CREATE TABLE messages (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  listing_id  BIGINT        NOT NULL,
  sender_id   BIGINT        NOT NULL,
  receiver_id BIGINT        NOT NULL,
  content     VARCHAR(1000) NOT NULL,
  sent_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  is_read     BOOLEAN       NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_msg_listing  FOREIGN KEY (listing_id)  REFERENCES listings(id),
  CONSTRAINT fk_msg_sender   FOREIGN KEY (sender_id)   REFERENCES users(id),
  CONSTRAINT fk_msg_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
);
CREATE INDEX idx_msg_listing      ON messages(listing_id);
CREATE INDEX idx_msg_receiver_read ON messages(receiver_id, is_read);