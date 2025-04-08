CREATE TABLE pf_audit_record (
    id BIGSERIAL PRIMARY KEY,
    operation VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT NOT NULL,
    details VARCHAR(1000),
    created_by_id BIGINT NOT NULL REFERENCES pf_user(id),
    created_time TIMESTAMP NOT NULL,
    modified_by_id BIGINT NOT NULL REFERENCES pf_user(id),
    modified_time TIMESTAMP NOT NULL
);