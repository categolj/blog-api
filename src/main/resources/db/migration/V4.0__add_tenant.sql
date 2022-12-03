ALTER TABLE entry
    ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(128) DEFAULT '_';

ALTER TABLE entry
    DROP CONSTRAINT entry_pkey;
ALTER TABLE entry
    ADD CONSTRAINT entry_pkey PRIMARY KEY (entry_id, tenant_id);