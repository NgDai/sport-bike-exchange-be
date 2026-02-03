-- 1. Table: role
CREATE TABLE role (
    role_id SERIAL PRIMARY KEY,
    role_name VARCHAR(255)
);

-- 2. Table: users
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255),
    password VARCHAR(255),
    full_name VARCHAR(255),
    email VARCHAR(255),
    phone VARCHAR(255),
    wallet_balance VARCHAR(255),
    status VARCHAR(255),
    create_date DATE
    -- Note: 'role' Set<String> is skipped in favor of the explicit 'UserRole' entity relationship
);

-- 3. Table: user_role
CREATE TABLE user_role (
    user_role_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    role_id INTEGER,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(role_id)
);

-- 4. Table: events
CREATE TABLE events (
    event_id SERIAL PRIMARY KEY,
    create_by INTEGER,
    name VARCHAR(255),
    location VARCHAR(255),
    start_date DATE,
    end_date DATE,
    seller_deposit_rate DOUBLE PRECISION,
    buyer_deposit_rate DOUBLE PRECISION,
    platform_fee_rate DOUBLE PRECISION,
    status VARCHAR(255),
    CONSTRAINT fk_events_creator FOREIGN KEY (create_by) REFERENCES users(user_id)
);

-- 5. Table: bike_listing
CREATE TABLE bike_listing (
    listing_id SERIAL PRIMARY KEY,
    seller_id INTEGER,
    event_id INTEGER,
    title VARCHAR(255),
    brand VARCHAR(255),
    model VARCHAR(255),
    category VARCHAR(255),
    frame_size VARCHAR(255),
    wheel_size VARCHAR(255),
    manufacture_year INTEGER,
    brake_type VARCHAR(255),
    transmission VARCHAR(255),
    weight DOUBLE PRECISION,
    image_url VARCHAR(255),
    description VARCHAR(255),
    price DOUBLE PRECISION,
    status VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_bike_listing_seller FOREIGN KEY (seller_id) REFERENCES users(user_id),
    CONSTRAINT fk_bike_listing_event FOREIGN KEY (event_id) REFERENCES events(event_id)
);

-- 6. Table: deposit
CREATE TABLE deposit (
    deposit_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    listing_id INTEGER,
    type VARCHAR(255),
    amount DOUBLE PRECISION,
    nvarchar VARCHAR(255), -- Field name from entity is 'nvarchar'
    create_at TIMESTAMP,
    CONSTRAINT fk_deposit_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_deposit_listing FOREIGN KEY (listing_id) REFERENCES bike_listing(listing_id)
);

-- 7. Table: check_in
CREATE TABLE check_in (
    check_in_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    event_id INTEGER,
    role_id VARCHAR(255), -- Mapped as String based on entity definition
    check_in_time TIMESTAMP,
    CONSTRAINT fk_check_in_user FOREIGN KEY (user_id) REFERENCES users(user_id),
    CONSTRAINT fk_check_in_event FOREIGN KEY (event_id) REFERENCES events(event_id)
);

-- 8. Table: wallet
CREATE TABLE wallet (
    wallet_id SERIAL PRIMARY KEY,
    user_id INTEGER,
    balance DOUBLE PRECISION,
    last_updated TIMESTAMP,
    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(user_id)
);

-- 9. Table: wallet_transaction
CREATE TABLE wallet_transaction (
    wallet_trans_id SERIAL PRIMARY KEY,
    wallet_id INTEGER,
    amount DOUBLE PRECISION,
    type VARCHAR(255),
    description VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_wallet_trans_wallet FOREIGN KEY (wallet_id) REFERENCES wallet(wallet_id)
);

-- 10. Table: reservation
CREATE TABLE reservation (
    reservation_id SERIAL PRIMARY KEY,
    buyer_id INTEGER,
    bike_listing_id INTEGER,
    status VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_reservation_buyer FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    CONSTRAINT fk_reservation_listing FOREIGN KEY (bike_listing_id) REFERENCES bike_listing(listing_id)
);

-- 11. Table: transaction
CREATE TABLE transaction (
    transaction_id SERIAL PRIMARY KEY,
    listing_id INTEGER,
    buyer_id INTEGER,
    seller_id INTEGER,
    deposit_id INTEGER,
    reservation_id INTEGER,
    status VARCHAR(255),
    amount DOUBLE PRECISION,
    created_at TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_transaction_listing FOREIGN KEY (listing_id) REFERENCES bike_listing(listing_id),
    CONSTRAINT fk_transaction_buyer FOREIGN KEY (buyer_id) REFERENCES users(user_id),
    CONSTRAINT fk_transaction_seller FOREIGN KEY (seller_id) REFERENCES users(user_id),
    CONSTRAINT fk_transaction_deposit FOREIGN KEY (deposit_id) REFERENCES deposit(deposit_id),
    CONSTRAINT fk_transaction_reservation FOREIGN KEY (reservation_id) REFERENCES reservation(reservation_id)
);

-- 12. Table: deposit_settlement
CREATE TABLE deposit_settlement (
    settlement_id SERIAL PRIMARY KEY,
    deposit_id INTEGER,
    receiver_id INTEGER,
    amount DOUBLE PRECISION,
    reason VARCHAR(255),
    create_at TIMESTAMP,
    CONSTRAINT fk_settlement_deposit FOREIGN KEY (deposit_id) REFERENCES deposit(deposit_id),
    CONSTRAINT fk_settlement_receiver FOREIGN KEY (receiver_id) REFERENCES users(user_id)
);

-- 13. Table: dispute
CREATE TABLE dispute (
    dispute_id SERIAL PRIMARY KEY,
    transaction_id INTEGER,
    raised_by INTEGER,
    reason VARCHAR(255),
    status VARCHAR(255),
    create_at TIMESTAMP,
    CONSTRAINT fk_dispute_transaction FOREIGN KEY (transaction_id) REFERENCES transaction(transaction_id),
    CONSTRAINT fk_dispute_raiser FOREIGN KEY (raised_by) REFERENCES users(user_id)
);

-- 14. Table: inspection_report
CREATE TABLE inspection_report (
    report_id SERIAL PRIMARY KEY,
    dispute_id INTEGER,
    inspector_id INTEGER,
    result VARCHAR(255),
    reason VARCHAR(255),
    note VARCHAR(255),
    create_at TIMESTAMP,
    CONSTRAINT fk_inspection_dispute FOREIGN KEY (dispute_id) REFERENCES dispute(dispute_id),
    CONSTRAINT fk_inspection_inspector FOREIGN KEY (inspector_id) REFERENCES users(user_id)
);

-- 15. Table: listing_approval
CREATE TABLE listing_approval (
    approval_id SERIAL PRIMARY KEY,
    listing_id INTEGER,
    approval_by INTEGER,
    decision VARCHAR(255),
    note VARCHAR(255),
    created_at TIMESTAMP,
    CONSTRAINT fk_approval_listing FOREIGN KEY (listing_id) REFERENCES bike_listing(listing_id),
    CONSTRAINT fk_approval_approver FOREIGN KEY (approval_by) REFERENCES users(user_id)
);
