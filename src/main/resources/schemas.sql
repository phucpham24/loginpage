                    CREATE TABLE IF NOT EXISTS testwebflux.roles (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100),
                            description VARCHAR(100)
                        );
                    CREATE TABLE IF NOT EXISTS testwebflux.users (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            password VARCHAR(255) NOT NULL,
                            age INT,
                            gender ENUM('MALE', 'FEMALE', 'OTHER'), -- Assuming GenderEnum has these values
                            address VARCHAR(255),
                            refresh_token LONGTEXT,
                            created_at TIMESTAMP NULL DEFAULT NULL,
                            updated_at TIMESTAMP NULL DEFAULT NULL,
                            role_id BIGINT,
                            FOREIGN KEY (role_id) REFERENCES roles(id)

                        );

                        INSERT INTO testwebflux.roles (name, description)
                            SELECT 'USER', 'Default user role'
                            WHERE NOT EXISTS (
                                SELECT 1 FROM testwebflux.roles WHERE name = 'USER'
                            );

                        INSERT INTO testwebflux.roles (name, description)
                            SELECT 'ADMIN', 'Administrator role'
                            WHERE NOT EXISTS (
                                SELECT 1 FROM testwebflux.roles WHERE name = 'ADMIN'
                            );
