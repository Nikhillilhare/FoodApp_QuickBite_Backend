-- Adds columns required for:
--  - Weekly Report notification preference (Settings > Notifications)
--  - Delivery Partners selection (Settings > Delivery)
--  - Two-Factor Authentication status (Settings > Security)

ALTER TABLE restaurant_setting
    ADD COLUMN weekly_report BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE restaurant_setting
    ADD COLUMN delivery_partners VARCHAR(255) NULL;

ALTER TABLE users
    ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE;
