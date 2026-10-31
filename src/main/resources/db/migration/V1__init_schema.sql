CREATE TABLE candidates (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name          VARCHAR(100)  NOT NULL,
    last_name           VARCHAR(100)  NOT NULL,
    email               VARCHAR(255)  NOT NULL,
    phone               VARCHAR(20),
    location            VARCHAR(100),
    current_company     VARCHAR(150),
    current_job_title   VARCHAR(150),
    headline            VARCHAR(200),
    experience_years    INT,
    notice_period_days  INT,
    highest_education   VARCHAR(100),
    status              VARCHAR(20)   NOT NULL DEFAULT 'NEW',
    source              VARCHAR(50),
    resume_url          VARCHAR(500),
    notes               TEXT,
    version             BIGINT        NOT NULL DEFAULT 0,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY ux_candidates_email (email)
);

CREATE INDEX ix_candidates_status ON candidates (status);
CREATE INDEX ix_candidates_location ON candidates (location);
CREATE INDEX ix_candidates_experience ON candidates (experience_years);
CREATE INDEX ix_candidates_created_at ON candidates (created_at);
CREATE INDEX ix_candidates_source ON candidates (source);
CREATE INDEX ix_candidates_notice_period ON candidates (notice_period_days);

CREATE TABLE candidate_skills (
    candidate_id BIGINT NOT NULL,
    skill        VARCHAR(50) NOT NULL,
    PRIMARY KEY (candidate_id, skill),
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
);

CREATE INDEX ix_candidate_skills_skill ON candidate_skills (skill);
