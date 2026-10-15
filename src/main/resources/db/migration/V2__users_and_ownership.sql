CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100),
    name VARCHAR(150) NOT NULL,
    provider VARCHAR(20) NOT NULL CHECK (provider IN ('LOCAL', 'GOOGLE', 'LINKEDIN')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE job_applications ADD COLUMN user_id UUID;
ALTER TABLE job_applications ADD CONSTRAINT fk_job_applications_user
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
CREATE INDEX idx_job_applications_user_id ON job_applications(user_id);
ALTER TABLE job_applications ADD COLUMN job_id VARCHAR(100);

ALTER TABLE job_applications DROP CONSTRAINT chk_job_application_status;
ALTER TABLE job_applications ADD CONSTRAINT chk_job_application_status CHECK (status IN (
    'SAVED', 'APPLIED', 'SCREENING', 'ASSESSMENT', 'INTERVIEW', 'OFFER',
    'ACCEPTED', 'REJECTED', 'WITHDRAWN', 'REFERRED'
));
