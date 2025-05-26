CREATE TABLE Users (
                       user_id INT AUTO_INCREMENT PRIMARY KEY,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       profile_picture VARCHAR(255),
                       role ENUM('client', 'provider', 'admin') NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       is_active BOOLEAN DEFAULT TRUE,
                       INDEX idx_role (role),
                       INDEX idx_email (email)
);

CREATE TABLE Service_Providers (
                                   provider_id INT PRIMARY KEY,
                                   name VARCHAR(100) NOT NULL,
                                   mobile_number VARCHAR(20) UNIQUE NOT NULL,
                                   address TEXT,
                                   service_type VARCHAR(50) NOT NULL,
                                   is_gold_member BOOLEAN DEFAULT FALSE,
                                   gold_member_status ENUM('pending', 'approved', 'rejected', 'none') DEFAULT 'none',
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                   FOREIGN KEY (provider_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                                   INDEX idx_service_type (service_type),
                                   INDEX idx_gold_member_status (gold_member_status)
);

CREATE TABLE Service_Requests (
                                  request_id INT AUTO_INCREMENT PRIMARY KEY,
                                  client_id INT NOT NULL,
                                  title VARCHAR(255),
                                  description TEXT,
                                  date DATE,
                                  budget DECIMAL(10, 2),
                                  service_type VARCHAR(50),
                                  venue VARCHAR(255),
                                  status ENUM('pending', 'open', 'assigned', 'completed', 'canceled') DEFAULT 'open',
                                  assigned_provider_id INT,
                                  is_direct_booking BOOLEAN DEFAULT FALSE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  updated_at TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  FOREIGN KEY (client_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                                  FOREIGN KEY (assigned_provider_id) REFERENCES Users(user_id) ON DELETE SET NULL,
                                  INDEX idx_status (status),
                                  INDEX idx_client_id (client_id),
                                  INDEX idx_assigned_provider_id (assigned_provider_id)
);

CREATE TABLE Pitches (
                         pitch_id INT AUTO_INCREMENT PRIMARY KEY,
                         request_id INT NOT NULL,
                         provider_id INT NOT NULL,
                         message TEXT,
                         proposed_price DECIMAL(10, 2),
                         status ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (request_id) REFERENCES Service_Requests(request_id) ON DELETE CASCADE,
                         FOREIGN KEY (provider_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                         UNIQUE KEY uk_request_provider (request_id, provider_id),
                         INDEX idx_request_id (request_id),
                         INDEX idx_provider_id (provider_id)
);

CREATE TABLE Notifications (
                               notification_id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               message TEXT NOT NULL,
                               type ENUM('pitch', 'assignment', 'direct_booking', 'payment', 'review', 'booking_response', 'gold_member_request') NOT NULL,
                               is_read BOOLEAN DEFAULT FALSE,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                               INDEX idx_user_id (user_id),
                               INDEX idx_type (type)
);

CREATE TABLE Payments (
                          payment_id INT AUTO_INCREMENT PRIMARY KEY,
                          request_id INT NOT NULL,
                          client_id INT NOT NULL,
                          provider_id INT NOT NULL,
                          amount DECIMAL(10, 2) NOT NULL,
                          status ENUM('pending', 'completed', 'failed') DEFAULT 'pending',
                          transaction_id VARCHAR(255),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (request_id) REFERENCES Service_Requests(request_id) ON DELETE CASCADE,
                          FOREIGN KEY (client_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (provider_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                          INDEX idx_request_id (request_id),
                          INDEX idx_client_id (client_id),
                          INDEX idx_provider_id (provider_id)
);

CREATE TABLE Reviews (
                         review_id INT AUTO_INCREMENT PRIMARY KEY,
                         request_id INT NOT NULL,
                         client_id INT NOT NULL,
                         provider_id INT NOT NULL,
                         rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
                         comment TEXT,
                         status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (request_id) REFERENCES Service_Requests(request_id) ON DELETE CASCADE,
                         FOREIGN KEY (client_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                         FOREIGN KEY (provider_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                         INDEX idx_request_id (request_id),
                         INDEX idx_provider_id (provider_id)
);

CREATE TABLE Portfolios (
                            portfolio_id INT AUTO_INCREMENT PRIMARY KEY,
                            provider_id INT NOT NULL,
                            service_type VARCHAR(50) NOT NULL,
                            description TEXT,
                            media JSON,
                            status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (provider_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                            INDEX idx_provider_id (provider_id),
                            INDEX idx_status (status)
);

INSERT INTO Users (username, email, password, role, is_active)
VALUES ('admin', 'admin@eventsync.com', '$2a$10$dummyhashedpassword', 'admin', TRUE);