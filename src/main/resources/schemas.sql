                    CREATE TABLE IF NOT EXISTS testwebflux.users (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,
                            age INT,
                            gender ENUM('MALE', 'FEMALE', 'OTHER'), -- Assuming GenderEnum has these values
                            address VARCHAR(255),
                            refresh_token VARCHAR(255),
                            created_at TIMESTAMP NULL DEFAULT NULL,
                            updated_at TIMESTAMP NULL DEFAULT NULL
                        );