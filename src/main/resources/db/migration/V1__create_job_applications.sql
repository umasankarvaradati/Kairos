CREATE TABLE job_applications (
    id UUID PRIMARY KEY,
    company_name VARCHAR(150) NOT NULL,
    job_title VARCHAR(150) NOT NULL,
    job_url VARCHAR(2048),
    location VARCHAR(150),
    application_source VARCHAR(50),
    status VARCHAR(30) NOT NULL,
    application_date DATE NOT NULL,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT chk_job_application_status CHECK (status IN (
        'SAVED', 'APPLIED', 'SCREENING', 'ASSESSMENT', 'INTERVIEW', 'OFFER',
        'ACCEPTED', 'REJECTED', 'WITHDRAWN'
    ))
);

CREATE INDEX idx_job_applications_status ON job_applications(status);
CREATE INDEX idx_job_applications_application_date ON job_applications(application_date);
CREATE INDEX idx_job_applications_company_name ON job_applications(company_name);
