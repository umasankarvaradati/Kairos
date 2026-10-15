CREATE TABLE referrals (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    job_application_id UUID NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    linkedin_url VARCHAR(2048),
    email VARCHAR(320),
    message_template TEXT,
    status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING', 'MESSAGE_SENT', 'REFERRED')),
    sent_at TIMESTAMP WITH TIME ZONE,
    referred_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_referrals_user_application ON referrals(user_id, job_application_id);
