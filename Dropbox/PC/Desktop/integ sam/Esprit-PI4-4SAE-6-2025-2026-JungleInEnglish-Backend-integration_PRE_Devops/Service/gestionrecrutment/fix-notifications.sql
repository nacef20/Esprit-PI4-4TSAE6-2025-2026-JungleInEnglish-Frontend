-- Run this once in MySQL to fix "Field 'read' doesn't have a default value"
-- The old 'read' column conflicts with the new 'is_read' column
ALTER TABLE notifications DROP COLUMN `read`;

-- Add job_offer_id for navigation from notification to job offer
ALTER TABLE notifications ADD COLUMN job_offer_id BIGINT NULL;
